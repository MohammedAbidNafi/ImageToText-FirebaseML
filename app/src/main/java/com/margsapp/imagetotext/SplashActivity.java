package com.margsapp.imagetotext;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {

            Intent i = new Intent(SplashActivity.this, MainActivity.class);

            startActivity(i);
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            // close this activity
            finish();

        }, 2*1000);
    }
}