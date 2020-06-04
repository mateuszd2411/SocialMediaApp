package com.matt.socialmediaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class GroupChatActivity extends AppCompatActivity {

    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        //get id of the group
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupId");
    }
}
