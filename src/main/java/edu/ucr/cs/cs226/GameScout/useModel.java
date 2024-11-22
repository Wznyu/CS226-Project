package edu.ucr.cs.cs226.GameScout;

import org.apache.spark.ml.PipelineModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.ml.tuning.TrainValidationSplitModel;

import java.util.ArrayList;
import java.util.List;

public class useModel {
    public static void main(String[] args) {
        SparkSession sparkSession = SparkSession.builder()
                .appName("GameReviewSentimentAnalysis")
                .master("local[*]")
                .config("spark.driver.memory", "4g")
                .getOrCreate();
        // Set the logging level
        sparkSession.sparkContext().setLogLevel("ERROR");

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
        data.add(RowFactory.create("The game is incredible! The graphics and gameplay are amazing. Definitely worth the money.", "Recommended"));
        data.add(RowFactory.create("I didn’t enjoy the controls, and the gameplay felt repetitive. Not worth my time.", "Not Recommended"));
        data.add(RowFactory.create("This is one of the best games I’ve ever played! Engaging story and fun combat mechanics.", "Recommended"));
        data.add(RowFactory.create("The updates keep breaking the game, and the developers don’t seem to care. Avoid this one.", "Not Recommended"));
        data.add(RowFactory.create("A decent game with a few bugs here and there, but overall a fun experience.", "Recommended"));
        data.add(RowFactory.create("The game crashes all the time, and there’s no support from the developers. Very disappointing.", "Not Recommended"));
        data.add(RowFactory.create("Fantastic game! Multiplayer mode is a blast and keeps me coming back every day.", "Recommended"));
        data.add(RowFactory.create("Too many microtransactions ruin the experience. It feels like a cash grab.", "Not Recommended"));
        data.add(RowFactory.create("The campaign mode was surprisingly deep and enjoyable. Highly recommended.", "Recommended"));
        data.add(RowFactory.create("The game is okay, but nothing special. You might enjoy it on sale.", "Not Recommended"));

        // Convert the data into a Dataset
        Dataset<Row> newReviews = sparkSession.createDataFrame(data, schema);

        // Display the Dataset
        newReviews.show(false);

        newReviews = newReviews.filter(newReviews.col("review").isNotNull());

        // Apply the model for predictions
        Dataset<Row> predictions = bestPipelineModel.transform(newReviews);

        // Display predictions
        predictions.select("review", "recommendation", "prediction").show();

        // Evaluate (if ground truth is available)
        BinaryClassificationEvaluator evaluator = new BinaryClassificationEvaluator()
                .setLabelCol("label")
                .setRawPredictionCol("prediction");

        double accuracy = evaluator.evaluate(predictions);
        System.out.println("Accuracy on new data: " + accuracy);

        sparkSession.stop();
    }
}
