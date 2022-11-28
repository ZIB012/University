package it.polimi.middleware.spark.streaming.wordcount;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.Function3;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.State;
import org.apache.spark.streaming.StateSpec;
import org.apache.spark.streaming.api.java.JavaMapWithStateDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

import scala.Tuple2;

// This example illustrates how we handle state in stream applications
// Windows are a particular form of state that spans across RDDs, if we define a window of 10 seconds, then spark will
    // remember the last 10 seconds of data and it will keep a state that involves the last 10 second of data.
// Sometimes I may want to customize the state you keep across the evaluation of subsequent RDDs
    // (windows are not enough, I want to have my own state that spans multiple evaluation)
    // Spark let you do so by using special version of operators that consider state.

public class StreamingWordCountWithState {
    public static void main(String[] args) {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String socketHost = args.length > 1 ? args[1] : "localhost";
        final int socketPort = args.length > 2 ? Integer.parseInt(args[2]) : 9999;

        final SparkConf conf = new SparkConf().setMaster(master).setAppName("StreamingWordCountWithState");
        final JavaStreamingContext sc = new JavaStreamingContext(conf, Durations.seconds(1));
        sc.sparkContext().setLogLevel("ERROR");

        // Checkpoint directory where the state is stored
        sc.checkpoint("/tmp/");

        // function that considers new input elements, but also previous state (global count we have received from the global computation)
        final Function3<String, Optional<Integer>, State<Integer>, Tuple2<String, Integer>> mapFunction = //
                (word, count, state) -> {
                    final int sum = count.orElse(0) + (state.exists() ? state.get() : 0);
                    state.update(sum);
                    return new Tuple2<>(word, sum);
                };

        final List<Tuple2<String, Integer>> initialList = new ArrayList<>();
        final JavaPairRDD<String, Integer> initialRDD = sc.sparkContext().parallelizePairs(initialList);

        final JavaMapWithStateDStream<String, Integer, Integer, Tuple2<String, Integer>> state = sc
                .socketTextStream(socketHost, socketPort)
                .map(String::toLowerCase)
                .flatMap(line -> Arrays.asList(line.split(" ")).iterator())
                .mapToPair(word -> new Tuple2<>(word, 1))
                .mapWithState(StateSpec.function(mapFunction).initialState(initialRDD));


        state.foreachRDD(rdd -> rdd
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
