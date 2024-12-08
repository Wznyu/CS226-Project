package edu.ucr.cs.cs226.GameScout.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Game {
    private String number_of_reviews_from_purchased_people;
    private String overall_player_rating;
    private String publisher;
    private String developer;
    private String genres;
    private String name;
    private String short_description;
    private String release_date;
    private String number_of_english_reviews;
    private String link;
    private String imgSrc;


    public String getNumber_of_reviews_from_purchased_people() {
        return number_of_reviews_from_purchased_people;
    }

    public void setNumber_of_reviews_from_purchased_people(String number_of_reviews_from_purchased_people) {
        this.number_of_reviews_from_purchased_people = number_of_reviews_from_purchased_people;
    }

    public String getOverall_player_rating() {
        return overall_player_rating;
    }

    public void setOverall_player_rating(String overall_player_rating) {
        this.overall_player_rating = overall_player_rating;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShort_description() {
        return short_description;
    }

    public void setShort_description(String short_description) {
        this.short_description = short_description;
    }


    public String getRelease_date() {
        return release_date;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public String getNumber_of_english_reviews() {
        return number_of_english_reviews;
    }

    public void setNumber_of_english_reviews(String number_of_english_reviews) {
        this.number_of_english_reviews = number_of_english_reviews;
    }

    public void setImgSrc() {
        String link = getLink();

        Pattern pattern = Pattern.compile("/app/(\\d+)/");
        Matcher matcher = pattern.matcher(link);

        if (matcher.find()) {
            String id = matcher.group(1);
            this.imgSrc = String.format(
                    "https://shared.fastly.steamstatic.com/store_item_assets/steam/apps/%s/header.jpg",
                    id);
        } else {
            this.imgSrc = "https://upload.wikimedia.org/wikipedia/commons/a/a3/Image-not-found.png";
        }
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
        setImgSrc();
    }
}
