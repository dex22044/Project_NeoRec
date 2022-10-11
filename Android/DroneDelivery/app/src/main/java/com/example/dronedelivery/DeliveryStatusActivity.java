package com.example.dronedelivery;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONObject;

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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class DeliveryStatusActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_status);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                loadShit();
            }
        });
        thread.start();
    }

    void loadShit() {
        SharedPreferences sp = getSharedPreferences("dd_prefs", 0);
        if(!sp.contains("email") || !sp.contains("password")) {
            DeliveryStatusActivity act = this;
            runOnUiThread(new Runnable() {
                public void run() {
                    AlertDialog.Builder dlgAlert = new AlertDialog.Builder(act);
                    dlgAlert.setMessage("Выйди и зайди нормально");
                    dlgAlert.setTitle("Вы не вошли");
                    dlgAlert.setPositiveButton("ок.", null);
                    dlgAlert.create().show();
                }
            });
            return;
        }

        String email = sp.getString("email", "");
        String password = sp.getString("password", "");

        HttpURLConnection connection = null;
        int respCode = 0;
        try {
            URL url = new URL("http://" + getResources().getString(R.string.server_address) + ":8000/get_delivery_statuses");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            String outData = Base64.getEncoder().encodeToString(email.getBytes(StandardCharsets.UTF_8)) + ";"
                    + Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8)) + "\n";
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
                JSONArray arr = new JSONArray(s);
                String text = "";
                for(int i = 0; i < arr.length(); i++) {
                    JSONObject jsonObj = arr.getJSONObject(i);
                    String ss = "";
                    ss += "ID" + jsonObj.getInt("id") + "; от: " + jsonObj.getString("email_from") + "; кому: " + jsonObj.getString("email_to") + "\n";
                    ss += "Из " + jsonObj.getString("from_post") + ", в " + jsonObj.getString("to_post") + "; статус: ";
                    switch(jsonObj.getInt("status")) {
                        case 0: ss += "Ожидает отправки"; break;
                        case 1: ss += "Отправлено"; break;
                        case 2: ss += "Получено"; break;
                    }
                    ss += "\n";
                    ss += "--------------------------------------\n";
                    text += ss;
                }
                final String textF = text;

                runOnUiThread(new Runnable() {
                    public void run() {
                        ((EditText)findViewById(R.id.deliveryStatuses)).setText(textF);
                    }
                });
            }
        } catch(FileNotFoundException e) {
            try {
                InputStream in = new BufferedInputStream(connection.getErrorStream());
                BufferedReader is = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                String s = is.readLine();
                is.close();
                in.close();
                respCode = connection.getResponseCode();
                DeliveryStatusActivity act = this;
                runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(act);
                        dlgAlert.setMessage("Ошибка: " + s);
                        dlgAlert.setTitle("Get deliveries error");
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