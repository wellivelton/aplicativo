package com.tv.telecine;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.tv.telecine.activity.BannerActivity;
import com.tv.telecine.database.DatabaseHelper;
import com.tv.telecine.database.PreferenceUtils;
import com.zhy.autolayout.config.AutoLayoutConifg;


public class SplashActivity extends AppCompatActivity {
    private TextView mTexView;
    private ProgressBar mProgressBar;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AutoLayoutConifg.getInstance().useDeviceSize();//Auto size
        setContentView(R.layout.activity_splash);

        db = new DatabaseHelper(SplashActivity.this);
        PreferenceUtils.updateSubscriptionStatus(SplashActivity.this);

        mProgressBar = findViewById(R.id.mProgressBar);
        mTexView=findViewById(R.id.mTextVersion);
        mTexView.setText("Versão "+ BuildConfig.VERSION_NAME);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (PreferenceUtils.isLoggedIn(SplashActivity.this)) {

                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();

               } else {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    finish();
               }
               mProgressBar.setVisibility(View.GONE);
            }

        },2500);


        checkStoragePermission();
    }


    // ------------------ checking storage permission ------------
    private boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                Log.d("test", "Permission is granted");
                return true;

            } else {
                Log.d("test", "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.d("test", "Permission is granted");
            return true;
        }
    }




}