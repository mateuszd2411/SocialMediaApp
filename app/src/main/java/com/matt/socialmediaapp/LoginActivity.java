package com.matt.socialmediaapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    //views
    EditText mEmailEt, mPasswordEt;
    TextView notHaveAccount;
    Button mLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Actionbar and its title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Login");
        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //init
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        notHaveAccount = findViewById(R.id.nothave_accountTv);
        mLoginBtn = findViewById(R.id.loginBtn);



    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();    //go previous activity
        return super.onSupportNavigateUp();
    }

}
