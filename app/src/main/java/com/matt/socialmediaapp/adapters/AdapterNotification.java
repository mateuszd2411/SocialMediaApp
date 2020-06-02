package com.matt.socialmediaapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.matt.socialmediaapp.R;
import com.matt.socialmediaapp.models.ModelNotifications;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

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
    public void onBindViewHolder(@NonNull HolderNotificacion holder, int position) {
        //get and set data to views

        //get data
        ModelNotifications model = notificationsList.get(position);
        String name = model.getsName();
        String notification = model.getNotification();
        String image = model.getsImage();
        String timestamp = model.getTimestamp();

        //set to views
        holder.nameTv.setText(name);
        holder.notificationTv.setText(notification);

        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_default_img).into(holder.avatarIv);
        } catch (Exception e) {
            holder.avatarIv.setImageResource(R.drawable.ic_default_img);
        }
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
