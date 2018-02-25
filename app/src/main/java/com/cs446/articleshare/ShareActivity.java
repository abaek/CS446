package com.cs446.articleshare;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ShareActivity extends AppCompatActivity {

    private EditText tweet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        tweet = (EditText) findViewById(R.id.tweet);
    }

    public void postTweet(View view) {
        Toast.makeText(this, tweet.getText(), Toast.LENGTH_LONG).show();
    }

}
