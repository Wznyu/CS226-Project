package edu.ucr.cs.cs226.GameScout.configuration;

import edu.ucr.cs.cs226.GameScout.integration.SparkService;
import edu.ucr.cs.cs226.GameScout.service.SearchService;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SparkConfig {

    @Bean
    public JavaSparkContext javaSparkContext() {
        SparkConf conf = new SparkConf()
                .setAppName("SpringBootSparkApp")
                .setMaster("local[*]");  // For local development
        return new JavaSparkContext(conf);
    }

    @Bean
    public SparkSession sparkSession(JavaSparkContext javaSparkContext) {
        return SparkSession.builder()
                .sparkContext(javaSparkContext.sc())
                .appName("SpringBootSparkApp")
                .getOrCreate();
    }

    @Bean
    public SparkService sparkService(SparkSession sparkSession) {
        return new SparkService(sparkSession); // If there are dependencies, pass them here
    }

    @Bean
    public SearchService searchService(SparkService sparkService) {
        return new SearchService(sparkService); // If there are dependencies, pass them here
    }
}
