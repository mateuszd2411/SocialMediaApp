package com.matt.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    Button changeLang;

    ////////////////////////////////////////////For Dark Theme
    SwitchCompat darkModeSwitch;

    ////////////////////////////////////////////For Notifications
    SwitchCompat postSwitch;

    //use shared preferences to save the state of Switch
    SharedPreferences sp;
    SharedPreferences.Editor editor;    //to edit value of shared pref

    //constant for topic
    private static final String TOPIC_POST_NOTIFICATION = "POST";   //assign any value but use same for this kind of notification
    ////////////////////////////////////////////For Notifications
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ////////////////////////////////////////////For Dark Theme

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        ////////////////////////////////////////////For Dark Theme

        super.onCreate(savedInstanceState);
        loadLocale();       //for change language
        setContentView(R.layout.activity_settings);

        darkModeSwitch = findViewById(R.id.darkModeSwitch);
        changeLang = findViewById(R.id.change_language);

        //validation for switching to dark mode theme
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            darkModeSwitch.setChecked(true);
        }

        darkModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    restartApp();
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    restartApp();
                }
            }
        });

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(R.string.SettingsTitle);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        ////////////////////////////////////////////For Notifications
        postSwitch = findViewById(R.id.postSwitch);

        //init sp
        sp = getSharedPreferences("Notification_SP", MODE_PRIVATE);
        boolean isPosteEnabled = sp.getBoolean("" + TOPIC_POST_NOTIFICATION, false);
        //if enabled checked switch, otherwise uncheck switch - by default unchecked/false
        if (isPosteEnabled) {
            postSwitch.setChecked(true);
        } else {
            postSwitch.setChecked(false);
        }

        //implement switch change listener
        postSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                //edit switch state
                editor = sp.edit();
                editor.putBoolean("" + TOPIC_POST_NOTIFICATION, isChecked);
                editor.apply();

                if (isChecked) {
                    subscribePostNotification();    //call to subscribe
                } else {
                    unsubscribePostNotification();    //call to unsubscribe
                }
            }
        });
        ////////////////////////////////////////////For Notifications

        //For change language
        changeLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show alert dialog to display list of language, one can be selected
                showChangeLanguageDialog();
            }
        });


    }//End onCreate

    ////////////////////////////////////////////For Dark Theme
    private void restartApp() {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
        finish();
    }

    ////////////////////////////////////////////For Notifications
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void unsubscribePostNotification() {
        //unsubscribe to a topic (POST) to disable it's notification
        FirebaseMessaging.getInstance().subscribeToTopic("" + TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.postNotificationsMsg);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.Subscribefailed);
                        }
                        Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void subscribePostNotification() {
        //subscribe to a topic (POST) to enable it's notification
        FirebaseMessaging.getInstance().subscribeToTopic("" + TOPIC_POST_NOTIFICATION)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.receivepostnotifications);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.Subscribefailed);
                        }
                        Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    ////////////////////////////////////////////For Notifications


    //For change language
    private void showChangeLanguageDialog() {
        //array of language to display in alert dialog
        final String[] listItems = {"Polski", "English"};

        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle("Choose Language...");
        builder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0) {
                    //Polski
                    setLocale("pl");
                    recreate();
                }
                if (i == 1) {
                    //English
                    setLocale("en");
                    recreate();
                }

                //dismiss alert dialog when language selected
                dialogInterface.dismiss();
            }
        });

        AlertDialog mDialog = builder.create();
        builder.show();
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());

        //save data to share preferences
        SharedPreferences.Editor editor = getSharedPreferences("Settings1", MODE_PRIVATE).edit();
        editor.putString("My_Lang1", lang);
        editor.apply();
    }

    //load language saved in share preferences
    private void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("Settings1", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Lang1", "");
        setLocale(language);
    }
}
