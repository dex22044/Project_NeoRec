package com.example.dronedelivery;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = getSharedPreferences("dd_prefs", 0);
        if(sp.contains("email") && sp.contains("password")) {
            Intent myIntent = new Intent(this, MenuActivity.class);
            startActivity(myIntent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        findViewById(R.id.registerButton).setOnClickListener(registerOnClickListener);
        findViewById(R.id.loginButton_login).setOnClickListener(loginOnClickListener);
    }

    private View.OnClickListener registerOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onRegisterClick();
        }
    };

    private View.OnClickListener loginOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onLoginClick();
        }
    };

    void onRegisterClick() {
        Intent myIntent = new Intent(this, RegisterActivity.class);
        startActivity(myIntent);
    }

    void onLoginClick() {
        Intent myIntent = new Intent(this, LoginActivity.class);
        startActivity(myIntent);
    }
}