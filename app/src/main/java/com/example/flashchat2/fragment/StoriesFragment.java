package com.example.flashchat2.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.flashchat2.Adapter.TopStatusAdapter;
import com.example.flashchat2.Models.Status;
import com.example.flashchat2.Models.Users;
import com.example.flashchat2.Models.UsersStatus;
import com.example.flashchat2.databinding.FragmentStoriesBinding;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class StoriesFragment extends Fragment {
    public StoriesFragment() {
    }
    FragmentStoriesBinding binding;
    FirebaseDatabase database;
    TopStatusAdapter statusAdapter;
    ArrayList<UsersStatus> usersStatuses;
    ProgressDialog dialog;
    Users users;
    ArrayList<String> chatList=new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding=FragmentStoriesBinding.inflate(getLayoutInflater());
        database=FirebaseDatabase.getInstance();
        usersStatuses=new ArrayList<>();
        dialog=new ProgressDialog(getContext());
        dialog.setMessage("Uploading Story...");
        dialog.setCancelable(false);
        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());
        binding.StoryRecylerview.setLayoutManager(layoutManager);
        statusAdapter=new TopStatusAdapter(getContext(),usersStatuses);
        binding.StoryRecylerview.setAdapter(statusAdapter);
        binding.storyTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,75);
            }
        });
        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        users=snapshot.getValue(Users.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        getchatlist();
        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data !=null){
            dialog.show();
            if (data.getData() !=null){
                FirebaseStorage storage=FirebaseStorage.getInstance();
                Date date=new Date();
                StorageReference reference=storage.getReference().child("status").child(date.getTime()+"");
                reference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    UsersStatus usersStatus=new UsersStatus();
                                    usersStatus.setName(users.getName());
                                    usersStatus.setProfileImage(users.getProfileImage());
                                    usersStatus.setLastUpdated(date.getTime());

                                    HashMap<String, Object> obj=new HashMap<>();
                                    obj.put("name",usersStatus.getName());
                                    obj.put("profileImage",usersStatus.getProfileImage());
                                    obj.put("lastUpdated",usersStatus.getLastUpdated());
                                    String imageUrl=uri.toString();
                                    Status status=new Status(imageUrl,usersStatus.getLastUpdated());
                                    database.getReference().child("stories").child(FirebaseAuth.getInstance().getUid())
                                            .updateChildren(obj);
                                    database.getReference().child("stories").child(FirebaseAuth.getInstance().getUid())
                                            .child("statuses")
                                            .push()
                                            .setValue(status);
                                    dialog.dismiss();
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    private void getchatlist() {
        database.getReference().child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //list.clear();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    UsersStatus users=dataSnapshot.getValue(UsersStatus.class);
                    users.setUid(dataSnapshot.getKey());
                    String chatId=users.getUid();
                    chatList.add(chatId);
                }
                getuserslist();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getuserslist() {
        database.getReference().child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    usersStatuses.clear();
                    for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                        UsersStatus status=new UsersStatus();
                        status.setName(dataSnapshot.child("name").getValue(String.class));
                        status.setProfileImage(dataSnapshot.child("profileImage").getValue(String.class));
                        status.setLastUpdated(dataSnapshot.child("lastUpdated").getValue(Long.class));
                        status.setUid(dataSnapshot.getKey());
                        ArrayList<Status> statuses=new ArrayList<>();
                        for (DataSnapshot storysnapshot:dataSnapshot.child("statuses").getChildren()){
                            Status samplestatus=storysnapshot.getValue(Status.class);
                            statuses.add(samplestatus);
                            try {
                                Long time=samplestatus.getTimestamp();
                                SimpleDateFormat dateFormat = new SimpleDateFormat("kk");
                                SimpleDateFormat day = new SimpleDateFormat("dd");
                                String hours=dateFormat.format(new Date(time));
                                String days=day.format(new Date(time));
                                int i=Integer.parseInt(hours);
                                int j=Integer.parseInt(days);
                                Date c = Calendar.getInstance().getTime();
                                SimpleDateFormat df = new SimpleDateFormat("kk", Locale.getDefault());
                                SimpleDateFormat dd = new SimpleDateFormat("dd", Locale.getDefault());
                                String formattedDate = df.format(c);
                                String date = dd.format(c);
                                int currenttimestamp=Integer.parseInt(formattedDate);
                                int currentdate=Integer.parseInt(date);
                                if (j>=30) {
                                    currentdate = currentdate + j;
                                }
                                Log.d("currentdate",String.valueOf(currentdate));
                                int statusday=currentdate-j;
                                int timezone=currenttimestamp-i+((statusday-1)*24);
                                if (statusday>=1){
                                    if (timezone>=0) {
                                        storysnapshot.getRef().removeValue();
                                    }
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }
                        status.setStatuses(statuses);
                        try {
                            Long time=status.getLastUpdated();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("kk");
                            SimpleDateFormat day = new SimpleDateFormat("dd");
                            String hours=dateFormat.format(new Date(time));
                            String days=day.format(new Date(time));
                            int i=Integer.parseInt(hours);
                            int j=Integer.parseInt(days);
                            Date c = Calendar.getInstance().getTime();
                            SimpleDateFormat df = new SimpleDateFormat("kk", Locale.getDefault());
                            SimpleDateFormat dd = new SimpleDateFormat("dd", Locale.getDefault());
                            String formattedDate = df.format(c);
                            String date = dd.format(c);
                            int currenttimestamp=Integer.parseInt(formattedDate);
                            int currentdate=Integer.parseInt(date);
                            if (j>=30) {
                                currentdate = currentdate + j;
                            }
                            int statusday=currentdate-j;
                            int timezone=currenttimestamp-i+((statusday-1)*24);
                            if (statusday>=1){
                                if (timezone>=0)
                                    dataSnapshot.getRef().removeValue();
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                        if (status.getUid().equals(FirebaseAuth.getInstance().getUid()))
                            usersStatuses.add(status);
                        String statusId=status.getUid()+FirebaseAuth.getInstance().getUid();
                        if (chatList.contains(statusId)){
                            usersStatuses.add(status);
                        }

                    }
                    statusAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
