package edu.ucr.cs.cs226.GameScout;

import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.PipelineStage;
import org.apache.spark.ml.classification.LinearSVC;
import org.apache.spark.ml.classification.LinearSVCModel;
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator;
import org.apache.spark.ml.feature.HashingTF;
import org.apache.spark.ml.feature.RegexTokenizer;
import org.apache.spark.ml.feature.StringIndexer;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.tuning.ParamGridBuilder;
import org.apache.spark.ml.tuning.TrainValidationSplit;
import org.apache.spark.ml.tuning.TrainValidationSplitModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;



public class test {
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
        long t1 = System.nanoTime();

        try {
            gameReviews.createOrReplaceTempView("reviews");


            String sqlQuery = "SELECT review, recommendation FROM reviews";

//            Dataset<Row> df = sparkSession.sql(sqlQuery);
            Dataset<Row> df = sparkSession.sql(sqlQuery).limit(50000);  // Limit rows to 1000 for testing

            df = df.filter(df.col("review").isNotNull());  // Or use .coalesce() if necessary



            df.show(5);
            //        Tokenizer tokenizer = new Tokenizer().setInputCol("review").setOutputCol("words");

            RegexTokenizer regexTokenizer = new RegexTokenizer()
                    .setInputCol("review")
                    .setOutputCol("words")
                    .setPattern("\\W");  // alternatively .setPattern("\\w+").setGaps(false);


            //        Dataset<Row> wordsData = regexTokenizer.transform(df);

            HashingTF hashingTF = new HashingTF()
                    .setInputCol("words")
                    .setOutputCol("features");


            StringIndexer stringIndexer = new StringIndexer()
                    .setInputCol("recommendation")
                    .setOutputCol("label");

            Dataset<Row> tokenizedData = regexTokenizer.transform(df);
            tokenizedData.show(5);

            Dataset<Row> featuredData = hashingTF.transform(tokenizedData);
            featuredData.show(5);

            Dataset<Row> indexedData = stringIndexer.fit(featuredData).transform(featuredData);
            indexedData.show(5);


            LinearSVC lsvc = new LinearSVC();

            Pipeline pipeline = new Pipeline()
                    .setStages(new PipelineStage[] {regexTokenizer, hashingTF, stringIndexer, lsvc});

            ParamMap[] paramGrid = new ParamGridBuilder()
                    .addGrid(hashingTF.numFeatures(), new int[] {100, 200})
                    .addGrid(lsvc.fitIntercept())
                    .addGrid(lsvc.regParam(), new double[] {0.01, 0.0001})
                    .addGrid(lsvc.maxIter(), new int[] {10, 15})
                    .addGrid(lsvc.threshold(), new double[] {0.0, 0.25})
                    .addGrid(lsvc.threshold(), new double[] {0.0001, 0.01}) // Note: only the last threshold grid will be used
                    .build();

            // In this case the estimator is simply the linear regression.
            // A TrainValidationSplit requires an Estimator, a set of Estimator ParamMaps, and an Evaluator.
            TrainValidationSplit trainValidationSplit = new TrainValidationSplit()
                    .setEstimator(pipeline)
                    .setEvaluator(new BinaryClassificationEvaluator())
                    .setEstimatorParamMaps(paramGrid)
                    .setTrainRatio(0.8)  // 80% for training and the remaining 20% for validation
                    .setParallelism(2);  // Evaluate up to 2 parameter settings in parallel

            Dataset<Row>[] splits = df.randomSplit(new double[] {0.8, 0.2});
            Dataset<Row> trainingData = splits[0];
            Dataset<Row> testData = splits[1];

            System.out.println("Training data count: " + trainingData.count());
            System.out.println("Test data count: " + testData.count());

            // Run train validation split, and choose the best set of parameters.
            TrainValidationSplitModel model = trainValidationSplit.fit(trainingData);

            // Assuming model is defined and has a bestModel method returning a PipelineModel
            PipelineModel bestModel = (PipelineModel) model.bestModel();

            // Extract `numFeatures` from HashingTF (assumed to be the second stage in the pipeline)
            HashingTF htf = (HashingTF) bestModel.stages()[1];
            int numFeatures = htf.getNumFeatures();

            // Extract parameters from LinearSVCModel (assumed to be the fourth stage in the pipeline)
            LinearSVCModel linearSVCModel = (LinearSVCModel) bestModel.stages()[3];
            boolean fitIntercept = linearSVCModel.getFitIntercept();
            double regParam = linearSVCModel.getRegParam();
            int maxIter = linearSVCModel.getMaxIter();
            double threshold = linearSVCModel.getThreshold();
            double tol = linearSVCModel.getTol();

            // Print model parameters
            System.out.printf("numFeatures: %d%n", numFeatures);
            System.out.printf("fitIntercept: %b%n", fitIntercept);
            System.out.printf("regParam: %f%n", regParam);
            System.out.printf("maxIter: %d%n", maxIter);
            System.out.printf("threshold: %f%n", threshold);
            System.out.printf("tol: %f%n", tol);

            Dataset<Row> predictions = model.transform(testData);

            // test
            predictions.printSchema();
            predictions.show(5, false);

            predictions.select("review", "recommendation", "label", "prediction").show();

            BinaryClassificationEvaluator evaluator = new BinaryClassificationEvaluator()
                    .setLabelCol("label")
                    .setRawPredictionCol("prediction");

            double accuracy = evaluator.evaluate(predictions);
            System.out.println("Accuracy of the test set is " + accuracy);

            // Measure and print the elapsed time
            long t2 = System.nanoTime();
            System.out.printf("Applied sentiment analysis algorithm on input game_reviews in %.2f seconds%n", (t2 - t1) * 1E-9);

        } catch (Exception e) {
            System.err.println("Error during sentiment analysis: " + e.getMessage());
        } finally {
            sparkSession.stop();
        }
    }
}

