package edu.ucr.cs.cs226.GameScout;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
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
}
