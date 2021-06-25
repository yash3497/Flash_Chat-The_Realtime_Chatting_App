package com.example.flashchat2.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flashchat2.Activities.GroupChatActivity;
import com.example.flashchat2.Models.GroupDetails;
import com.example.flashchat2.Models.Users;
import com.example.flashchat2.R;
import com.example.flashchat2.databinding.UserviewBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GroupUsersAdapter extends RecyclerView.Adapter<GroupUsersAdapter.UsersViewHolder>{
    Context context;
    ArrayList<GroupDetails> users;

    public GroupUsersAdapter(Context context, ArrayList<GroupDetails> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.userview,parent,false);

        return new UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        GroupDetails user=users.get(position);
        FirebaseDatabase.getInstance().getReference()
                .child("Groupchat")
                .child(user.getGroupId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String lastmsg = snapshot.child("lastmsg").getValue(String.class);
                            long time = snapshot.child("lastmsgtime").getValue(long.class);
                            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
                            holder.binding.lasttime.setText(dateFormat.format(new Date(time)));
                            holder.binding.lastmessage.setText(lastmsg);
                        }else {
                            holder.binding.lastmessage.setText("Tap to chat");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        holder.binding.chatusername.setText(user.getGroupName());
        Glide.with(context).load(user.getGroupImage()).placeholder(R.drawable.ic_user).into(holder.binding.circleImageView);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupid",user.getGroupId());
                intent.putExtra("groupname",user.getGroupName());
                intent.putExtra("groupimage",user.getGroupImage());
                intent.putExtra("groupabout",user.getGroupAbout());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder{
        UserviewBinding binding;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            binding=UserviewBinding.bind(itemView);
        }
    }
}
