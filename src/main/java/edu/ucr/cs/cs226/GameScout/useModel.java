package edu.ucr.cs.cs226.GameScout;

import org.apache.spark.ml.PipelineModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
//import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.ml.tuning.TrainValidationSplitModel;
import org.apache.spark.sql.functions;

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
        data.add(RowFactory.create("The campaign mode was surprisingly deep and enjoyable. Highly recommended.", "Recommended"));
        data.add(RowFactory.create("The game is okay, but nothing special. You might enjoy it on sale.", "Not Recommended"));
        data.add(RowFactory.create("The game crashes all the time. Very disappointing", "Not Recommended"));
//        data.add(RowFactory.create("Never cared much about Warhammer until this game showed me the error of my ways and made me cast away the heresy and fully embrace the Emperor's glorious light. No longer do I wander in the shadow of ignorance, for Space Marine 2 has torn asunder the veil that clouded my mortal sight.With each righteous strike of the chainsword, I felt the will of the Emperor coursing through my veins. The xenos or the heretic—none can stand before the might of the Adeptus Astartes, and now I, too, feel the weight of that sacred duty upon my shoulders. Every battle is a prayer to His undying majesty, every victory a testament to the purity of the Emperor's cause.In this game, I learned what it truly means to be a warrior of the Imperium: to purge the unclean without hesitation, to stand unwavering in the face of annihilation, and to know that failure is not an option, for failure would mean the death of mankind itself. This is no mere entertainment—this is an initiation into the sacred brotherhood of those who fight for the survival of the Imperium.Space Marine 2 is not just a game—it is a revelation, a holy writ in digital form, and a reminder that we must be ever vigilant, ever faithful, and ever ready to cleanse the galaxy of those who would defy the Emperor's will.Praise be to the God-Emperor! Burn the heretic, purge the unclean!", "Recommended"));
//        data.add(RowFactory.create("The game itself is also super fun. The PvP and the campaign are a joy to play. Your actions feel deliberate, almost as if you're controlling an Angel in a 1 Ton metal suit. I love it.-------------------------------It's sad that I have to say this, but it's wonderfully refreshing to be able to boot up a game, play for an hour or two, and close it again. Rather than boot it up, do my dailies, check my weeklies, check my season pass etc.You boot the game up, and you play it. It doesn't try to open your wallet at any point.While this should not be something to be praised in the year 2024, it is refreshing to see.", "Recommended"));
        // Convert the data into a Dataset
        Dataset<Row> newReviews = sparkSession.createDataFrame(data, schema);

        // Display the Dataset
        newReviews.show(false);

        newReviews = newReviews.filter(newReviews.col("review").isNotNull());

        // Apply the model for predictions
        Dataset<Row> predictions = bestPipelineModel.transform(newReviews);

        // Display predictions
        predictions.select("review", "recommendation", "prediction").show(false);

        // Evaluate (if ground truth is available)
//        BinaryClassificationEvaluator evaluator = new BinaryClassificationEvaluator()
//                .setLabelCol("label")
//                .setRawPredictionCol("prediction");
//
//        double accuracy = evaluator.evaluate(predictions);

        // Map "recommendation" to a numerical label
        predictions = predictions.withColumn("label",
                functions.when(functions.col("recommendation").equalTo("Recommended"), 0.0)
                        .otherwise(1.0));

        // Evaluate predictions
        Dataset<Row> correctPredictions = predictions.filter(functions.col("prediction").equalTo(functions.col("label")));
        long totalDataCount = predictions.count();
        long correctCount = correctPredictions.count();

        double accuracy = (double) correctCount / totalDataCount;
        System.out.println("Accuracy on new data: " + accuracy);

        sparkSession.stop();
    }
}
