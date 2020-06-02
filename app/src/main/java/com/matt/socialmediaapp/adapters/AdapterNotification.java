package com.matt.socialmediaapp.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matt.socialmediaapp.R;
import com.matt.socialmediaapp.models.ModelNotifications;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterNotification extends RecyclerView.Adapter<AdapterNotification.HolderNotificacion>{

    private Context context;
    private ArrayList<ModelNotifications> notificationsList;

    public AdapterNotification(Context context, ArrayList<ModelNotifications> notificationsList) {
        this.context = context;
        this.notificationsList = notificationsList;
    }

    @NonNull
    @Override
    public HolderNotificacion onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate view row_notification

        View view = LayoutInflater.from(context).inflate(R.layout.row_notification, parent, false);

        return new HolderNotificacion(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HolderNotificacion holder, int position) {
        //get and set data to views

        //get data
        final ModelNotifications model = notificationsList.get(position);
        String name = model.getsName();
        String notification = model.getNotification();
        String image = model.getsImage();
        String timestamp = model.getTimestamp();
        String senderUid = model.getsUid();

        //convert timestamp to dd/mm/yyyy hh:mm am/pm
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String pTime = DateFormat.format("dd/MM/yyyy hh:mm aa", calendar).toString();

        //we will get the name, email, image, of the user of notification from his uid
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(senderUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            String name = "" + ds.child("name").getValue();
                            String image = "" + ds.child("image").getValue();
                            String email = "" + ds.child("email").getValue();

                            //added to model
                            model.setsName(name);
                            model.setsEmail(email);
                            model.setsImage(image);

                            //set to views
                            holder.nameTv.setText(name);

                            try {
                                Picasso.get().load(image).placeholder(R.drawable.ic_default_img).into(holder.avatarIv);
                            } catch (Exception e) {
                                holder.avatarIv.setImageResource(R.drawable.ic_default_img);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        holder.notificationTv.setText(notification);
        holder.timeTv.setText(pTime);

    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    //holder class for views of row_notifications.xml
    class HolderNotificacion extends RecyclerView.ViewHolder {

        //decelerate views
        ImageView avatarIv;
        TextView nameTv, notificationTv, timeTv;

        public HolderNotificacion(@NonNull View itemView) {
            super(itemView);

            //init views
            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            notificationTv = itemView.findViewById(R.id.notificationTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }

}
