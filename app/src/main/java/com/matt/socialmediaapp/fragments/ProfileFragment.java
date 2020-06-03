package com.matt.socialmediaapp.fragments;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.matt.socialmediaapp.AddPostActivity;
import com.matt.socialmediaapp.MainActivity;
import com.matt.socialmediaapp.R;
import com.matt.socialmediaapp.SettingsActivity;
import com.matt.socialmediaapp.adapters.AdapterPosts;
import com.matt.socialmediaapp.models.ModelPost;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.storage.FirebaseStorage.getInstance;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {


    //firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FloatingActionButton fab;
    //storage
    StorageReference storageReference;
    //path where images of user profile and cover will be stored
    String storagePath = "Users_Profile_Cover_Imgs/";

    //views
    ImageView avatarIv, coverIv;
    TextView nameTv, emailTv, phoneTv;
    RecyclerView postsRecyclerView;

    //progress dialog
    ProgressDialog progressDialog;

    //permissions constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;
    //arrays of permissions to be request
    String cameraPermissions[];
    String storagePermissions[];

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    //uri of picked image
    Uri image_uri;

    //for checking profile or cover photo
    String profileOrCoverPhoto;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference();        //firebase storage reference


        //init arrays of permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //init views
        avatarIv = view.findViewById(R.id.avatarIv);
        coverIv = view.findViewById(R.id.coverIv);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        fab = view.findViewById(R.id.fab);
        postsRecyclerView = view.findViewById(R.id.recyclerview_posts);

        //init progress dialog
        progressDialog = new ProgressDialog(getActivity());

        /*We have to get info of currently signed in user. We can get it using user's email or uid
        I'm gonna retrieve user detail using email
        By using orderByChild query we will show the detail from a node whose key named email has
        value equal to currently signed in email.
        It will search all nodes, where the key matches it will get its detail*/

        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //check until required data get
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data from database
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();

                    //set data to fragment_profile.xml
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);
                    try {
                        //if image is received then set
                        Picasso.get().load(image).into(avatarIv);
                    } catch (Exception e) {
                        //if there is any exception while getting image then set default
                        Picasso.get().load(R.drawable.ic_add_image).into(avatarIv);
                    }

                    try {
                        //if image is received then set
                        Picasso.get().load(cover).into(coverIv);
                    } catch (Exception e) {
                        //if there is any exception while getting image then set default
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //fab button click
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditProfileDialog();
            }
        });

        postList = new ArrayList<>();

        checkUserStatus();
        loadMyPost();

        return view;
    }

    private void loadMyPost() {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show newest post first, for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerView
        postsRecyclerView.setLayoutManager(layoutManager);

        //init posts list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //query to load posts
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    //add to list
                    postList.add(myPosts);

                    //adapter
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    //set this adapter to recyclerView
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchMyPost(final String searchQuery) {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show newest post first, for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerView
        postsRecyclerView.setLayoutManager(layoutManager);

        //init posts list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //query to load posts
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    if (myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                        //add to list
                        postList.add(myPosts);
                    }

                    //adapter
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    //set this adapter to recyclerView
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkStoragePermission() {
        //check if storage permission is enabled or not
        //return true if enable
        // return false if not enable
        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission() {
        //request runtime storage permission
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        //check if storage permission is enabled or not
        //return true if enable
        // return false if not enable

        boolean result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermission() {
        //request runtime storage permission
        requestPermissions(cameraPermissions, CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialog() {
        /*
        Show dialing containing options with
        1) Edit Profile Picture
        2) Edit Cover Photo
        3) Edit Name
        4) Edit Phone
        5) Change Password
         */

        //options to show in dialog
        String options[] = {"Edit Profile Picture", "Edit Cover Photo", "Edit Name", "Edit Phone", "Change Password"};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle("Choose Action");
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                //handle dialog item click
                if (which == 0) {
                    //Edit Profile clicked
                    progressDialog.setMessage("Updating Profile Picture");
                    profileOrCoverPhoto = "image";
                    showImagePicDialog();
                }
                if (which == 1) {
                    //Edit Cover Photo clicked
                    progressDialog.setMessage("Updating Cover Photo");
                    profileOrCoverPhoto = "cover";
                }
                if (which == 2) {
                    //Edit Name clicked
                    //calling method and pass key "name" as parameter to update it's value in database
                    showNamePhoneUpdateDialog("name");
                    progressDialog.setMessage("Updating Name");
                }
                if (which == 3) {
                    //Edit Phone clicked
                    progressDialog.setMessage("Updating Phone");
                    showNamePhoneUpdateDialog("phone");
                }
                if (which == 4) {
                    //Edit Phone clicked
                    progressDialog.setMessage("Changing Password");
                    showChangePasswordDialog();
                }
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void showChangePasswordDialog() {
        //password change dialog with custom layout having currentPassword, newPassword and update button

        //inflate layout for dialog
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_update_password, null);
        final EditText passwordEt = view.findViewById(R.id.passwordEt);
        final EditText newPasswordEt = view.findViewById(R.id.newPasswordEt);
        Button updatePasswordBtn = view.findViewById(R.id.updatePasswordBtn);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);  //set view to dialog

        final AlertDialog dialog = builder.create();
        dialog.show();

        updatePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //validate data
                String oldPassword = passwordEt.getText().toString().trim();
                String newPassword = newPasswordEt.getText().toString().trim();
                if (TextUtils.isEmpty(oldPassword)) {
                    Toast.makeText(getActivity(), "Enter your current password...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newPassword.length() < 6) {
                    Toast.makeText(getActivity(), "Password length must at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                dialog.dismiss();
                updatePassword(oldPassword, newPassword);
            }
        });
    }

    private void updatePassword(String oldPassword, final String newPassword) {
        progressDialog.show();

        //get current user
        final FirebaseUser user = firebaseAuth.getCurrentUser();

        //before changing password re-authenticate the user
        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
        user.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //password undated
                                        progressDialog.dismiss();
                                        Toast.makeText(getActivity(), "Password Updated...", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showNamePhoneUpdateDialog(final String key) {
        // "key" parameter is for name and phone

        //custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update " + key);      // name or phone
        //set layout of dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10, 10, 10, 10);
        //add edit text
        final EditText editText = new EditText(getActivity());
        editText.setHint("Enter " + key);       // name or phone
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //add buttons in dialog to update
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //input text from edit text
                final String value = editText.getText().toString().trim();
                //validate if user has entered something or not
                if (!TextUtils.isEmpty(value)) {
                    progressDialog.show();
                    HashMap<String, Object> result = new HashMap<>();
                    result.put(key, value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), "Updated...", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    //if user edit his name, also change it from his posts
                    if (key.equals("name")) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    String child = ds.getKey();
                                    dataSnapshot.getRef().child(child).child("uName").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        //update name in current user comments on posts
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    String child = ds.getKey();
                                    if (dataSnapshot.child(child).hasChild("Comments")) {
                                        String child1 = "" + dataSnapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                    String child = ds.getKey();
                                                    dataSnapshot.getRef().child(child).child("uName").setValue(value);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                } else {
                    Toast.makeText(getActivity(), "Please enter " + key, Toast.LENGTH_SHORT).show();
                }
            }
        });
        //add buttons in dialog to cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                progressDialog.dismiss();
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void showImagePicDialog() {
        //show dialog containing options Camera and Gallery to pick the image
        String options[] = {"Camera", "Gallery"};
        //alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle("Pick Image From");
        //set items to dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                //handle dialog item click
                if (which == 0) {
                    //Camera clicked

                    if (!checkCameraPermission()) {
                        requestCameraPermission();
                    } else {
                        pickFromCamera();
                    }
                } else if (which == 1) {
                    //Gallery clicked

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /*
        This method called when user press Allow or Deny from permission request dialog
        here we will handle permission cases (allowed and denied)
         */

        switch (requestCode) {
            case CAMERA_REQUEST_CODE:{
                //picking from camera, first check if camera and storage permissions allowed or not
                if (grantResults.length > 0) {
                    boolean cameraActepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageActepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraActepted && writeStorageActepted) {
                        //permissions enabled
                        pickFromCamera();
                    } else {
                        //permissions denied
                        Toast.makeText(getActivity(), "Please enable camera & storage permissions", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                //picking from gallery, first check if storage permissions allowed or not
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        //permissions enabled
                        pickFromGallery();
                    } else {
                        //permissions denied
                        Toast.makeText(getActivity(), "Please enable storage permission", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //This method will be called after picking image from Ca,era or Gallery

        if (resultCode == RESULT_OK) {

            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
                //image is picked from gallery, get uri of image
                image_uri = data.getData();

                uploadProfileCoverPhoto(image_uri);
            }
            if (requestCode == IMAGE_PICK_CAMERA_CODE) {
                //image is picked from camera, get uri of image
                uploadProfileCoverPhoto(image_uri);
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri uri) {
        //show progress bar
        progressDialog.show();

        /*
        Instead of creating separate function for Profile Picture and Cover Photo (for both in same function)
         */

        //path and name of image to be stored in firebase storage
        String filePathAndName = storagePath + "" + profileOrCoverPhoto + "_" + user.getUid();

        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //image is uploaded to storage, now get it's url and store in user's database
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                final Uri downloadUri = uriTask.getResult();

                //check if image is uploaded or not and url is received
                if (uriTask.isSuccessful()) {
                    //image uploaded
                    HashMap<String, Object> result = new HashMap<>();
                    /*
                    First Parameter is profileOrCoverPhoto that has value "image" or "cover"
                    which are keys in user's database where url of image will be saved in one of them

                    Second Parameter contains the url of the image stored in firebase storage, this url
                    will be saved as value against key "image" or "cover"
                     */
                    result.put(profileOrCoverPhoto, downloadUri.toString());

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //url in database of user is added successfully
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), "Image Updated...", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Error Updating Image...", Toast.LENGTH_SHORT).show();
                        }
                    });

                    //if user edit his photo, also change it from his posts
                    if (profileOrCoverPhoto.equals("image")) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = ref.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    String child = ds.getKey();
                                    dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        //update user image in current users comments on posts
                        //update name in current user comments on posts
                        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    String child = ds.getKey();
                                    if (dataSnapshot.child(child).hasChild("Comments")) {
                                        String child1 = "" + dataSnapshot.child(child).getKey();
                                        Query child2 = FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                                    String child = ds.getKey();
                                                    dataSnapshot.getRef().child(child).child("uDp").setValue(downloadUri.toString());
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                } else {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //there were some error
                progressDialog.dismiss();
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void pickFromCamera() {
        //Intent of picking image form device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp Description");
        //put image uri
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        //intent to start camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE);
    }

    private void pickFromGallery() {
        //pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGE_PICK_GALLERY_CODE);

    }

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            //set email of logged in user
            uid = user.getUid();
        } else {
            //user not signed in, go to MainActivity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);        //to show menu option in fragment
        super.onCreate(savedInstanceState);
    }


    /*
    inflate options menu
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflating menu
        inflater.inflate(R.menu.menu_main, menu);

        //hide some options
        menu.findItem(R.id.action_create_group).setVisible(false);

        MenuItem item = menu.findItem(R.id.action_search);
        //v7 searchView to search user specific posts
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button
                if (!TextUtils.isEmpty(s)) {
                    //search
                    searchMyPost(s);
                } else {
                    loadMyPost();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called whenever user type any letter
                if (!TextUtils.isEmpty(s)) {
                    //search
                    searchMyPost(s);
                } else {
                    loadMyPost();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    //handle menu item click


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        else if (id == R.id.action_add_post) {
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }
        else if (id == R.id.action_settings) {
            //go to settings activity
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}
