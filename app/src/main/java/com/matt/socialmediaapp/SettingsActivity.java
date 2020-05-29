package com.matt.socialmediaapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;

public class SettingsActivity extends AppCompatActivity {

    SwitchCompat postSwitch;

    //use shared preferences to save the state of Switch
    SharedPreferences sp;
    SharedPreferences.Editor editor;    //to edit value of shared pref

    //constant for topic
    private static final String TOPIC_POST_NOTIFICATION = "POST";   //assign any value but use same for this kind of notification

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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
    }

    private void unsubscribePostNotification() {

    }

    private void subscribePostNotification() {
    }
}
