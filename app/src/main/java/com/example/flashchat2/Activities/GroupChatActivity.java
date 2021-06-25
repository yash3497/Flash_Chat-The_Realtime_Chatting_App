package com.example.flashchat2.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.flashchat2.Adapter.GroupMessageAdapter;
import com.example.flashchat2.Models.Message;
import com.example.flashchat2.R;
import com.example.flashchat2.databinding.ActivityGroupChatBinding;
import com.example.flashchat2.fragment.GroupFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {
     ActivityGroupChatBinding binding;
     GroupMessageAdapter adapter;
    ArrayList<Message> messages;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog progressDialog;
    String currentUid,groupid,groupname,groupimages,groupabout;
    File file;
    Uri fileUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityGroupChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();
        groupid=getIntent().getStringExtra("groupid");
        groupname=getIntent().getStringExtra("groupname");
        groupimages=getIntent().getStringExtra("groupimage");
        groupabout=getIntent().getStringExtra("groupabout");
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Uploading image...");
        progressDialog.setCancelable(false);
        binding.GrouprecyclerView.requestDisallowInterceptTouchEvent(true);
        currentUid= FirebaseAuth.getInstance().getUid();
        binding.profiletoolbar.inflateMenu(R.menu.groupmenu);
        messages=new ArrayList<>();
        adapter=new GroupMessageAdapter(this,messages,groupid,GroupChatActivity.this);
        binding.GrouprecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.GrouprecyclerView.setAdapter(adapter);
        database.getReference().child("Groupchat").child(groupid)
                .child("messages").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    Message message=snapshot1.getValue(Message.class);
                    message.setMessageId(snapshot1.getKey());
                    messages.add(message);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        binding.sendbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (binding.messagebox.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), "Type your message", Toast.LENGTH_SHORT).show();
                    } else {
                        String messageTxt = binding.messagebox.getText().toString();
                        Date date = new Date();
                        Message message = new Message(messageTxt, currentUid, date.getTime());
                        binding.messagebox.setText("");
                        String randomKey = database.getReference().push().getKey();
                        HashMap<String, Object> lastMsgobj = new HashMap<>();
                        lastMsgobj.put("lastmsg", message.getMessage());
                        lastMsgobj.put("lastmsgtime", date.getTime());
                        database.getReference().child("Groupchat").child(groupid).updateChildren(lastMsgobj);
                        database.getReference().child("Groupchat")
                                .child(groupid)
                                .child("messages")
                                .child(randomKey)
                                .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                            }
                        });
                        ViewCompat.setNestedScrollingEnabled(binding.GrouprecyclerView, false);
                        binding.GrouprecyclerView.smoothScrollToPosition(messages.size());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        binding.username1.setText(groupname);
        Glide.with(this).load(groupimages).placeholder(R.drawable.ic_user).into(binding.profilepic);
        binding.backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finishAffinity();
            }
        });
        binding.attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,28);
            }
        });
        binding.profiletoolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId()==R.id.groupdetail){
                    binding.userclick.callOnClick();
                }else if (item.getItemId()==R.id.addcontact){
                    Intent intent=new Intent(GroupChatActivity.this,GroupAdduserActivity.class);
                    intent.putExtra("groupid",groupid);
                    intent.putExtra("groupname",groupname);
                    intent.putExtra("groupimage",groupimages);
                    intent.putExtra("groupabout",groupabout);
                    startActivity(intent);
                }else if (item.getItemId()==R.id.clearchat){
                    messages.clear();
                    adapter.notifyDataSetChanged();
                }
                return false;
            }
        });
        binding.userclick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(GroupChatActivity.this,GroupProfileDetails.class);
                intent.putExtra("groupid",groupid);
                intent.putExtra("groupname",groupname);
                intent.putExtra("groupimage",groupimages);
                intent.putExtra("groupabout",groupabout);
                Pair p[]=new Pair[2];
                p[0]=Pair.create(binding.profilepic, "logo");
                p[1]=Pair.create(binding.username1,"name");
                ActivityOptions activityOptions=ActivityOptions.makeSceneTransitionAnimation(GroupChatActivity.this,p);
                startActivity(intent,activityOptions.toBundle());
            }
        });
        binding.camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                file = new File(getApplication().getExternalCacheDir(),
                        String.valueOf(System.currentTimeMillis()) + ".jpg");
                fileUri = Uri.fromFile(file);
                camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                startActivityForResult(camera_intent, 22);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==28){
            if (data!=null){
                if (data.getData()!=null){
                    Uri selectedImage=data.getData();
                    Calendar calendar=Calendar.getInstance();
                    StorageReference reference=storage.getReference().child("Groupchat").child(calendar.getTimeInMillis()+"");
                    progressDialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        try {
                                            String filePath = uri.toString();
                                            String messageTxt = binding.messagebox.getText().toString();
                                            Date date = new Date();
                                            Message message = new Message(messageTxt, currentUid, date.getTime());
                                            message.setMessage("PHOTO");
                                            message.setImageUrl(filePath);
                                            binding.messagebox.setText("");
                                            String randomKey = database.getReference().push().getKey();
                                            HashMap<String, Object> lastMsgobj = new HashMap<>();
                                            lastMsgobj.put("lastmsg", message.getMessage());
                                            lastMsgobj.put("lastmsgtime", date.getTime());
                                            database.getReference().child("Groupchat").child(groupid).updateChildren(lastMsgobj);
                                            database.getReference().child("Groupchat")
                                                    .child(groupid)
                                                    .child("messages")
                                                    .child(randomKey)
                                                    .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                }
                                            });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
        if (requestCode==22){
            Calendar calendar=Calendar.getInstance();
            StorageReference reference=storage.getReference().child("Groupchat").child(calendar.getTimeInMillis()+"");
            progressDialog.show();
            reference.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    progressDialog.dismiss();
                    if (task.isSuccessful()){
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                try {
                                    String filePath = uri.toString();
                                    String messageTxt = binding.messagebox.getText().toString();
                                    Date date = new Date();
                                    Message message = new Message(messageTxt, currentUid, date.getTime());
                                    message.setMessage("PHOTO");
                                    message.setImageUrl(filePath);
                                    binding.messagebox.setText("");
                                    String randomKey = database.getReference().push().getKey();
                                    HashMap<String, Object> lastMsgobj = new HashMap<>();
                                    lastMsgobj.put("lastmsg", message.getMessage());
                                    lastMsgobj.put("lastmsgtime", date.getTime());
                                    database.getReference().child("Groupchat").child(groupid).updateChildren(lastMsgobj);
                                    database.getReference().child("Groupchat")
                                            .child(groupid)
                                            .child("messages")
                                            .child(randomKey)
                                            .setValue(message).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                        }
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            });
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.groupmenu,menu);
        return true;
    }
}