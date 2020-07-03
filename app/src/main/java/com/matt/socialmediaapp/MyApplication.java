package com.matt.socialmediaapp;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        ////////////////////////////////////////////For Dark Theme

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        ////////////////////////////////////////////For Dark Theme
        super.onCreate();

        //enable firebase offline
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
