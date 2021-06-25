package com.example.flashchat2.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.flashchat2.databinding.ActivityPhoneloginBinding;
import com.google.firebase.auth.FirebaseAuth;

public class PhoneloginActivity extends AppCompatActivity {
     ActivityPhoneloginBinding binding;
     FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityPhoneloginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth=FirebaseAuth.getInstance();
        if (auth.getCurrentUser()!=null){
            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
        binding.phonenumber.requestFocus();
        binding.getotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.phonenumber.getText().toString().length()==10) {
                    Intent intent = new Intent(getApplicationContext(), OtpActivity.class);
                    intent.putExtra("phonenumber", binding.phonenumber.getText().toString());
                    startActivity(intent);
                }else{
                    binding.phonenumberlayout.setError("Enter a valid number");
                }
            }
        });
    }
}