package com.example.dronedelivery;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ContextParams;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.View;
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

public class SendActivity extends AppCompatActivity {
    ArrayAdapter<String> arrs1, arrs2;
    ArrayList<Integer> postIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        postIds = new ArrayList<Integer>();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                loadShit();
            }
        });
        thread.start();

        findViewById(R.id.sendButton).setOnClickListener(sendOnClickListener);
    }

    private View.OnClickListener sendOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    onSendClick();
                }
            });
            thread.start();
        }
    };

    void onSendClick() {
        SharedPreferences sp = getSharedPreferences("dd_prefs", 0);
        if(!sp.contains("email") || !sp.contains("password")) {
            SendActivity act = this;
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

        Integer post1 = postIds.get(((Spinner)findViewById(R.id.sendPostSelector)).getSelectedItemPosition());
        Integer post2 = postIds.get(((Spinner)findViewById(R.id.receivePostSelector)).getSelectedItemPosition());

        System.out.println(post1 + " " + post2 + " " + email + " " + password);

        HttpURLConnection connection = null;
        int respCode = 0;
        try {
            URL url = new URL("http://" + getResources().getString(R.string.server_address) + ":8000/add_delivery");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            String outData = Base64.getEncoder().encodeToString(email.getBytes(StandardCharsets.UTF_8)) + ";"
                    + Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8)) + ";"
                    + Base64.getEncoder().encodeToString(((EditText)findViewById(R.id.receiverEmailField)).getText().toString().getBytes(StandardCharsets.UTF_8)) + ";"
                    + Base64.getEncoder().encodeToString(post1.toString().getBytes(StandardCharsets.UTF_8)) + ";"
                    + Base64.getEncoder().encodeToString(post2.toString().getBytes(StandardCharsets.UTF_8)) + "\n";
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
                SendActivity act = this;
                runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(act);
                        dlgAlert.setMessage("Заказ добавлен");
                        dlgAlert.setTitle("OK");
                        dlgAlert.setPositiveButton("ок.", (dialogInterface, i) -> {
                            act.finish();
                        });
                        dlgAlert.create().show();
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
                SendActivity act = this;
                runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(act);
                        dlgAlert.setMessage("Ошибка: " + s);
                        dlgAlert.setTitle("Add delivery error");
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

    void loadShit() {
        HttpURLConnection connection = null;
        int respCode = 0;
        try {
            URL url = new URL("http://" + getResources().getString(R.string.server_address) + ":8000/get_posts");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            InputStream in = new BufferedInputStream(connection.getInputStream());
            BufferedReader is = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String s = is.readLine();
            is.close();
            in.close();
            respCode = connection.getResponseCode();

            if(respCode == 200) {
                JSONArray arr = new JSONArray(s);
                List<String> arrr = new ArrayList<String>();
                postIds.clear();
                for(int i = 0; i < arr.length(); i++) {
                    JSONObject jsonObj = arr.getJSONObject(i);
                    postIds.add(jsonObj.getInt("id"));
                    String ss = "[" + jsonObj.getInt("id") + "] " + jsonObj.getString("address");
                    arrr.add(ss);
                }
                SendActivity act = this;
                runOnUiThread(new Runnable() {
                    public void run() {
                        arrs1 = new ArrayAdapter<String>(act, R.layout.simple_spinner_item, arrr);
                        ((Spinner)findViewById(R.id.sendPostSelector)).setAdapter(arrs1);
                        arrs2 = new ArrayAdapter<String>(act, R.layout.simple_spinner_item, arrr);
                        ((Spinner)findViewById(R.id.receivePostSelector)).setAdapter(arrs2);
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
                SendActivity act = this;
                runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(act);
                        dlgAlert.setMessage("Произошёл троллинг: " + s);
                        dlgAlert.setTitle("Load error");
                        dlgAlert.setPositiveButton("ок.", null);
                        // dlgAlert.setCancelable(true);
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