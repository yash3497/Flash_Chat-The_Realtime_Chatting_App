package com.example.flashchat2.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.example.flashchat2.Models.Users;
import com.example.flashchat2.databinding.ActivityProfilesetupBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class profilesetup extends AppCompatActivity {
    ActivityProfilesetupBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityProfilesetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();
        dialog=new ProgressDialog(this);
        dialog.setMessage("Profile Created");
        dialog.setCancelable(false);

        binding.profilemage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,45);
            }
        });
        binding.setup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=binding.username.getText().toString();
                String about=binding.about.getText().toString();
                if (name.isEmpty()){
                    binding.username1.setError("Please type a name");
                    return;
                }
                if (about.isEmpty()){
                    binding.about1.setError("Please type something about you");
                    return;
                }
                dialog.show();
                if (selectedImage!=null){
                    StorageReference reference=storage.getReference().child("Profiles").child(auth.getUid());
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imageUrl=uri.toString();
                                        String uid= auth.getUid();
                                        String phone=auth.getCurrentUser().getPhoneNumber();
                                        String name=binding.username.getText().toString();
                                        String about=binding.about.getText().toString();
                                        Users users= new Users(uid,name,phone,imageUrl,about);
                                        database.getReference()
                                                .child("Users")
                                                .child(uid)
                                                .setValue(users)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        dialog.dismiss();
                                                        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                                                        startActivity(intent);
                                                    }
                                                });
                                    }
                                });
                            }
                        }
                    });
                }else {
                    String uid= auth.getUid();
                    String phone=auth.getCurrentUser().getPhoneNumber();
                    Users users= new Users(uid,name,phone,"No Image",about);
                    database.getReference()
                            .child("Users")
                            .child(uid)
                            .setValue(users)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    dialog.dismiss();
                                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                                    startActivity(intent);
                                }
                            });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data!=null){
            if (data.getData()!=null){
                binding.profilemage.setImageURI(data.getData());
                selectedImage= data.getData();
            }
        }
    }
}