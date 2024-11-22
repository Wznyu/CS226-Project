package edu.ucr.cs.cs226.GameScout;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.api.java.UDF1;

import java.util.HashMap;
import java.util.Map;


public class dataPreparation {
    public static void main(String[] args) {
        SparkSession sparkSession = SparkSession.builder()
                .appName("GameReviewSentimentAnalysis")
                .master("local[*]")
                .config("spark.driver.memory", "4g")
                .config("spark.executor.memory", "4g")
                .config("spark.cores.max", "2")
                .config("spark.sql.shuffle.partitions", "8")
                .getOrCreate();

        // Set the logging level
//        sparkSession.sparkContext().setLogLevel("ERROR");
        sparkSession.sparkContext().setLogLevel("ERROR");


        Dataset<Row> gameReviews = sparkSession.read()
                .option("sep", ",")
                .option("quote", "\"")
                .option("escape", "\"")
                .option("multiline", "true")
                .option("inferSchema", "true")
                .option("header", "true")
                .csv("src/main/resources/data/steam_game_reviews.csv");


        gameReviews.createOrReplaceTempView("reviews");



        UDF1<String, String> expandContractions = (String text) -> {
            if (text == null) return null;
            Map<String, String> contractions = new HashMap<>();
            contractions.put("don't", "do not");
            contractions.put("doesn't", "does not");
            contractions.put("it's", "it is");
            contractions.put("can't", "can not");
            contractions.put("won't", "will not");
            contractions.put("didn't", "did not");
            contractions.put("haven't", "have not");
            contractions.put("hasn't", "has not");
            contractions.put("I'm", "I am");
            contractions.put("you're", "you are");
            contractions.put("we're", "we are");
            // Add more contractions as needed
            for (Map.Entry<String, String> entry : contractions.entrySet()) {
                text = text.replaceAll("\\b" + entry.getKey() + "\\b", entry.getValue());
            }
            return text;
        };

        sparkSession.udf().register("expandContractions", expandContractions, DataTypes.StringType);

        String expandQuery = """ 
                SELECT review,
                expandContractions(review) AS expanded_review,
                recommendation
                FROM reviews
                """;
        Dataset<Row> expandedData = sparkSession.sql(expandQuery);
        expandedData.show(5,false);

        expandedData.createOrReplaceTempView("reviews");


        String cleaningQuery = """
                SELECT
                regexp_replace(expanded_review, '[^\\\\w\\\\s]', '') AS review,
                recommendation
                FROM reviews
                """;
        Dataset<Row> cleanedData = sparkSession.sql(cleaningQuery);

        cleanedData = cleanedData.filter(cleanedData.col("review").isNotNull());

        cleanedData.show(5,false);
        cleanedData.write().save("src/main/resources/data/filtered_reviews");
    }

}
