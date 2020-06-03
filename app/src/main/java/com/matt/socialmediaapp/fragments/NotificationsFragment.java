package com.matt.socialmediaapp.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matt.socialmediaapp.R;
import com.matt.socialmediaapp.adapters.AdapterNotification;
import com.matt.socialmediaapp.models.ModelNotifications;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationsFragment extends Fragment {

    //recyclerView
    RecyclerView notificationsRv;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelNotifications> notificationsList;

    private AdapterNotification adapterNotification;
    public NotificationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        //init recyclerView
        notificationsRv = view.findViewById(R.id.notificationRv);

        firebaseAuth = FirebaseAuth.getInstance();

        getAllNotifications();

        return view;
    }

    private void getAllNotifications() {

        notificationsList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Notifications")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        notificationsList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()){

                            ModelNotifications model = ds.getValue(ModelNotifications.class);

                            notificationsList.add(model);
                        }

                        adapterNotification = new AdapterNotification(getActivity(), notificationsList);

                        notificationsRv.setAdapter(adapterNotification);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

}