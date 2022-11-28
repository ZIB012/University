package it.polimi.middleware.spark.batch.iterative;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Arrays;

/**
 * Start from a dataset of investments. Each element is a Tuple2(amount_owned, interest_rate).
 * At each iteration the new amount is (amount_owned * (1+interest_rate)).
 *
 * Implement an iterative algorithm that computes the new amount for each investment and stops
 * when the overall amount overcomes 1000.
 *
 * Set the value of the flag useCache to see the effects of caching.
 */

public class InvestmentSimulator {
    private static final boolean useCache = true;

    public static void main(String[] args) {
        final String master = args.length > 0 ? args[0] : "local[1]";
        final String filePath = args.length > 1 ? args[1] : "./";
        final double threshold = 35;

        final SparkConf conf = new SparkConf().setMaster(master).setAppName("InvestmentSimulator");
        final JavaSparkContext sc = new JavaSparkContext(conf);
        sc.setLogLevel("ERROR");

        final JavaRDD<String> textFile = sc.textFile(filePath + "files/iterative/investment.txt");

        // Transforms each line into a tuple (amount_owned, investment_rate)
        JavaRDD<Tuple2<Double, Double>> investments = textFile.map(w -> {
            String[] values = w.split(" ");
            double amountOwned = Double.parseDouble(values[0]);
            double investmentRate = Double.parseDouble(values[1]);
            return new Tuple2<>(amountOwned, investmentRate);
        });

        // spark doesn't have any native iterative operator --> we can do them inside the sequential java code
        // within the loop we can spawn multiple jobs (that's the way spark manage iterations)
        int iteration = 0;
        double sum = sumAmount(investments);
        // at each iteration we spawn a job
        while (sum < threshold) {
            iteration++;
            investments = investments.map(i -> {
                System.out.println("AAA");
                return new Tuple2<>(i._1*(1+i._2), i._2);});  // we update the investments:
                    // the new RDD is computed from the previous RDD where each element is trasnformed in this way:
                        // the second part remains the same, while the first changes

                investments.cache();    // --> save the result of the previous step for the next one

            sum = sumAmount(investments);
        }

        System.out.println("Sum: " + sum + " after " + iteration + " iterations");
        sc.close();
    }

    // the .map() operator [the one that takes the investment and computes the new amount of money] we would expect that
    // is executed 5 times for each iteration. How can we validate this hypothesis? We can put a print inside the .map()
    // and we print one line every time this .map() is executed.
    // We find instead a lot of prints:
        // at first iteration: this action only requires to do one pass over the investment (1 iteration)
        // in the second: investment needs to start from the result of the previous iteration, but those were computed
        // in a different job, and we didn't save them in memory, so we don't have them and spark recomputes them.
            // --> in the second iteration spark executes twice .map() and so on...
    // So the problem here is that each iteration represents a job, the job is based on the result of the previous one,
       // but jobs do not share partial results.
    // If we want to persist the result of the jobs, we can cache them, so that spark will find the results of the previous iteration


    private static final double sumAmount(JavaRDD<Tuple2<Double, Double>> investments) {
        return investments
                .mapToDouble(a -> a._1)
                .sum(); // sum it's a reduction --> it's spawning a job
    }

}
