package com.example.flashchat2.Activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import com.example.flashchat2.databinding.ActivityAboutBinding;

public class AboutActivity extends AppCompatActivity {
    ActivityAboutBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar3);
        binding.toolbar3.setNavigationOnClickListener(v -> {
            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finishAffinity();
        });
        binding.instagram.setOnClickListener(v -> {
            Uri uri = Uri.parse("https://www.instagram.com/myself_yashgupta/");
            Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

            likeIng.setPackage("com.instagram.android");

            try {
                startActivity(likeIng);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.instagram.com/myself_yashgupta/")));
            }
        });
        binding.github.setOnClickListener(v -> {
            String url="https://github.com/yash3497";
            CustomTabsIntent.Builder builder=new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            customTabsIntent.launchUrl(getApplicationContext(), Uri.parse(url));
        });
        binding.linkedin.setOnClickListener(v -> {
            Uri uri = Uri.parse("https://www.linkedin.com/in/yash-gupta-84a5511b8/");
            Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

            likeIng.setPackage("com.linkedin.android");

            try {
                startActivity(likeIng);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.linkedin.com/in/yash-gupta-84a5511b8/")));
            }
        });
        binding.youtube.setOnClickListener(v -> {
            Uri uri = Uri.parse("https://www.youtube.com/channel/UCEZFp5sO1H9RU160Agb3VuA");
            Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);

            likeIng.setPackage("com.youtube.android");

            try {
                startActivity(likeIng);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.youtube.com/channel/UCEZFp5sO1H9RU160Agb3VuA")));
            }
        });
    }
}