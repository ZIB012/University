package it.polimi.middleware.spark.batch.bank;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.max;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.Aggregator$;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

/**
 * Bank example
 *
 * Input: csv files with list of deposits and withdrawals, having the following
 * schema ("person: String, account: String, amount: Int)
 *
 * Queries
 * Q1. Print the total amount of withdrawals for each person.
 * Q2. Print the person with the maximum total amount of withdrawals
 * Q3. Print all the accounts with a negative balance
 */
public class Bank_lab {
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

        // Q1. Total amount of withdrawals for each person

        // TODO
        // We are taking the withdrawls, we are grouping by person, we sum all the amounts for each group and we select to keep only two columns
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

        // TODO

        final long maxTotal = sumWithdrawals
                .agg(max("sum(amount)"))
                .first()
                .getLong(0);

        final Dataset<Row> maxWithdrawals = sumWithdrawals
                .filter(sumWithdrawals.col("sum(amount)").equalTo(maxTotal));

        maxWithdrawals.show();

        // Q3 Accounts with negative balance

        // TODO

        final Dataset<Row> totWithdrawals = withdrawals
                .groupBy("account")
                .sum("amount")
                .drop("person")
                .as("totalWithdrawals");

        final Dataset<Row> totDeposits = deposits
                .groupBy("account")
                .sum("amount")
                .drop("person")
                .as("totalDeposits");

        final Dataset<Row> negativeAccounts = totWithdrawals
                .join(totDeposits, totDeposits.col("account").equalTo(totWithdrawals.col("account")), "left_outer")
                .filter(totDeposits.col("sum(amount)").isNull().and(totWithdrawals.col("sum(amount)").gt(0)).or
                        (totWithdrawals.col("sum(amount)").gt(totDeposits.col("sum(amount)")))
                ).select(totWithdrawals.col("account"));

        totWithdrawals.join(totDeposits, totDeposits.col("account").equalTo(totWithdrawals.col("account")), "outer").show();

        negativeAccounts.show();

        // Q4 Prints all the account in a decreasing order

        // TODO

        Dataset<Row> balance = withdrawals.withColumn("amount", col("amount").multiply(-1)).union(deposits);
        balance.show();


        spark.close();

    }
}