package edu.ucr.cs.cs226.GameScout.controller;

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
        return searchService.searchGame(keyword);
    }

    // Handles POST requests to /api/games/search
    @PostMapping("/search")
    public List<Game> findGamesByPost(@RequestBody Map<String, String> body) {
        String keyword = body.get("keyword");
        return searchService.searchGame(keyword);
    }
}