package edu.ucr.cs.cs226.GameScout.integration;

import edu.ucr.cs.cs226.GameScout.model.Game;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.LinearSVC;
import org.apache.spark.ml.classification.LinearSVCModel;
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator;
import org.apache.spark.ml.feature.*;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.tuning.ParamGridBuilder;
import org.apache.spark.ml.tuning.TrainValidationSplit;
import org.apache.spark.ml.tuning.TrainValidationSplitModel;
import org.apache.spark.sql.*;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.grouping;

import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.jvnet.hk2.annotations.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SparkService {
    private final SparkSession sparkSession;
    private Dataset<Row> gameDescriptions;

    private Dataset<Row> gameRanking;
//    private Dataset<Row> gameReviews;
//    @Autowired
//    private JavaSparkContext sparkContext;

    @Autowired
    public SparkService(SparkSession sparkSession) {
        this.sparkSession = sparkSession;
        loadData();
    }

    private void loadData() {
        gameDescriptions = sparkSession.read()
                .format("csv")
                .option("sep", ",")
                .option("quote", "\"")
                .option("escape", "\"")
                .option("multiline", "true")
                .option("inferSchema", "true")
                .option("header", "true")
                .csv("src/main/resources/data/games_description.csv");

        gameRanking = sparkSession.read()
                .option("inferSchema", "true")
                .option("header", "true")
                .csv("src/main/resources/data/games_ranking.csv");

//        gameReviews = sparkSession.read()
//                .option("sep", ",")
//                .option("quote", "\"")
//                .option("escape", "\"")
//                .option("multiline", "true")
//                .option("inferSchema", "true")
//                .option("header", "true")
//                .csv("src/main/resources/data/steam_game_reviews.csv");
//        Sentiment();
    }

    public String appendSQuery(String tableName, List<String> columns, String keyword){
        StringBuilder sqlQuery = new StringBuilder("SELECT * FROM ");
        sqlQuery.append(tableName).append(" WHERE ");

        // Append the LIKE conditions for each column
        for (int i = 0; i < columns.size(); i++) {
            sqlQuery.append(columns.get(i))
                    .append(String.format(" LIKE '%%%s%%'",keyword));

            if (i < columns.size() - 1) {
                sqlQuery.append(" OR ");
            }
        }

        return sqlQuery.toString();
    }

    public List<Game> findGameByKeywords(String keyword) {
        gameDescriptions.createOrReplaceTempView("rankings");

        List<String> columns = List.of("name", "short_description", "long_description","genres","developer","publisher");
        String sqlQuery = appendSQuery("rankings",columns, keyword);
        System.out.println(sqlQuery);
        Dataset<Row> df = sparkSession.sql(sqlQuery);
        df.show();
        return df.as(Encoders.bean(Game.class)).collectAsList();
    }

    public List<Map<String, Object>> getRanking(String genre, String type) {
        
        gameRanking.createOrReplaceTempView("ranking");


        String sqlQuery = String.format(
                "SELECT * FROM ranking WHERE rank BETWEEN 1 AND 16 AND genre = '%s' AND rank_type = '%s'",
                genre, type
        );

        Dataset<Row> df = sparkSession.sql(sqlQuery);

        // Convert each row to a Map<String, Object>

        return df.collectAsList().stream()
                .map(row -> {
                    // Convert each row to a map
                    Map<String, Object> map = new java.util.HashMap<>();
                    for (String field : row.schema().fieldNames()) {
                        map.put(field, row.getAs(field));
                    }
                    return map;
                })
                .collect(Collectors.toList());
    }

    public String sentiment(String review, String recommendation) {
        TrainValidationSplitModel trainValidationSplitModel = TrainValidationSplitModel.load("src/main/resources/models/sentiment_model");

        // Extract the best PipelineModel
        PipelineModel bestPipelineModel = (PipelineModel) trainValidationSplitModel.bestModel();

        // Define the schema
        StructType schema = DataTypes.createStructType(new StructField[]{
                DataTypes.createStructField("review", DataTypes.StringType, false),
                DataTypes.createStructField("recommendation", DataTypes.StringType, false)
        });

        // Create data entries
        List<Row> data = new ArrayList<>();
        data.add(RowFactory.create(review, recommendation));

        // Convert the data into a Dataset
        Dataset<Row> newReviews = this.sparkSession.createDataFrame(data, schema);

        // Filter out null reviews
        newReviews = newReviews.filter(newReviews.col("review").isNotNull());

        // Apply the model for predictions
        Dataset<Row> predictions = bestPipelineModel.transform(newReviews);

        // Map "recommendation" to a numerical label for accuracy evaluation
        predictions = predictions.withColumn("label",
                functions.when(functions.col("recommendation").equalTo("Recommended"), 0.0)
                        .otherwise(1.0));

        // Evaluate accuracy
        Dataset<Row> correctPredictions = predictions.filter(functions.col("prediction").equalTo(functions.col("label")));
        long totalDataCount = predictions.count();
        long correctCount = correctPredictions.count();
        double accuracy = (double) correctCount / totalDataCount;

        System.out.println("Accuracy on new data: " + accuracy);

        // Extract the first prediction
        Row predictionRow = predictions.select("prediction").first();
        double prediction = predictionRow.getDouble(0); // Assuming "prediction" is a double column

        // Return the prediction as a string
        return prediction == 0.0 ? "Recommended" : "Not Recommended";
    }
}
