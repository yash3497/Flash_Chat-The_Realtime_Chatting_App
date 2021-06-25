package com.example.flashchat2.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.example.flashchat2.R;
import com.example.flashchat2.databinding.ActivityImageViewActionBinding;

public class ImageViewAction extends AppCompatActivity {
    ActivityImageViewActionBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityImageViewActionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        String imageUrl=getIntent().getStringExtra("image");
        Glide.with(this).load(imageUrl).placeholder(R.drawable.imageplaceholder).into(binding.imageaction);
    }
}