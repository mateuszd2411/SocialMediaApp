package com.matt.socialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AddPostActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    ActionBar actionBar;

    //permissions constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;

    //permissions array
    String[] cameraPermissions;
    String[] storagePermissions;

    //views
    EditText titleEt, descriptionEt;
    ImageView imageIv;
    Button uploadBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Add New Post");
        //enable back button in actionbar
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //init permissions array
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        cameraPermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
        checkUserStatus();

        //init views
        titleEt = findViewById(R.id.pTitleEt);
        descriptionEt = findViewById(R.id.pDescriptionEt);
        imageIv = findViewById(R.id.pImageIv);
        uploadBtn = findViewById(R.id.pUploadBtn);

        //get image from camera/gallery on click
        imageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show image pick dialog
                showImagePickDialog();
            }
        });

        //upload button click listener
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get data (title, description) from EditTexes
                String title = titleEt.getText().toString().trim();
                String description = descriptionEt.getText().toString().trim();
            }
        });
    }

    private void showImagePickDialog() {
        //options(camera, gallery) to show in dialog
        String[] options = {"Camera", "Gallery"};

        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image from");
        //set options to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //item click here
                if (i == 0) {
                    //camera clicked
                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }
                }
                if (i == 1) {
                    //gallery clicked
                    if (!checkStoragePermission()) {
                        requestStoragePermission();
                    } else {
                        pickFromGallery();
                    }
                }
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void pickFromGallery() {

    }

    private void pickFromCamera() {

    }

    private boolean checkStoragePermission() {
        //check if storage permission is enable or not
        //return if enable
        //return false if not enable
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        //request runtime storage permission
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        //check if camera permission is enable or not
        //return if enable
        //return false if not enable
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);

        return result && result1;
    }

    private void requestCameraPermission() {
        //request runtime storage permission
        ActivityCompat.requestPermissions(this, storagePermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserStatus();
    }

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            //set email of logged in user
        } else {
            //user not signed in, go to MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();    //go to previous activity
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_add_post).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    //handle permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //this method is called when user press Allow or Deny from permission request dialog
        //here we will handle permission cases (allow and denied)

        switch (requestCode) {
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        //both permission are granted
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "Camera & Storage both permissions are necessary", Toast.LENGTH_SHORT).show();
                    }
                } else {

                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        //storage permission granted
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Storage permissions is necessary", Toast.LENGTH_SHORT).show();
                    }
                } else {

                }

            }
            break;

        }
    }
}
