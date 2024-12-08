package edu.ucr.cs.cs226.GameScout.controller;

import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ucr.cs.cs226.GameScout.model.Game;
import edu.ucr.cs.cs226.GameScout.model.SearchRequest;
import edu.ucr.cs.cs226.GameScout.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/games")
public class SearchController {

    private final SearchService searchService;

    @Autowired
    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

//    @PostMapping("/search")
//    public List<Game> findGamesByPost(@RequestBody SearchRequest searchRequest){
//        return searchService.searchGame(searchRequest.getKeyword());
//    }


    @GetMapping("/search")
    public List<Game> findGamesByGet(@RequestParam String keyword) {
        Map<String, String> t = new HashMap<String, String>();
        t.put("keyword",keyword);
        return searchService.searchGame(keyword);
    }

    // Handles POST requests to /api/games/search
    @PostMapping("/search")
    public List<Game> findGamesByPost(@RequestBody Map<String, String> body) {

        return searchService.searchGame(body.get("keyword"));
    }


    @GetMapping("/ranking")
    public List<Map<String, Object>> rankingByGet(@RequestParam String genre, @RequestParam String type) {
        return searchService.getRanking(genre, type);
    }

    @PostMapping("/ranking")
    public List<Map<String, Object>> getRankingByPost(@RequestBody Map<String, String> body) {
        String genre = body.get("genre");
        String type = body.get("type");
        return searchService.getRanking(genre, type);
    }


}
