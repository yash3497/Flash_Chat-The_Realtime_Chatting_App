package com.example.flashchat2.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flashchat2.Activities.ChatActivity;
import com.example.flashchat2.Models.Users;
import com.example.flashchat2.R;
import com.example.flashchat2.databinding.UserviewBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.UsersViewHolder>{
    Context context;
    ArrayList<Users> users;

    public ContactAdapter(Context context, ArrayList<Users> users) {
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
        Users user=users.get(position);
        holder.binding.chatusername.setText(user.getName());
        Glide.with(context).load(user.getProfileImage()).placeholder(R.drawable.ic_user).into(holder.binding.circleImageView);
        holder.binding.lastmessage.setText(user.getAbout());
        holder.binding.lasttime.setVisibility(View.INVISIBLE);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, ChatActivity.class);
                intent.putExtra("userid",user.getUid());
                intent.putExtra("profilepic",user.getProfileImage());
                intent.putExtra("username",user.getName());
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
