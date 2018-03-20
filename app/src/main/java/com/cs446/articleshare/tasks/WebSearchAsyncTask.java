package com.cs446.articleshare.tasks;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.cs446.articleshare.App;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

/**
 * Performs a web search on text
 */
public class WebSearchAsyncTask extends AsyncTask<Void, Void, Void> {
    private final static int MAX_QUERY_LENGTH = 2048; // TODO use this constant to limit URL length
    private final static String HOST = "https://api.cognitive.microsoft.com";
    private final static String PATH = "/bing/v7.0/search";

    private String mSearchStr;
    private int mNumOfResults = 0;
    private WebSearchAsyncTaskCallback mCallback;
    private BingSearchResults mBingSearchResults;
    private Error mError;

    public WebSearchAsyncTask(String searchStr, int numOfResults, WebSearchAsyncTaskCallback callback) {
        mSearchStr = searchStr;
        mNumOfResults = numOfResults;
        mCallback = callback;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            String finalQuery = max32Query(mSearchStr);
            String searchStr = URLEncoder.encode(finalQuery, "UTF-8");
            String numOfResultsStr = mNumOfResults <= 0 ? "" : "&count=" + mNumOfResults;

            URL url = new URL(HOST + PATH + "?q=" +  searchStr + numOfResultsStr);
            HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
            connection.setRequestProperty("Ocp-Apim-Subscription-Key", App.BING_KEY_1);

            // receive JSON body
            InputStream stream = connection.getInputStream();
            String response = new Scanner(stream).useDelimiter("\\A").next();

            // construct result object for return
            BingSearchResults results = new BingSearchResults(new HashMap<String, String>(), response);

            // extract Bing-related HTTP headers
            Map<String, List<String>> headers = connection.getHeaderFields();
            for (String header : headers.keySet()) {
                if (header == null) continue;      // may have null key
                if (header.startsWith("BingAPIs-") || header.startsWith("X-MSEdge-")) {
                    results.relevantHeaders.put(header, headers.get(header).get(0));
                }
            }

            stream.close();

            mBingSearchResults = results;
        } catch (Exception e) {
            e.printStackTrace();
            mError = new Error(e.getMessage(), e);
        }

        return null;
    }

    private String max32Query(String searchStr) {
        /*
        Get the middle 32 words of the query.
        Inspired by Google's 32 word limit for searches.
        The middle of a query is most likely to be "good" data.
         */

        // remove quotation marks and extra whitespace
        searchStr = searchStr.replaceAll("\"", "").replaceAll("\\s+", " ");

        String[] words = searchStr.split("\\s+");
        if(words.length <= 32) {
            // verbatim search entire query
            return "\"" + searchStr +  "\"";
        } else {
            int toChop = (words.length - 32) / 2; // integer division will make stuff off by one maybe, no big deal
            String[] wordsToSearch = Arrays.copyOfRange(words, toChop, words.length - toChop);
            return TextUtils.join(" ", wordsToSearch);
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        if (mCallback != null) {
            mCallback.onComplete(mBingSearchResults, mError);
        }

    }

    public interface WebSearchAsyncTaskCallback {
        void onComplete(Object o, Error error);
    }
}