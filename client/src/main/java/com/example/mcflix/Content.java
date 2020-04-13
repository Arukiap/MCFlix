package com.example.mcflix;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*
 * Defines the content that is shown to the user such as videos, movies or streams
 */

public class Content implements Serializable {
    private String title, description;
    private int id,type;
    private List<String> categories, urls;

    public static int VIDEO_TYPE = 1;
    public static int STREAM_TYPE = 2;

    public Content(){

    }

    public Content(int id,String title, int type,String description, List<String> categories, List<String> urls){
        this.id = id;
        this.title = title;
        this.type = type;
        this.description = description;
        this.categories = categories;
        this.urls = urls;
    }

    public Content(String title,int type){
        this.id = 0;
        this.title = title;
        this.type = type;
        this.description = "";
        this.categories = new ArrayList<>();
        this.urls = new ArrayList<>();
    }

    public int getId() { return this.id; }

    public String getTitle(){
        return this.title;
    }

    public int getType() {return this.type;}

    public String getDescription(){
        return this.description;
    }

    public List<String> getCategories() { return this.categories;}

    public List<String> getUrls() { return this.urls; }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
}
