package com.example.flashchat2.Activities;

import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.flashchat2.Adapter.MessagesAdapter;
import com.example.flashchat2.Models.Message;
import com.example.flashchat2.Models.Users;
import com.example.flashchat2.Notification.APIService;
import com.example.flashchat2.Notification.Client;
import com.example.flashchat2.Notification.Data;
import com.example.flashchat2.Notification.Response;
import com.example.flashchat2.Notification.Sender;
import com.example.flashchat2.Notification.Token;
import com.example.flashchat2.R;
import com.example.flashchat2.databinding.ActivityChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;

public class ChatActivity extends AppCompatActivity {
    ActivityChatBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message> messages;
    String senderRoom,receiverRoom;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog progressDialog;
    String currentUid;
    APIService apiService;
    boolean notify = false;
    File file;
    Uri fileUri;
    String userName,profilepic,phoneno,about;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database=FirebaseDatabase.getInstance();
        storage=FirebaseStorage.getInstance();
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Uploading image...");
        progressDialog.setCancelable(false);
        binding.recyclerView.requestDisallowInterceptTouchEvent(true);
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);
        binding.profiletoolbar.inflateMenu(R.menu.chatmenu);
        userName=getIntent().getStringExtra("username");
        String uid=getIntent().getStringExtra("userid");
        profilepic=getIntent().getStringExtra("profilepic");
        phoneno=getIntent().getStringExtra("phoneno");
        about=getIntent().getStringExtra("about");
            database.getReference().child("Users")
                    .child(uid)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Users user1=snapshot.getValue(Users.class);
                            userName=user1.getName();
                            profilepic=user1.getProfileImage();
                            about=user1.getAbout();
                            phoneno=user1.getPhoneNumber();
                            binding.username1.setText(userName);
                            Glide.with(getApplicationContext()).load(profilepic).placeholder(R.drawable.ic_user).into(binding.profilepic);
                            binding.userclick.setOnClickListener(v -> {
                                Intent intent=new Intent(ChatActivity.this,Profiledetails.class);
                                intent.putExtra("username",userName);
                                intent.putExtra("phoneno",phoneno);
                                intent.putExtra("about",about);
                                intent.putExtra("image",profilepic);
                                intent.putExtra("uid",uid);
                                Pair[] p =new Pair[2];
                                p[0]=Pair.create(binding.profilepic, "logo");
                                p[1]=Pair.create(binding.username1,"name");
                                ActivityOptions activityOptions=ActivityOptions.makeSceneTransitionAnimation(ChatActivity.this,p);
                                startActivity(intent,activityOptions.toBundle());
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

        currentUid= FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               if (snapshot.exists()){
                   String status=snapshot.getValue(String.class);
                   if (!status.isEmpty()){
                       binding.indicator.setText(status);
                       binding.indicator.setVisibility(View.VISIBLE);
                   }
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        senderRoom=currentUid+uid;
        receiverRoom=uid+currentUid;
        messages=new ArrayList<>();
        adapter=new MessagesAdapter(this,messages,senderRoom,receiverRoom,ChatActivity.this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        database.getReference().child("chats").child(senderRoom)
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
        binding.sendbutton.setOnClickListener(v -> {
            notify = true;
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
                    database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgobj);
                    database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgobj);
                    assert randomKey != null;
                    database.getReference().child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(randomKey)
                            .setValue(message).addOnSuccessListener(aVoid -> database.getReference().child("chats")
                                    .child(receiverRoom)
                                    .child("messages")
                                    .child(randomKey)
                                    .setValue(message).addOnSuccessListener(aVoid1 -> {

                                    }));
                    ViewCompat.setNestedScrollingEnabled(binding.recyclerView, false);
                    binding.recyclerView.smoothScrollToPosition(messages.size());
                    DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Users").child(currentUid);
                    databaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                           Users users=snapshot.getValue(Users.class);
                           if (notify){
                               senNotification(uid,users.getName(),messageTxt);
                           }
                           notify=false;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        binding.username1.setText(userName);
        Glide.with(this).load(profilepic).placeholder(R.drawable.ic_user).into(binding.profilepic);
        binding.backarrow.setOnClickListener(v -> {
            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finishAffinity();
        });
        binding.profiletoolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId()==R.id.profiledetails){
                binding.userclick.callOnClick();
            }else if (item.getItemId()==R.id.setting){
                Intent setting=new Intent(getApplicationContext(),SettingActivity.class);
                startActivity(setting);
            }else if (item.getItemId()==R.id.clearchat){
                FirebaseDatabase.getInstance().getReference()
                        .child("chats")
                        .child(String.valueOf(senderRoom))
                        .setValue(null);
                Toast.makeText(getApplicationContext(),"Chat clear",Toast.LENGTH_SHORT).show();
            }
            return false;
        });
        binding.attachment.setOnClickListener(v -> {
            Intent intent=new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent,26);
        });
        final Handler handler=new Handler();
        binding.messagebox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                database.getReference().child("presence").child(currentUid).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping,1000);
            }
            final Runnable userStoppedTyping=new Runnable() {
                @Override
                public void run() {
                    database.getReference().child("presence").child(currentUid).setValue("Online");
                }
            };
        });
        binding.userclick.setOnClickListener(v -> {
            Intent intent=new Intent(ChatActivity.this,Profiledetails.class);
            intent.putExtra("username",userName);
            intent.putExtra("phoneno",phoneno);
            intent.putExtra("about",about);
            intent.putExtra("image",profilepic);
            intent.putExtra("uid",uid);
            Pair[] p =new Pair[2];
            p[0]=Pair.create(binding.profilepic, "logo");
            p[1]=Pair.create(binding.username1,"name");
            ActivityOptions activityOptions=ActivityOptions.makeSceneTransitionAnimation(ChatActivity.this,p);
            startActivity(intent,activityOptions.toBundle());
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
                startActivityForResult(camera_intent, 19);
            }
        });
    }

    private void senNotification(String uid, String name, String messageTxt) {
        DatabaseReference allTokens=FirebaseDatabase.getInstance().getReference("Tokens");
        Query query=allTokens.orderByKey().equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                   Token token=dataSnapshot.getValue(Token.class);
                   Data data=new Data(currentUid,name+":"+messageTxt,"New Message",uid,R.drawable.ic_flash);
                   Sender sender=new Sender(data,token.getToken());
                   apiService.sendNotification(sender)
                           .enqueue(new Callback<Response>() {
                               @Override
                               public void onResponse(@NotNull Call<Response> call, @NotNull retrofit2.Response<Response> response) {
                               }

                               @Override
                               public void onFailure(@NotNull Call<Response> call, Throwable t) {

                               }
                           });
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==26){
            if (data!=null){
                if (data.getData()!=null){
                    Uri selectedImage=data.getData();
                    Calendar calendar=Calendar.getInstance();
                    StorageReference reference=storage.getReference().child("chats").child(calendar.getTimeInMillis()+"");
                    progressDialog.show();
                    reference.putFile(selectedImage).addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        if (task.isSuccessful()){
                            reference.getDownloadUrl().addOnSuccessListener(uri -> {
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
                                    database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgobj);
                                    database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgobj);
                                    database.getReference().child("chats")
                                            .child(senderRoom)
                                            .child("messages")
                                            .child(randomKey)
                                            .setValue(message).addOnSuccessListener(aVoid -> database.getReference().child("chats")
                                                    .child(receiverRoom)
                                                    .child("messages")
                                                    .child(randomKey)
                                                    .setValue(message).addOnSuccessListener(aVoid1 -> {

                                                    }));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    });
                }
            }
        }
        if (requestCode ==19)
        {
                    Calendar calendar=Calendar.getInstance();
                    StorageReference reference=storage.getReference().child("chats").child(calendar.getTimeInMillis()+"");
                    progressDialog.show();
                    reference.putFile(fileUri).addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        if (task.isSuccessful()){
                            reference.getDownloadUrl().addOnSuccessListener(uri -> {
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
                                    database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgobj);
                                    database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgobj);
                                    database.getReference().child("chats")
                                            .child(senderRoom)
                                            .child("messages")
                                            .child(randomKey)
                                            .setValue(message).addOnSuccessListener(aVoid -> database.getReference().child("chats")
                                            .child(receiverRoom)
                                            .child("messages")
                                            .child(randomKey)
                                            .setValue(message).addOnSuccessListener(aVoid1 -> {

                                            }));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    });
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        String currentUserId=FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentUserId).setValue("Online");
    }
    @Override
    public void onPause() {
        super.onPause();
        String currentUserId=FirebaseAuth.getInstance().getUid();
        database.getReference().child("presence").child(currentUserId).setValue("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chatmenu,menu);
        return true;
    }
}