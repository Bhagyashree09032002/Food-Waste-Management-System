package com.example.ne.foodneed;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();

        new CountDownTimer(7000, 500) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                Intent i = new Intent(Splash.this, Login.class);
                startActivity(i);
                finish();
            }

        }.start();
    }
}
