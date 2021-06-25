package com.example.flashchat2.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.example.flashchat2.R;
import com.example.flashchat2.databinding.ActivityProfiledetailsBinding;

public class Profiledetails extends AppCompatActivity {
    ActivityProfiledetailsBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityProfiledetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent=getIntent();
        String username=intent.getStringExtra("username");
        String phoneno=intent.getStringExtra("phoneno");
        String about=intent.getStringExtra("about");
        String imageUrl=intent.getStringExtra("image");
        String uid=intent.getStringExtra("uid");
        binding.username.setText(username);
        binding.usernumber.setText(phoneno);
        binding.about.setText(about);
        Glide.with(this).load(imageUrl).placeholder(R.drawable.nature).into(binding.imageView2);
        Glide.with(this).load(imageUrl).placeholder(R.drawable.ic_user).into(binding.userprofile);
    }
}