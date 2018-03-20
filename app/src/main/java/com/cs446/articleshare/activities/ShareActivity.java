package com.cs446.articleshare.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cs446.articleshare.App;
import com.cs446.articleshare.R;
import com.cs446.articleshare.Util;
import com.twitter.Validator;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import twitter4j.StatusUpdate;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

public class ShareActivity extends BaseActivity {

    private TwitterSession twitterSession;

    private TwitterLoginButton loginButton;
    private TextView userName;
    private Button tweetButton;
    private Button openTwitterButton;
    private TextView characterCount;
    private EditText tweet;
    private LinearLayout tweetLayout;

    private Bitmap img;
    private String fileName;
    private String tweetText;
    private String selectedUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        twitterSession =
                TwitterCore.getInstance().getSessionManager().getActiveSession();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        tweetButton = (Button) findViewById(R.id.tweet_button);
        openTwitterButton = (Button) findViewById(R.id.open_twitter_button);
        openTwitterButton.setVisibility(View.GONE);

        userName = (TextView) findViewById(R.id.user_name);

        tweetLayout = (LinearLayout) findViewById(R.id.tweet_layout);

        initLoginButton();

        showLoggedOutState();

        initLogoutButton();

        selectedUrl = getIntent().getStringExtra(CustomizeActivity.URL);
        initLinkPreviewView(selectedUrl);

        tweetText = selectedUrl;

        final Validator twitterValidator = new Validator();
        int charLimit = (Validator.MAX_TWEET_LENGTH * 2) - (2 * (twitterValidator.getShortUrlLengthHttps() + 1));
        initCharacterCountView(charLimit);

        initTweetEditor(twitterValidator);

        initPreviewImage();

        boolean imageSaved = saveImage();

        if (!imageSaved) {
            //TODO throw error
            return;
        }

        if (twitterSession != null) {
            showLoggedInState(twitterSession);
        }
    }

    private boolean saveImage() {
        @SuppressLint("SimpleDateFormat") // locale unnecessary
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MMdd_HHmm_ssSS");
        Date now = new Date();
        String strDate = sdf.format(now);
        fileName = strDate + ".png";

        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(fileName, MODE_PRIVATE);
            img.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void initPreviewImage() {
        img = getImagePreview();
        ImageView finalImage = (ImageView) findViewById(R.id.final_image);
        finalImage.setImageBitmap(img);
    }

    private Bitmap getImagePreview() {
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        byte[] byteArray = extras.getByteArray(CustomizeActivity.IMAGE);
        assert byteArray != null;
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    private void initTweetEditor(final Validator twitterValidator) {
        tweet = (EditText) findViewById(R.id.tweet);
        tweet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable editable) {

                tweetText = tweet.getText() + " " + selectedUrl;
                int tweetLength = twitterValidator.getTweetLength(tweet.getText().toString()) + (2 * (twitterValidator.getShortUrlLengthHttps() + 1));
                int charsRemaining = Validator.MAX_TWEET_LENGTH - tweetLength;
                characterCount.setText(Integer.toString(charsRemaining));
                if (charsRemaining >= 0) {
                    tweetButton.setEnabled(true);
                    characterCount.setTextColor(Color.BLACK);
                } else {
                    tweetButton.setEnabled(false);
                    characterCount.setTextColor(Color.RED);
                }
            }
        });
    }

    private void initLogoutButton() {
        TextView logOut = (TextView) findViewById(R.id.logout);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Twitter.logOut();
                showLoggedOutState(view);
            }
        });
    }

    private void initLinkPreviewView(String selectedUrl) {
        final int LINK_PREVIEW_LENGTH = 32;

        String baseUrl = Util.getPrettyUrl(selectedUrl);
        String urlPreview;
        if (baseUrl.length() < LINK_PREVIEW_LENGTH) {
            urlPreview = baseUrl;
        } else {
            urlPreview = baseUrl.substring(0, LINK_PREVIEW_LENGTH) + "...";
        }
        TextView linkPreview = (TextView) findViewById(R.id.link_preview);
        linkPreview.setText(urlPreview);
        linkPreview.setTextColor(getResources().getColor(R.color.tw__blue_default));
    }

    @SuppressLint("SetTextI18n")
    private void initCharacterCountView(int characterLimit) {
        characterCount = (TextView) findViewById(R.id.character_count);
        characterCount.setText(Integer.toString(characterLimit));
        characterCount.setTextColor(Color.BLACK);
    }

    private void showLoggedInState(TwitterSession twitterSession) {
        final String PREFIX = "Post as @";
        loginButton.setVisibility(View.GONE);
        tweetLayout.setVisibility(View.VISIBLE);
        tweetButton.setVisibility(View.VISIBLE);
        userName.setText(String.format("%s%s", PREFIX, twitterSession.getUserName()));
    }

    private void showLoggedOutState() {
        showLoggedOutState(null);
    }

    private void showLoggedOutState(View view) {
        loginButton.setVisibility(View.VISIBLE);
        tweetLayout.setVisibility(View.GONE);
        tweetButton.setVisibility(View.GONE);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void initLoginButton() {
        loginButton = (TwitterLoginButton) findViewById(R.id.twitter_login_button);
        loginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                twitterSession = result.data;
                showLoggedInState(twitterSession);
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(ShareActivity.this, R.string.twitter_app_not_installed_msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        File imageFile = getFileStreamPath(fileName);
        boolean delete = imageFile.delete();
        if (!delete) {
            // TODO show warning ?
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loginButton.onActivityResult(requestCode, resultCode, data);
    }

    public void postTweet(View view) {
        File imageFile = getFileStreamPath(fileName);
        UpdateTwitterStatusTask postTweet = new UpdateTwitterStatusTask(tweetText, imageFile);
        postTweet.execute();
    }

    public void viewTwitter(View view) {
        App.getInstance().openTwitterProfile(twitterSession.getUserName());
    }

    // TODO refactor so that this task is decoupled from ShareActivity
    public class UpdateTwitterStatusTask extends AsyncTask<String, String, Void> {

        String status;
        File image;

        UpdateTwitterStatusTask(String status, File image) {
            this.status = status;
            this.image = image;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            ProgressDialog pDialog = new ProgressDialog(ShareActivity.this);
            pDialog.setMessage(getString(R.string.posting_to_twitter));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            displayDialog(pDialog);
        }

        protected Void doInBackground(String... args) {

            try {
                ConfigurationBuilder builder = new ConfigurationBuilder();
                builder.setOAuthConsumerKey(App.TWITTER_KEY);
                builder.setOAuthConsumerSecret(App.TWITTER_SECRET);

                // Access Token
                String access_token = twitterSession.getAuthToken().token;
                // Access Token Secret
                String access_token_secret = twitterSession.getAuthToken().secret;

                AccessToken accessToken = new AccessToken(access_token, access_token_secret);
                twitter4j.Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

                // Update status
                StatusUpdate statusUpdate = new StatusUpdate(status);
                statusUpdate.setMedia(image);

                twitter4j.Status response = twitter.updateStatus(statusUpdate);

                Log.d("Status", response.getText());

            } catch (twitter4j.TwitterException e) {
                Toast.makeText(ShareActivity.this, R.string.failed_to_tweet, Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            /* Dismiss the progress dialog after sharing */
            closeDialog();
            Toast.makeText(ShareActivity.this, getString(R.string.posted_to_twitter), Toast.LENGTH_SHORT).show();
            tweet.setEnabled(false);
            tweetButton.setVisibility(View.GONE);
            openTwitterButton.setVisibility(View.VISIBLE);
        }

    }
}
