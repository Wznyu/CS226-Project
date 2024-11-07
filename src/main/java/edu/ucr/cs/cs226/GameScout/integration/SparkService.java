package edu.ucr.cs.cs226.GameScout.integration;

import edu.ucr.cs.cs226.GameScout.model.Game;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import static org.apache.spark.sql.functions.col;
import org.apache.spark.sql.SparkSession;
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
    }

    public List<Game> findGameByKeywords(String keyword) {
        Dataset<Row> filtered = gameDescriptions.filter(col("short_description").contains(keyword));
        return filtered.as(Encoders.bean(Game.class)).collectAsList();
    }

    public List<Map<String, Object>> getRanking(String genre, String type) {
        
        gameRanking.createOrReplaceTempView("ranking");


        String sqlQuery = String.format(
                "SELECT * FROM ranking WHERE rank BETWEEN 1 AND 10 AND genre = '%s' AND rank_type = '%s'",
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
}
