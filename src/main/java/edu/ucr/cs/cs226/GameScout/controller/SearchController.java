package edu.ucr.cs.cs226.GameScout.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ucr.cs.cs226.GameScout.model.Game;
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

    @GetMapping("/search")
    public List<Game> findGamesByGet(@RequestParam String keyword) {
        Map<String, String> t = new HashMap<String, String>();
        t.put("keyword",keyword);
        return searchService.searchGame(keyword);
    }


    @GetMapping("/ranking")
    public List<Map<String, Object>> rankingByGet(@RequestParam String genre, @RequestParam String type) {
        return searchService.getRanking(genre, type);
    }

    @GetMapping("/popular")
    public List<Map<String, Object>> popularByGet(@RequestParam String start, @RequestParam String end) {
        return searchService.getPopular(start, end);
    }


}
