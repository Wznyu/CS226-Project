package edu.ucr.cs.cs226.GameScout.model;

public class Game {
    private String name;
    private String short_description;
    private String release_date;
    private String number_of_english_reviews;
    private String link;

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

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public String getNumber_of_english_reviews() {
        return number_of_english_reviews;
    }

    public void setNumber_of_english_reviews(String number_of_english_reviews) {
        this.number_of_english_reviews = number_of_english_reviews;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
