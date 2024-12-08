package edu.ucr.cs.cs226.GameScout.controller;

import edu.ucr.cs.cs226.GameScout.service.SentimentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/games")
public class SentimentController {
    private final SentimentService sentimentService;


    @Autowired
    public SentimentController(SentimentService sentimentService) {
        this.sentimentService = sentimentService;
    }


    @GetMapping("/sentiment")
    public Map<String, String> sentiment(@RequestParam String review, @RequestParam String recommendation) {
        Map<String, String> response = new HashMap<>();
        response.put("recommendation", sentimentService.sentiment(review, recommendation));
        return response;
    }

}







