package edu.ucr.cs.cs226.GameScout.controller;

import edu.ucr.cs.cs226.GameScout.service.SentimentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
public class SentimentController {
    private final SentimentService sentimentService;


    @Autowired
    public SentimentController(SentimentService sentimentService) {
        this.sentimentService = sentimentService;
    }


    @GetMapping("/Sentiment")
    public String sentiment(@RequestParam String review, @RequestParam String recommendation) {
        return sentimentService.sentiment(review, recommendation);
    }

}







