package it.polimi.middleware.spark.batch.wordcount;

import java.util.Arrays;

import org.apache.commons.compress.harmony.unpack200.Segment;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;

import scala.Tuple2;

public class WordCount {

    public static void main(String[] args) {
        //LogUtils.setLogLevel();

        final String master = args.length > 0 ? args[0] : "local[4]";   // we run in the local environment with 4 threads
        final String filePath = args.length > 1 ? args[1] : "./";

        // in the configuration variable conf we set the application name and we set the address of the Master for Spark
        final SparkConf conf = new SparkConf().setMaster(master).setAppName("WordCount");
        final JavaSparkContext sc = new JavaSparkContext(conf); // to use spark you need to create a JavaSparkContext

        // it reads lines from the textFile (this RDD contains strings, one string for each line)
        final JavaRDD<String> lines = sc.textFile(filePath + "files/wordcount/in.txt");
        // each line is converted in multiple words (it's a transformation)
        final JavaRDD<String> words = lines.flatMap(line -> Arrays.asList(line.split(" ")).iterator());
        // we map each line into a pair, we map each word into a Tuple which contains the word and te count (set to 1)
        final JavaPairRDD<String, Integer> pairs = words.mapToPair(s -> new Tuple2<>(s, 1));
        // we reduce by key --> we consider words as key and we reduce all the values for same key
        final JavaPairRDD<String, Integer> counts = pairs.reduceByKey((a, b) -> a + b); // given two values for the same key we reduce the value
        // we print out our solution
        System.out.println(counts.collect()); // collect() is an action --> something that returns a result to the driver programm

        // we close the context
        sc.close();
    }

}