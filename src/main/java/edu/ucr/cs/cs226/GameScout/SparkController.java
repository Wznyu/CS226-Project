package edu.ucr.cs.cs226.GameScout;


import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
class SparkController {

    @Autowired
    private JavaSparkContext sparkContext;

    @GetMapping("/testSparkConnection")
    public String testSparkConnection() {
        // Sample data
        List<String> data = Arrays.asList("Spark", "Spring", "Boot", "Integration");
        // Creating an RDD from the data
        JavaRDD<String> rdd = sparkContext.parallelize(data);

        // Performing a simple operation
        long count = rdd.count();  // This should trigger Spark actions

        // Returning the result to confirm the connection
        return "Spark is working! Total elements in RDD: " + count;
    }
}