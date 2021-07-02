package com.example.picsdk.model;

public class ShareUserReport extends BaseObject {
    public Data data;

    public static class Data {
        public UserReport user_report_data;
        public int first_win_points;
    }
}
