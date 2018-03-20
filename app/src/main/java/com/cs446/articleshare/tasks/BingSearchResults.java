package com.cs446.articleshare.tasks;

import java.util.HashMap;

public class BingSearchResults{
    HashMap<String, String> relevantHeaders;
    private String jsonResponse;

    public String getJson() {
        return jsonResponse;
    }

    BingSearchResults(HashMap<String, String> headers, String json) {
        relevantHeaders = headers;
        jsonResponse = json;
    }
}