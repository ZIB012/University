package it.polimi.middleware.spark.streaming.wordcount;

import java.util.Arrays;

import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import scala.Tuple2;

public class StreamingWordCount {
    public static void main(String[] args) {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String socketHost = args.length > 1 ? args[1] : "localhost";
        final int socketPort = args.length > 2 ? Integer.parseInt(args[2]) : 9999;
        // we first need to create a source --> spark expects a socket on my localhost on port 9999
                // in my terminal: nc -l 9999

        final SparkConf conf = new SparkConf().setMaster(master).setAppName("StreamingWordCountSum");

        // To start the computation in Spark Streaming you have to define a JavaStreamingContext
        final JavaStreamingContext sc = new JavaStreamingContext(conf, Durations.seconds(1)); // we need to specify a duration
                // in this case Spark will create a new RDD every 1 second, with all the data from the stream that has been received within that interval
                // if the RDD is not empty, then it's processed by the engine

        // if we don't define a window, we are fixed to the Duration of 1 second
        // a window is a construct that enables to group elements together, and it's specified by 2 parameters
            // the first one si the size (length of the window), the second is the slide (how frequently a window needs to be evaluated)

        final JavaPairDStream<String, Integer> counts = sc.socketTextStream(socketHost, socketPort)
                .window(Durations.seconds(10), Durations.seconds(5))
                .map(String::toLowerCase)  // we first make a transformation (remove upper cases)
                .flatMap(line -> Arrays.asList(line.split(" ")).iterator())  // we transform each line into words
                .mapToPair(s -> new Tuple2<>(s, 1)) // we transform each word in a pair that contains the word and a count which starts to 1
                .reduceByKey((a, b) -> a + b);  // we reduce by key --> we sum all the counts for the same key (== word)
        sc.sparkContext().setLogLevel("ERROR");

        // finally we perform an action .collect() for each RDD in the stream that is not empty
        counts.foreachRDD(rdd -> rdd
                .collect()
                .forEach(System.out::println)
        );

        sc.start();

        try {
            sc.awaitTermination();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        sc.close();
    }
}