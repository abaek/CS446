package com.cs446.articleshare.fragments;

public class Source {
    String title;
    String url;
    String displayUrl;
    public Source(String title, String displayUrl, String url) {
        this.title = title;
        this.url = url;
        this.displayUrl = displayUrl;
    }

    public String getTitle() { return title; }
    public String getUrl() { return url; }
    public String getDisplayUrl() {return displayUrl; }
}
