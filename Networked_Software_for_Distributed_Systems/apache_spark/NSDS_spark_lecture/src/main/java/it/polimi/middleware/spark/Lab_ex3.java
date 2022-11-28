package it.polimi.middleware.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.window;

/**
 * This code snippet exemplifies a typical scenario in event processing: merging
 * incoming events with some background knowledge.
 *
 * A static dataset (read from a file) classifies products (associates products to the
 * class they belong to).  A stream of products is received from a socket.
 *
 * We want to count the number of products of each class in the stream. To do so, we
 * need to integrate static knowledge (product classification) and streaming data
 * (occurrences of products in the stream).
 */

public class Lab_ex3 {
    public static void main(String[] args) throws TimeoutException {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 3 ? args[1] : "./";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName("EventEnrichment")
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        final List<StructField> productClassificationFields = new ArrayList<>();
        productClassificationFields.add(DataTypes.createStructField("product", DataTypes.StringType, false));
        productClassificationFields.add(DataTypes.createStructField("classification", DataTypes.StringType, false));
        final StructType productClassificationSchema = DataTypes.createStructType(productClassificationFields);

        final Dataset<Row> inputRecords = spark
                .readStream()
                .format("rate")
                .option("rowsPerSecond", 1)
                .load();

        final Dataset<Row> productsClassification = spark
                .read()
                .option("header", "false")
                .option("delimiter", ",")
                .schema(productClassificationSchema)
                .csv(filePath + "files/enrichment/product_classification.csv");

        productsClassification.show();

        final StreamingQuery query = inputRecords
                .join(productsClassification, inputRecords.col("value").equalTo(productsClassification.col("product")))
                .groupBy(window(col("timestamp"), "30 seconds", "5 seconds"), col("classification"))
                .count()
                .writeStream()
                .outputMode("update")   // update --> spark print only the updates
                .format("console")
                .start();

        try {
            query.awaitTermination();
        } catch (final StreamingQueryException e) {
            e.printStackTrace();
        }

        spark.close();
    }
}