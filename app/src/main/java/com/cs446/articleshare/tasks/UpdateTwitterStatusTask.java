package com.cs446.articleshare.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.cs446.articleshare.App;
import com.twitter.sdk.android.core.TwitterSession;

import java.io.File;

import twitter4j.StatusUpdate;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

public class UpdateTwitterStatusTask extends AsyncTask<String, String, Void> {

    String status;
    File image;
    Callback callback;
    twitter4j.TwitterException error;
    TwitterSession session;

    public UpdateTwitterStatusTask(TwitterSession session, String status, File image, Callback callback) {
        this.status = status;
        this.image = image;
        this.callback = callback;
        this.session = session;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        callback.onStart();
    }

    protected Void doInBackground(String... args) {

        try {
            ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(App.TWITTER_KEY);
            builder.setOAuthConsumerSecret(App.TWITTER_SECRET);

            // Access Token
            String access_token = session.getAuthToken().token;
            // Access Token Secret
            String access_token_secret = session.getAuthToken().secret;

            AccessToken accessToken = new AccessToken(access_token, access_token_secret);
            twitter4j.Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

            // Update status
            StatusUpdate statusUpdate = new StatusUpdate(status);
            statusUpdate.setMedia(image);

            twitter4j.Status response = twitter.updateStatus(statusUpdate);

            Log.d("Status", response.getText());

        } catch (twitter4j.TwitterException e) {
            error = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        callback.onComplete(error);
    }

    public interface Callback {
        void onComplete(twitter4j.TwitterException error);
        void onStart();
    }
}
