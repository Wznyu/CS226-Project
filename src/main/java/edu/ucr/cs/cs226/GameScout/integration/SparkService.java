package edu.ucr.cs.cs226.GameScout.integration;

import edu.ucr.cs.cs226.GameScout.model.Game;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import static org.apache.spark.sql.functions.col;
import org.apache.spark.sql.SparkSession;
import org.jvnet.hk2.annotations.Service;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SparkService {
    private final SparkSession sparkSession;
    private Dataset<Row> gameDescriptions;
//    @Autowired
//    private JavaSparkContext sparkContext;

    @Autowired
    public SparkService(SparkSession sparkSession) {
        this.sparkSession = sparkSession;
        loadData();
    }

    private void loadData() {
        gameDescriptions = sparkSession.read()
                .option("header","true")
                .csv("src/main/resources/data/games_description.csv");
    }

    public List<Game> findGamebyKeywords(String keyword) {
        Dataset<Row> filtered = gameDescriptions.filter(col("long_description").contains(keyword));
        return filtered.as(Encoders.bean(Game.class)).collectAsList();
    }
}
