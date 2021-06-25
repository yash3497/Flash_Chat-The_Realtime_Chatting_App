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
import com.example.flashchat2.Activities.ChatActivity;
import com.example.flashchat2.Models.GroupDetails;
import com.example.flashchat2.Models.Users;
import com.example.flashchat2.R;
import com.example.flashchat2.databinding.UserviewBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupProfileDetailsAdapter extends RecyclerView.Adapter<GroupProfileDetailsAdapter.UsersViewHolder>{
    Context context;
    ArrayList<GroupDetails> user;

    public GroupProfileDetailsAdapter(Context context, ArrayList<GroupDetails> user) {
        this.context = context;
        this.user = user;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.userview,parent,false);

        return new UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        GroupDetails groupDetails=user.get(position);
        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(groupDetails.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users users=snapshot.getValue(Users.class);
                holder.binding.chatusername.setText(users.getName());
                holder.binding.lastmessage.setText(users.getAbout());
                holder.binding.lasttime.setText(groupDetails.getRole());
                Glide.with(context).load(users.getProfileImage()).placeholder(R.drawable.ic_user).into(holder.binding.circleImageView);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(context, ChatActivity.class);
                        intent.putExtra("userid",users.getUid());
                        intent.putExtra("profilepic",users.getProfileImage());
                        intent.putExtra("username",users.getName());
                        intent.putExtra("phoneno",users.getPhoneNumber());
                        intent.putExtra("about",users.getAbout());
                        context.startActivity(intent);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return user.size();
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder{
        UserviewBinding binding;
        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            binding=UserviewBinding.bind(itemView);
        }
    }
}
