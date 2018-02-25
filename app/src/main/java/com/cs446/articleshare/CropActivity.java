package com.cs446.articleshare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class CropActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
    }

    public void previousActivity(View view) {
        Intent intent = new Intent(this, InputActivity.class);
        startActivity(intent);
    }
}
