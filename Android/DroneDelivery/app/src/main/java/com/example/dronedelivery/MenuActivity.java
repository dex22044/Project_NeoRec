package com.example.dronedelivery;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = getSharedPreferences("dd_prefs", 0);
        if(!sp.contains("email") || !sp.contains("password")) {
            Intent myIntent = new Intent(this, MainActivity.class);
            startActivity(myIntent);
            finish();
        }
        setContentView(R.layout.activity_menu);
        ((TextView)findViewById(R.id.loginText)).setText("Logged in as " + sp.getString("email", "???"));

        findViewById(R.id.exitButton).setOnClickListener(exitOnClickListener);
        findViewById(R.id.sendOrderButton).setOnClickListener(sendOnClickListener);
        findViewById(R.id.trackOrderButton).setOnClickListener(trackOnClickListener);
        findViewById(R.id.profileButton).setOnClickListener(profileOnClickListener);
    }

    private View.OnClickListener exitOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            exitButtonClick();
        }
    };

    private View.OnClickListener profileOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            profileButtonClick();
        }
    };

    private View.OnClickListener sendOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendButtonClick();
        }
    };

    private View.OnClickListener trackOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            trackButtonClick();
        }
    };

    void exitButtonClick() {
        System.out.println("FUCK");
        SharedPreferences sp = getSharedPreferences("dd_prefs", 0);
        SharedPreferences.Editor ed = sp.edit();
        if(sp.contains("email")) ed.remove("email");
        if(sp.contains("password")) ed.remove("password");
        ed.commit();

        Intent myIntent = new Intent(this, MainActivity.class);
        startActivity(myIntent);
        finish();
    }

    void profileButtonClick() {
        Intent myIntent = new Intent(this, EditProfileActivity.class);
        startActivity(myIntent);
    }

    void sendButtonClick() {
        Intent myIntent = new Intent(this, SendActivity.class);
        startActivity(myIntent);
    }

    void trackButtonClick() {
        Intent myIntent = new Intent(this, DeliveryStatusActivity.class);
        startActivity(myIntent);
    }
}