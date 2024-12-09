package edu.ucr.cs.cs226.GameScout.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
        return sparkService.findGameByKeywords(keyword);
    }

    public List<Map<String, Object>> getRanking(String genre, String type){
        return sparkService.getRanking(genre, type);
    }

    public List<Map<String, Object>> getPopular(String start1, String end1){

        try {

            // Parse the string to a Date object
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-mm-dd");
            Date start = inputFormat.parse(start1);
            Date end = inputFormat.parse(end1);


//            System.out.println(start);
//            System.out.println(end);
//            System.out.println("==========");
            // Format the Date object to the desired format "MMMM dd, yyyy"
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM d, yyyy");
            String formattedDate1 = outputFormat.format(start);
            String formattedDate2 = outputFormat.format(end);
//            System.out.println(formattedDate1);
//            System.out.println(formattedDate2);
            // Output the formatted date

            return sparkService.getPopular(formattedDate1, formattedDate2);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sparkService.getPopular(start1, end1);
    }
}
