package com.cs446.articleshare.dataStructures;

import java.util.HashMap;

public class BingSearchResults{
    HashMap<String, String> relevantHeaders;
    private String jsonResponse;

    public String getJson() {
        return jsonResponse;
    }

    public HashMap<String, String> getRelevantHeaders() {
        return relevantHeaders;
    }

    public BingSearchResults(HashMap<String, String> headers, String json) {
        relevantHeaders = headers;
        jsonResponse = json;
    }
}