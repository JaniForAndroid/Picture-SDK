package com.example.picsdk.model;

import com.google.gson.JsonArray;

import java.util.List;

public class MyStore extends BaseObject {
    public Data data;

    public static class Data {
        public List<DataList> data_list;
    }

    public static class DataList {
        public String category;
        public Section section;
        public JsonArray list;
        public String id;
        public UserLoginInfo user_info;
    }
}
