package com.cs446.articleshare.tasks;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WebPages {

    @SerializedName("webSearchUrl")
    @Expose
    private String webSearchUrl;
    @SerializedName("totalEstimatedMatches")
    @Expose
    private Integer totalEstimatedMatches;
    @SerializedName("value")
    @Expose
    private List<Value> value = null;
    @SerializedName("someResultsRemoved")
    @Expose
    private Boolean someResultsRemoved;

    public String getWebSearchUrl() {
        return webSearchUrl;
    }

    public void setWebSearchUrl(String webSearchUrl) {
        this.webSearchUrl = webSearchUrl;
    }

    public Integer getTotalEstimatedMatches() {
        return totalEstimatedMatches;
    }

    public void setTotalEstimatedMatches(Integer totalEstimatedMatches) {
        this.totalEstimatedMatches = totalEstimatedMatches;
    }

    public List<Value> getValue() {
        return value;
    }

    public void setValue(List<Value> value) {
        this.value = value;
    }

    public Boolean getSomeResultsRemoved() {
        return someResultsRemoved;
    }

    public void setSomeResultsRemoved(Boolean someResultsRemoved) {
        this.someResultsRemoved = someResultsRemoved;
    }

}