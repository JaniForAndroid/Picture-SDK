package com.example.picsdk.model;


import com.google.gson.JsonObject;

public class PictureBook {
    public int index;
    public int milesson_item_id;
    public String thumb_url;
    public JsonObject action;
    public String max_star;
    public String star;
    public String chinese_name;
    public String text;
    public String desc;
    public String last_dubbing;
    public long time;
    public int id;

    public static class Introduction {

        public String learning_topic;
        public String story_introduction;
    }
}
