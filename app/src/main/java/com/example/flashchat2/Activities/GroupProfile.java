package com.example.flashchat2.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.flashchat2.Models.GroupDetails;
import com.example.flashchat2.Models.Selectchat;
import com.example.flashchat2.R;
import com.example.flashchat2.databinding.ActivityGroupProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;

public class GroupProfile extends AppCompatActivity {
    ActivityGroupProfileBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityGroupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();
        dialog=new ProgressDialog(this);
        dialog.setMessage("Group Created");
        dialog.setCancelable(false);
        Selectchat currentuser=new Selectchat();
        currentuser.setUid(auth.getUid());
        currentuser.setUsername(auth.getCurrentUser().getDisplayName());
        currentuser.setProfileimageurl(null);
        currentuser.setPhoneNumber(auth.getCurrentUser().getPhoneNumber());
        currentuser.setAbout(null);
        currentuser.setSelected(false);
        Intent intent=getIntent();
        Bundle args=intent.getBundleExtra("bundle");
        ArrayList<Selectchat> userid=(ArrayList<Selectchat>)args.getSerializable("arraylist");
        userid.add(userid.size(),currentuser);

        binding.GroupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,48);
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
                    Date date=new Date();
                    StorageReference reference=storage.getReference().child("Group").child(date.getTime()+"");
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imageurl=uri.toString();
                                        String groupname=binding.username.getText().toString();
                                        String about=binding.about.getText().toString();
                                        String groupid=date.getTime()+"";
                                        String timestamp=date.getTime()+"";
                                        String createdBy=auth.getUid();
                                        GroupDetails group=new GroupDetails();
                                        group.setGroupName(groupname);
                                        group.setGroupAbout(about);
                                        group.setCreatedBy(createdBy);
                                        group.setGroupId(groupid);
                                        group.setGroupImage(imageurl);
                                        group.setTimestamp(timestamp);
                                        database.getReference().child("Group").child(groupid).setValue(group).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                for (int i=0;i<userid.size();i++){
                                                    String userUid=userid.get(i).getUid();
                                                    GroupDetails ParticipantsDetails=new GroupDetails();
                                                    ParticipantsDetails.setUid(userUid);
                                                    ParticipantsDetails.setTimestamp(date.getTime()+"");
                                                    if (userUid.equals(auth.getUid())){
                                                        ParticipantsDetails.setRole("Admin");
                                                    }else{
                                                        ParticipantsDetails.setRole("");
                                                    }
                                                    database.getReference().child("Group").child(groupid).child("Participants").child(userUid).setValue(ParticipantsDetails)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    dialog.dismiss();
                                                                    //Toast.makeText(getApplicationContext(),"create",Toast.LENGTH_SHORT).show();
                                                                    Intent intent1 = new Intent(GroupProfile.this,GroupChatActivity.class);
                                                                    intent1.putExtra("groupid",groupid);
                                                                    intent1.putExtra("groupname",groupname);
                                                                    intent1.putExtra("groupimage",imageurl);
                                                                    startActivity(intent1);
                                                                    finishAffinity();
                                                                }
                                                            });
                                                }
                                            }
                                        });
                                    }
                                });
                            }
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
                binding.GroupImage.setImageURI(data.getData());
                selectedImage= data.getData();
            }
        }
    }
}