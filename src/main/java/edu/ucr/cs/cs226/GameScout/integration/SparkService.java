package edu.ucr.cs.cs226.GameScout.integration;

import edu.ucr.cs.cs226.GameScout.model.Game;
import org.apache.hadoop.shaded.org.eclipse.jetty.websocket.common.frames.DataFrame;
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

import static org.apache.spark.sql.functions.to_date;
import static org.apache.spark.sql.functions.when;
import static org.apache.spark.sql.functions.concat;
import static org.apache.spark.sql.functions.lit;


import org.jvnet.hk2.annotations.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SparkService {
    private final SparkSession sparkSession;
    private Dataset<Row> gameDescriptions;

    private Dataset<Row> gameRanking;
    private Dataset<Row> gameReviews;

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

        gameReviews = sparkSession.read()
                .option("sep", ",")
                .option("quote", "\"")
                .option("escape", "\"")
                .option("multiline", "true")
                .option("inferSchema", "true")
                .option("header", "true")
                .csv("src/main/resources/data/steam_game_reviews.csv");
//        sentiment();
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

    public List<Map<String, Object>> getPopular(String start, String end){
        gameReviews.createOrReplaceTempView("abc");

        // 3. Select date and name columns

        Dataset<Row> selectedDF = sparkSession.sql("SELECT * FROM abc");
//        Dataset<Row> selectedDF = sparkSession.sql("SELECT date, game_name FROM abc");

//        selectedDF.show(20);
//        selectedDF = selectedDF.withColumn("date",
////                functions.when(functions.to_date(col("date"), "d MMMM").isNotNull(),
////                                functions.to_date(functions.concat(col("date"), functions.lit(" 2024")), "d MMMM yyyy"))
////                        .when(functions.to_date(col("date"), "MMMM d").isNotNull(),
////                                functions.to_date(functions.concat(col("date"), functions.lit(" 2024")), "MMMM d yyyy"))
////                        .when(functions.to_date(col("date"), "MMMM d, yyyy").isNotNull(),
////                                functions.date_format(functions.to_date(col("date"), "MMMM d, yyyy"), "d MM yyyy"))
////                        .otherwise(col("date"))
////        );
//
//        selectedDF.show(20);
//        selectedDF = selectedDF.withColumn("date",
//                functions.when(functions.to_date(col("date"), "yyyy MM dd").isNotNull(),
//                                functions.to_date(col("date"), "d MM yyyy"))
//                        .when(functions.to_date(col("date"), "MMMM d yyyy").isNotNull(),
//                                functions.to_date(col("date"), "d MM yyyy"))
//                        .when(functions.to_date(col("date"), "MMMM d, yyyy").isNotNull(),
//                                functions.to_date(col("date"), "d MM yyyy"))
//                        .otherwise(col("date"))
//        );
//
//
//        selectedDF.show(20);
//        selectedDF = selectedDF.filter(col("date").isNotNull());

//        selectedDF = selectedDF.withColumn("date",
//                functions.when(functions.to_date(col("date"), "MMMM d, yyyy").isNotNull(),
//                                functions.date_format(functions.to_date(col("date"), "MMMM d, yyyy"), "MM d yyyy"))
//                        .otherwise(col("date"))
//        );
        Dataset<Row> result = sparkSession.sql("SELECT game_name, COUNT(*) AS count FROM abc WHERE date >= '" + start + "' AND date <= '" + end + "' GROUP BY game_name ORDER BY count DESC");


        result.show();
        return result.collectAsList().stream()
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

    public static String convertMonth(String month) {
        String monthName;
        switch (month) {
            case "01":
                monthName = "January";
                break;
            case "02":
                monthName = "February";
                break;
            case "03":
                monthName = "March";
                break;
            case "04":
                monthName = "April";
                break;
            case "05":
                monthName = "May";
                break;
            case "06":
                monthName = "June";
                break;
            case "07":
                monthName = "July";
                break;
            case "08":
                monthName = "August";
                break;
            case "09":
                monthName = "September";
                break;
            case "10":
                monthName = "October";
                break;
            case "11":
                monthName = "November";
                break;
            case "12":
                monthName = "December";
                break;
            default:
                monthName = "Invalid month";
                break;
        }
        return monthName;
    }
//    public void sentiment(){
//
//        long t1 = System.nanoTime();
//        gameReviews.createOrReplaceTempView("reviews");
//
//
//        String sqlQuery = "SELECT review, recommendation FROM reviews";
//
//        Dataset<Row> df = sparkSession.sql(sqlQuery);
//
//        df.show(5);
////        Tokenizer tokenizer = new Tokenizer().setInputCol("review").setOutputCol("words");
//
//        RegexTokenizer regexTokenizer = new RegexTokenizer()
//                .setInputCol("review")
//                .setOutputCol("words")
//                .setPattern("\\W");  // alternatively .setPattern("\\w+").setGaps(false);
//
//
////        Dataset<Row> wordsData = regexTokenizer.transform(df);
//
//        HashingTF hashingTF = new HashingTF()
//                .setInputCol("words")
//                .setOutputCol("features");
//
//
//        StringIndexer stringIndexer = new StringIndexer()
//                .setInputCol("recommendation")
//                .setOutputCol("label");
//
//        LinearSVC lsvc = new LinearSVC();
//
//        Pipeline pipeline = new Pipeline()
//                .setStages(new PipelineStage[] {regexTokenizer, hashingTF, stringIndexer, lsvc});
//
//        ParamMap[] paramGrid = new ParamGridBuilder()
//                .addGrid(hashingTF.numFeatures(), new int[] {100, 200})
//                .addGrid(lsvc.fitIntercept())
//                .addGrid(lsvc.regParam(), new double[] {0.01, 0.0001})
//                .addGrid(lsvc.maxIter(), new int[] {10, 15})
//                .addGrid(lsvc.threshold(), new double[] {0.0, 0.25})
//                .addGrid(lsvc.threshold(), new double[] {0.0001, 0.01}) // Note: only the last threshold grid will be used
//                .build();
//
//        // In this case the estimator is simply the linear regression.
//        // A TrainValidationSplit requires an Estimator, a set of Estimator ParamMaps, and an Evaluator.
//        TrainValidationSplit trainValidationSplit = new TrainValidationSplit()
//                .setEstimator(pipeline)
//                .setEvaluator(new BinaryClassificationEvaluator())
//                .setEstimatorParamMaps(paramGrid)
//                .setTrainRatio(0.8)  // 80% for training and the remaining 20% for validation
//                .setParallelism(2);  // Evaluate up to 2 parameter settings in parallel
//
//        Dataset<Row>[] splits = df.randomSplit(new double[] {0.8, 0.2});
//        Dataset<Row> trainingData = splits[0];
//        Dataset<Row> testData = splits[1];
//
//        // Run train validation split, and choose the best set of parameters.
//        TrainValidationSplitModel model = trainValidationSplit.fit(trainingData);
//
//        // Assuming model is defined and has a bestModel method returning a PipelineModel
//        PipelineModel bestModel = (PipelineModel) model.bestModel();
//
//        // Extract `numFeatures` from HashingTF (assumed to be the second stage in the pipeline)
//        HashingTF htf = (HashingTF) bestModel.stages()[1];
//        int numFeatures = htf.getNumFeatures();
//
//        // Extract parameters from LinearSVCModel (assumed to be the fourth stage in the pipeline)
//        LinearSVCModel linearSVCModel = (LinearSVCModel) bestModel.stages()[3];
//        boolean fitIntercept = linearSVCModel.getFitIntercept();
//        double regParam = linearSVCModel.getRegParam();
//        int maxIter = linearSVCModel.getMaxIter();
//        double threshold = linearSVCModel.getThreshold();
//        double tol = linearSVCModel.getTol();
//
//        // Print model parameters
//        System.out.printf("numFeatures: %d%n", numFeatures);
//        System.out.printf("fitIntercept: %b%n", fitIntercept);
//        System.out.printf("regParam: %f%n", regParam);
//        System.out.printf("maxIter: %d%n", maxIter);
//        System.out.printf("threshold: %f%n", threshold);
//        System.out.printf("tol: %f%n", tol);
//
//        Dataset<Row> predictions = model.transform(testData);
//        predictions.select("text", "sentiment", "label", "prediction").show();
//
//        BinaryClassificationEvaluator evaluator = new BinaryClassificationEvaluator()
//                .setLabelCol("label")
//                .setRawPredictionCol("prediction");
//
//        double accuracy = evaluator.evaluate(predictions);
//        System.out.println("Accuracy of the test set is " + accuracy);
//
//        // Measure and print the elapsed time
//        long t2 = System.nanoTime();
//        System.out.printf("Applied sentiment analysis algorithm on input game_reviews in %.2f seconds%n", (t2 - t1) * 1E-9);
//
//    }
}
