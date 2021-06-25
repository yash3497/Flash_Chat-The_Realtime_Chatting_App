package com.example.flashchat2.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.flashchat2.databinding.ActivityLicenseBinding;

public class License extends AppCompatActivity {
    ActivityLicenseBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityLicenseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbar7.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),HelpActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}