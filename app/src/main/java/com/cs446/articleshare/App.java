package com.cs446.articleshare;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import io.fabric.sdk.android.Fabric;

public class App extends Application {
    public static final String TWITTER_KEY = BuildConfig.TWITTER_KEY;
    public static final String TWITTER_SECRET = BuildConfig.TWITTER_SECRET_KEY;

    private static App singleton;
    private TwitterAuthConfig authConfig;

    public static App getInstance() {
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
    }

    public void openTwitterProfile(String username) {
        try {
            pushToTwitterAppProfile(username);
        } catch (Exception e) {
            pushToTwitterWebProfile(username);
        }
    }

    private void pushToTwitterWebProfile(String username) {
        Intent intent;
        intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://twitter.com/" + username)
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void pushToTwitterAppProfile(String username) throws PackageManager.NameNotFoundException {
        Intent intent;
        getPackageManager().getPackageInfo("com.twitter.android", 0);
        intent = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("twitter://user?screen_name=" + username)
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
