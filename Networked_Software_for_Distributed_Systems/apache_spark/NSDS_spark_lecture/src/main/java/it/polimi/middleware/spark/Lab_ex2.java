package it.polimi.middleware.spark;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import static org.apache.spark.sql.functions.*;

public class Lab_ex2 {
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
        mySchemaFields.add(DataTypes.createStructField("friend", DataTypes.StringType, true));

        final StructType mySchema = DataTypes.createStructType(mySchemaFields);

        final Dataset<Row> relation = spark
                .read()
                .option("header", "false")
                .option("delimiter", ",")
                .schema(mySchema)
                .csv(filePath + "files/relation.csv");

        relation.cache();

        Dataset<Row> relations = relation;

        Dataset<Row> solution = relation;
        Dataset<Row> indirect = spark.emptyDataFrame();


        while (!relations.isEmpty())
        {
            relations = relations.as("1").join(solution.as("2"), col("1.friend").equalTo(col("2.person")), "inner").
                    drop(col("1.friend")).drop(col(("2.person")));
            solution = solution.union(relations);
            indirect = (indirect.isEmpty()) ? relations : indirect.union(relations);
            indirect.cache();
            solution.cache();
            relations.cache();

        }
        System.out.println("Indirect friendships:");
        indirect.show();
        System.out.println("All friendships:");
        solution.show();




    }
}
