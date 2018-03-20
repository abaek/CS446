package com.cs446.articleshare.dataStructures;

public class Source {
    private String title;
    private String url;
    private String displayUrl;
    public Source(String title, String displayUrl, String url) {
        this.title = title;
        this.url = url;
        this.displayUrl = displayUrl;
    }

    public String getTitle() { return title; }
    public String getUrl() { return url; }
    public String getDisplayUrl() {return displayUrl; }
}
