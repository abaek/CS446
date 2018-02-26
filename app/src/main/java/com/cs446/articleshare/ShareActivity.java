package com.cs446.articleshare;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;

public class ShareActivity extends AppCompatActivity {

    private EditText tweet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        tweet = (EditText) findViewById(R.id.tweet);

        initPreviewImage();
    }

    private void initPreviewImage() {
        Uri uri = Uri.parse(getIntent().getExtras().getString(InputActivity.IMAGE));

        try {
            Bitmap img = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            if (img == null) {
                return;
            }
            ImageView finalImage = (ImageView) findViewById(R.id.final_image);
            finalImage.setImageBitmap(img);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void postTweet(View view) {
        Toast.makeText(this, tweet.getText(), Toast.LENGTH_SHORT).show();
    }

}
