package edu.ucr.cs.cs226.GameScout.service;

import edu.ucr.cs.cs226.GameScout.integration.SparkService;
import edu.ucr.cs.cs226.GameScout.model.Game;
import org.jvnet.hk2.annotations.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
@Service
public class SentimentService {
    private final SparkService sparkService;

    @Autowired
    public SentimentService(SparkService sparkService){
        this.sparkService = sparkService;
    }

    public String sentiment(String review, String recommendation){
        return sparkService.sentiment(review, recommendation);
    }

}
