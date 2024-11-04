package edu.ucr.cs.cs226.GameScout.service;

import java.util.List;

import edu.ucr.cs.cs226.GameScout.integration.SparkService;
import org.jvnet.hk2.annotations.Service;
import edu.ucr.cs.cs226.GameScout.model.Game;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;  // Make sure to import the correct Service

@Service
public class SearchService {
    private final SparkService sparkService;

    @Autowired
    public SearchService(SparkService sparkService){
        this.sparkService = sparkService;
    }

    public List<Game> searchGame(String keyword){
        return sparkService.findGamebyKeywords(keyword);
    }
}
