package com.example.dronedelivery;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
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

public class EditProfileActivity extends AppCompatActivity {
    Bitmap selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        findViewById(R.id.registerButton2).setOnClickListener(registerOnClickListener);
        findViewById(R.id.imageButton2).setOnClickListener(capPhotoOnClickListener);
    }

    private View.OnClickListener registerOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    onRegisterClick();
                }
            });

            thread.start();
        }
    };

    private View.OnClickListener capPhotoOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onCapPhotoClick();
        }
    };

    void onCapPhotoClick() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePicture.putExtra("return-data", true);
        Intent pickPicture = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        pickPicture.putExtra("return-data", true);

        ArrayList<Intent> intentList = new ArrayList<Intent>();
        intentList.add(takePicture);
        intentList.add(pickPicture);

        Intent chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1), "???????????? ?? ?????????? ?????????????? ?? ?????? ??????????");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));

        startActivityForResult(chooserIntent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {
            case 0:
            case 1:
                if(resultCode == RESULT_OK) {
                    if(imageReturnedIntent.getExtras() != null && imageReturnedIntent.getExtras().containsKey("data")) {
                        System.out.println("FUCK");
                        System.out.flush();
                        selectedImage = (Bitmap) imageReturnedIntent.getExtras().get("data");
                    } else {
                        System.out.println(imageReturnedIntent.getData().toString());
                        AssetFileDescriptor fileDescriptor = null;
                        try {
                            fileDescriptor = getContentResolver().openAssetFileDescriptor(imageReturnedIntent.getData(), "r");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        selectedImage = (Bitmap) BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor());
                    }

                    if(selectedImage == null) return;
                    ((ImageButton)findViewById(R.id.imageButton2)).setImageBitmap(selectedImage);
                }

                break;
        }
    }

    void onRegisterClick() {
        if(((EditText)findViewById(R.id.registerEmailField2)).getText().toString().length() == 0) {
            EditProfileActivity act = this;
            runOnUiThread(new Runnable() {
                public void run() {
                    AlertDialog.Builder dlgAlert = new AlertDialog.Builder(act);
                    dlgAlert.setMessage("?????????? ?????????? ??????????");
                    dlgAlert.setTitle("Edit error");
                    dlgAlert.setPositiveButton("????.", null);
                    // dlgAlert.setCancelable(true);
                    dlgAlert.create().show();
                }
            });
            return;
        }

        if(((EditText)findViewById(R.id.registerPasswordField2)).getText().toString().length() == 0) {
            EditProfileActivity act = this;
            runOnUiThread(new Runnable() {
                public void run() {
                    AlertDialog.Builder dlgAlert = new AlertDialog.Builder(act);
                    dlgAlert.setMessage("???????????? ?????????? ??????????");
                    dlgAlert.setTitle("Edit error");
                    dlgAlert.setPositiveButton("????.", null);
                    // dlgAlert.setCancelable(true);
                    dlgAlert.create().show();
                }
            });
            return;
        }
        if(((EditText)findViewById(R.id.registerFullNameField2)).getText().toString().length() == 0) {
            EditProfileActivity act = this;
            runOnUiThread(new Runnable() {
                public void run() {
                    AlertDialog.Builder dlgAlert = new AlertDialog.Builder(act);
                    dlgAlert.setMessage("?????? ?????????? ??????????");
                    dlgAlert.setTitle("Edit error");
                    dlgAlert.setPositiveButton("????.", null);
                    // dlgAlert.setCancelable(true);
                    dlgAlert.create().show();
                }
            });
            return;
        }

        if(selectedImage == null) {
            EditProfileActivity act = this;
            runOnUiThread(new Runnable() {
                public void run() {
                    AlertDialog.Builder dlgAlert = new AlertDialog.Builder(act);
                    dlgAlert.setMessage("?????????? ???????????? ??????????");
                    dlgAlert.setTitle("Edit error");
                    dlgAlert.setPositiveButton("????.", null);
                    // dlgAlert.setCancelable(true);
                    dlgAlert.create().show();
                }
            });
            return;
        }

        HttpURLConnection connection = null;
        int respCode = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        selectedImage.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] faceEncoding = null;
        try {
            URL url = new URL("http://" + getResources().getString(R.string.server_address) + ":8000/get_face_encoding");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setReadTimeout(1000);

            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            out.write(baos.toByteArray(), 0, baos.size());
            baos.close();
            out.flush();
            out.close();
            InputStream in = new BufferedInputStream(connection.getInputStream());
            respCode = connection.getResponseCode();

            if(respCode == 200) {
                faceEncoding = new byte[1024];
                in.read(faceEncoding, 0, 1024);
            }

            in.close();
        } catch(FileNotFoundException e) {
            try {
                InputStream in = new BufferedInputStream(connection.getErrorStream());
                BufferedReader is = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                String s = is.readLine();
                is.close();
                in.close();
                respCode = connection.getResponseCode();
                EditProfileActivity act = this;
                runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(act);
                        dlgAlert.setMessage("???????????? ??????????????????: " + s);
                        dlgAlert.setTitle("Edit error");
                        dlgAlert.setPositiveButton("????.", null);
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

        if(faceEncoding == null) return;

        try {
            URL url = new URL("http://" + getResources().getString(R.string.server_address) + ":8000/edit_user");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            String outData = Base64.getEncoder().encodeToString(((EditText)findViewById(R.id.registerEmailField2)).getText().toString().getBytes(StandardCharsets.UTF_8)) + ";"
                    + Base64.getEncoder().encodeToString(((EditText)findViewById(R.id.registerPasswordField2)).getText().toString().getBytes(StandardCharsets.UTF_8)) + ";"
                    + Base64.getEncoder().encodeToString(((EditText)findViewById(R.id.registerFullNameField2)).getText().toString().getBytes(StandardCharsets.UTF_8)) + ";"
                    + Base64.getEncoder().encodeToString(faceEncoding) + "\n";
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
                EditProfileActivity act = this;
                runOnUiThread(new Runnable() {
                    public void run() {
                        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(act);
                        dlgAlert.setMessage("???????????? ??????????????????: " + s);
                        dlgAlert.setTitle("Edit error");
                        dlgAlert.setPositiveButton("????.", null);
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