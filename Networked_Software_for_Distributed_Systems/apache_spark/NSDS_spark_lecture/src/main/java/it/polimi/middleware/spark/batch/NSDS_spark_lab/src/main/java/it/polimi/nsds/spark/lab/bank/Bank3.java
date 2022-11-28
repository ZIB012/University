package it.polimi.nsds.spark.lab.bank;

import static org.apache.spark.sql.functions.max;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

// Solution proposed by Walter Catalfamo (thanks :)
public class Bank3 {
    private static final boolean useCache = true;

    public static void main(String[] args) {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";
        final String appName = useCache ? "BankWithCache" : "BankNoCache";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName(appName)
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        final List<StructField> mySchemaFields = new ArrayList<>();
        mySchemaFields.add(DataTypes.createStructField("person", DataTypes.StringType, true));
        mySchemaFields.add(DataTypes.createStructField("account", DataTypes.StringType, true));
        mySchemaFields.add(DataTypes.createStructField("amount", DataTypes.IntegerType, true));
        final StructType mySchema = DataTypes.createStructType(mySchemaFields);

        final Dataset<Row> deposits = spark
                .read()
                .option("header", "false")
                .option("delimiter", ",")
                .schema(mySchema)
                .csv(filePath + "files/bank/deposits.csv");

        final Dataset<Row> withdrawals = spark
                .read()
                .option("header", "false")
                .option("delimiter", ",")
                .schema(mySchema)
                .csv(filePath + "files/bank/withdrawals.csv");

        // Used in two different queries
        if (useCache) {
            withdrawals.cache();
        }

        // Q1. Total amount of withdrawals for each person

        final Dataset<Row> sumWithdrawals = withdrawals
                .groupBy("person")
                .sum("amount")
                .select("person", "sum(amount)");

        // Used in two different queries
        if (useCache) {
            sumWithdrawals.cache();
        }

        sumWithdrawals.show();

        // Q2. Person with the maximum total amount of withdrawals

        final long maxTotal = sumWithdrawals
                .agg(max("sum(amount)"))
                .first()
                .getLong(0);

        final Dataset<Row> maxWithdrawals = sumWithdrawals
                .filter(sumWithdrawals.col("sum(amount)").equalTo(maxTotal));

        maxWithdrawals.show();

        // Q3 Accounts with negative balance

        final Dataset<Row> totWithdrawals = withdrawals
                .groupBy("account")
                .sum("amount")
                .drop("person")
                .as("totalWithdrawals");
        totWithdrawals.show();

        final Dataset<Row> totDeposits = deposits
                .groupBy("account")
                .sum("amount")
                .drop("person")
                .as("totalDeposits");

        totDeposits.show();


        final Dataset<Row> negativeAccounts = totWithdrawals
                .join(totDeposits, totDeposits.col("account").equalTo(totWithdrawals.col("account")), "left_outer")
                .filter(totDeposits.col("sum(amount)").isNull().and(totWithdrawals.col("sum(amount)").gt(0)).or
                        (totWithdrawals.col("sum(amount)").gt(totDeposits.col("sum(amount)")))
                ).select(totWithdrawals.col("account"));

        negativeAccounts.show();


        // Q4 accounts sorted by balance


        withdrawals.withColumnRenamed("sum(amount)", "amount").createOrReplaceTempView("withdrawals");
        deposits.withColumnRenamed("sum(amount)", "amount").createOrReplaceTempView("deposits");

        Dataset<Row> commonAccountsBalance = spark.sql("" +
                        "SELECT          w.account, w.amount-d.amount as amount\n" +
                        "FROM            withdrawals w, deposits d\n" +
                        "WHERE           w.account = d.account")
                .groupBy("account")
                .sum("amount")
                .withColumnRenamed("sum(amount)", "amount");

        Dataset<Row> onlyWithdrawals = spark.sql("\n" +
                        "SELECT          account, amount\n" +
                        "FROM            withdrawals\n" +
                        "WHERE           account not in (select account from deposits)")
                .groupBy("account")
                .sum("amount")
                .withColumnRenamed("sum(amount)", "amount");

        Dataset<Row> onlyDeposits = spark.sql("\n" +
                        "SELECT          account, amount\n" +
                        "FROM            deposits\n" +
                        "WHERE           account not in (select account from withdrawals)         ")
                .groupBy("account")
                .sum("amount")
                .withColumnRenamed("sum(amount)", "amount");

        Dataset<Row> fullBalance = commonAccountsBalance
                .union(onlyDeposits)
                .union(onlyWithdrawals)
                .orderBy("amount");
        fullBalance.show();


        spark.close();

    }
}