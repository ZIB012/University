package it.polimi.nsds.spark.lab.bank;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;

import static org.apache.spark.sql.functions.max;
import static org.apache.spark.sql.functions.when;

// Solution proposed by Andrea Zanin (thanks! :)
public class Bank2 {
    private static final boolean useCache = true;

    public static void main(String[] args) {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";

        final SparkSession spark = SparkSession.builder().master(master).appName("Bank").getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        // Load data from the csv files
        final List<StructField> mySchemaFields = new ArrayList<>();
        mySchemaFields.add(DataTypes.createStructField("person", DataTypes.StringType, true));
        mySchemaFields.add(DataTypes.createStructField("account", DataTypes.StringType, true));
        mySchemaFields.add(DataTypes.createStructField("amount", DataTypes.IntegerType, true));
        final StructType mySchema = DataTypes.createStructType(mySchemaFields);
        final Dataset<Row> deposits = spark.read().option("header", "false").option("delimiter", ",").schema(mySchema).csv(filePath + "files/bank/deposits.csv");
        final Dataset<Row> withdrawals = spark.read().option("header", "false").option("delimiter", ",").schema(mySchema).csv(filePath + "files/bank/withdrawals.csv");

        // Compute total withdrawals and total deposits in each account
        final Dataset<Row> totWithdraw = withdrawals.groupBy("account").sum("amount").select("account", "sum(amount)");
        final Dataset<Row> totDeposit = deposits.groupBy("account").sum("amount").select("account", "sum(amount)");

        // Perform an outer join to get all accounts, including those
        // that only had withdrawals or only had deposits
        final Dataset<Row> totals = totWithdraw
                .join(totDeposit, totDeposit.col("account").equalTo(totWithdraw.col("account")), "outer");

        // Replace the null values with 0 and compute the balance
        Column deposited = when(totDeposit.col("sum(amount)").isNull(), 0)
                .otherwise(totDeposit.col("sum(amount)"));
        Column withdrew = when(totWithdraw.col("sum(amount)").isNull(), 0)
                .otherwise(totWithdraw.col("sum(amount)"));
        Column balance = deposited.minus(withdrew).as("balance");

        Column accountName = when(totWithdraw.col("account").isNull(), totDeposit.col("account"))
                .otherwise(totWithdraw.col("account")).as("account");

        // Create a table with the balance in each account and print it in console sorted by balance
        final Dataset<Row> balances = totals.select(balance, accountName);
        balances.sort(balances.col("balance").desc()).show();

        spark.close();
    }
}