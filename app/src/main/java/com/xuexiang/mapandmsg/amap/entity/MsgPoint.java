package com.xuexiang.mapandmsg.amap.entity;

public class MsgPoint {
    private String title;
    private String snippet;
    private double latitude;
    private double longitude;

    public MsgPoint() {

    }

    public MsgPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = "title";
        this.snippet = "snippet";
    }

    public MsgPoint(String title, String snippet, double latitude, double longitude) {
        this.title = title;
        this.snippet = snippet;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
