package it.polimi.nsds.spark.eval;

import org.apache.spark.api.java.JavaRDD;
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

import static org.apache.spark.sql.functions.*;


/**
 * Group number: 34
 * Group members:
 * member 1  Marco Rapetti  10612623
 * member 2  Viola Brenzone  10628532
 * member 3  Federico Lanteri  10677037
 */
public class SparkGroup34 {
    public static void main(String[] args) throws TimeoutException {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName("SparkEval")
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        final List<StructField> citiesRegionsFields = new ArrayList<>();
        citiesRegionsFields.add(DataTypes.createStructField("city", DataTypes.StringType, false));
        citiesRegionsFields.add(DataTypes.createStructField("region", DataTypes.StringType, false));
        final StructType citiesRegionsSchema = DataTypes.createStructType(citiesRegionsFields);

        final List<StructField> citiesPopulationFields = new ArrayList<>();
        citiesPopulationFields.add(DataTypes.createStructField("id", DataTypes.IntegerType, false));
        citiesPopulationFields.add(DataTypes.createStructField("city", DataTypes.StringType, false));
        citiesPopulationFields.add(DataTypes.createStructField("population", DataTypes.IntegerType, false));
        final StructType citiesPopulationSchema = DataTypes.createStructType(citiesPopulationFields);

        final Dataset<Row> citiesPopulation = spark
                .read()
                .option("header", "true")
                .option("delimiter", ";")
                .schema(citiesPopulationSchema)
                .csv(filePath + "files/cities_population.csv");

        final Dataset<Row> citiesRegions = spark
                .read()
                .option("header", "true")
                .option("delimiter", ";")
                .schema(citiesRegionsSchema)
                .csv(filePath + "files/cities_regions.csv");

        // TODO: add code here if necessary

        Dataset<Row> data_joined = citiesPopulation.as("cit").join(citiesRegions.as("reg"),col("cit.city").equalTo(col("reg.city")),"outer");
        data_joined.cache();


        // TODO: query Q1

        final Dataset<Row> q1 = data_joined.drop(col("cit.city")).drop(col("reg.city")).drop(col("cit.id"))
                .groupBy(col("region")).sum();

        System.out.println("Total population for each region");
        q1.show();


        // TODO: query Q2

        Dataset<Row> maxx = data_joined.drop(col("cit.city")).groupBy(col("region")).max("population");
        maxx.cache();

        final Dataset<Row> q2 = data_joined.drop(col("cit.city")).groupBy(col("region")).count()
                .as("final").join(maxx.as("max"), col("final.region").equalTo(col("max.region")), "inner")
                .drop(col("max.region"));

        System.out.println("number of cities and max population in a city for each region");
        q2.show();


        // JavaRDD where each element is an integer and represents the population of a city
        JavaRDD<Integer> population = citiesPopulation.toJavaRDD().map(r -> r.getInt(2));

        // TODO: add code here to produce the output for query Q3

        final int threshold = 100000000;

        int sum = sumPopulation(population);
        int year = 0;

        while(sum < threshold)
        {
            population = population.map(SparkGroup34::change_population);
            sum = sumPopulation(population);
            System.out.println("Year: " + String.valueOf(++year) + ", total population: " + sum);
            population.cache();
        }


        // Bookings: the value represents the city of the booking
        final Dataset<Row> bookings = spark
                .readStream()
                .format("rate")
                .option("rowsPerSecond", 100)
                .load();

        // TODO query Q4

        final StreamingQuery q4 = bookings.join(data_joined, bookings.col("value").equalTo(data_joined.col("id")))
                .groupBy(col("region"), window(col("timestamp"), "30 seconds", "5 seconds"))
                .count()
                .writeStream()
                .outputMode("update")
                .format("console")
                .start();

        try {
            q4.awaitTermination();
        } catch (final StreamingQueryException e) {
            e.printStackTrace();
        }

        spark.close();
    }

    private static int sumPopulation(JavaRDD<Integer> population) {
        return population.reduce(Integer::sum);
    }

    private static int change_population (int value)
    {
        if (value <= 1000)
            return (int) (value*0.99);
        else
            return (int) (value*1.01);
    }
}