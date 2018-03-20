package com.cs446.articleshare.dataStructures;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Value {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("isFamilyFriendly")
    @Expose
    private Boolean isFamilyFriendly;
    @SerializedName("displayUrl")
    @Expose
    private String displayUrl;
    @SerializedName("snippet")
    @Expose
    private String snippet;
    @SerializedName("dateLastCrawled")
    @Expose
    private String dateLastCrawled;
    @SerializedName("deepLinks")
    @Expose
    private List<DeepLink> deepLinks = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getIsFamilyFriendly() {
        return isFamilyFriendly;
    }

    public void setIsFamilyFriendly(Boolean isFamilyFriendly) {
        this.isFamilyFriendly = isFamilyFriendly;
    }

    public String getDisplayUrl() {
        return displayUrl;
    }

    public void setDisplayUrl(String displayUrl) {
        this.displayUrl = displayUrl;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getDateLastCrawled() {
        return dateLastCrawled;
    }

    public void setDateLastCrawled(String dateLastCrawled) {
        this.dateLastCrawled = dateLastCrawled;
    }

    public List<DeepLink> getDeepLinks() {
        return deepLinks;
    }

    public void setDeepLinks(List<DeepLink> deepLinks) {
        this.deepLinks = deepLinks;
    }

}
