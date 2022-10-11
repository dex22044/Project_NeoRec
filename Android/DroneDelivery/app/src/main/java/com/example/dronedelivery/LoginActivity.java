package com.example.dronedelivery;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        findViewById(R.id.registerButton_login).setOnClickListener(registerOnClickListener);
        findViewById(R.id.loginButton_login).setOnClickListener(loginOnClickListener);
    }

    private View.OnClickListener registerOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onRegisterClick();
        }
    };

    void onRegisterClick() {
        setContentView(R.layout.activity_register);
    }

    private View.OnClickListener loginOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    onLoginClick();
                }
            });

            thread.start();
        }
    };

    void onLoginClick() {
        HttpURLConnection connection = null;
        int respCode = 0;
        try {
            URL url = new URL("http://" + getResources().getString(R.string.server_address) + ":8000/login");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            String outData = Base64.getEncoder().encodeToString(((EditText)findViewById(R.id.EmailField)).getText().toString().getBytes(StandardCharsets.UTF_8)) + ";"
                    + Base64.getEncoder().encodeToString(((EditText)findViewById(R.id.PasswordField)).getText().toString().getBytes(StandardCharsets.UTF_8)) + "\n";
            writer.write(outData);
            writer.flush();
            writer.close();
            out.close();
            InputStream in = new BufferedInputStream(connection.getInputStream());
            BufferedReader is = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String s = is.readLine();
            is.close();
            in.close();
            respCode = connection.getResponseCode();

            if(respCode == 200) {
                SharedPreferences sp = getSharedPreferences("dd_prefs", 0);
                SharedPreferences.Editor ed = sp.edit();
                if(sp.contains("email")) ed.remove("email");
                if(sp.contains("password")) ed.remove("password");
                ed.putString("email", ((EditText)findViewById(R.id.EmailField)).getText().toString());
                ed.putString("password", ((EditText)findViewById(R.id.PasswordField)).getText().toString());
                ed.commit();

                Intent myIntent = new Intent(this, MenuActivity.class);
                startActivity(myIntent);
                finish();
            }
        } catch(FileNotFoundException e) {
            try {
                InputStream in = new BufferedInputStream(connection.getErrorStream());
                BufferedReader is = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                String s = is.readLine();
                is.close();
                in.close();
                respCode = connection.getResponseCode();
                LoginActivity act = this;
                runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(act);
                        dlgAlert.setMessage("Ошибка входа: " + s);
                        dlgAlert.setTitle("Login error");
                        dlgAlert.setPositiveButton("ок.", null);
                        dlgAlert.create().show();
                    }
                });
            } catch(Exception ee) {
                System.out.println(ee);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}