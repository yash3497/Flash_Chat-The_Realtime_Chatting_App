package com.example.flashchat2.fragment;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.flashchat2.Activities.ContactActivity;
import com.example.flashchat2.Adapter.UsersAdapter;
import com.example.flashchat2.Models.Users;
import com.example.flashchat2.Notification.Token;
import com.example.flashchat2.Permission.permission;
import com.example.flashchat2.R;
import com.example.flashchat2.Utils.CountryToPhonePrefix;
import com.example.flashchat2.databinding.FragmentChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.ImmutableSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.collection.ImmutableSortedSet;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

public class ChatFragment extends Fragment {
    public ChatFragment(){}
    FragmentChatBinding binding;
    ArrayList<Users> list ,chatlist;
    FirebaseDatabase database;
    UsersAdapter adapter;
    String mUID;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding=FragmentChatBinding.inflate(getLayoutInflater());
        database=FirebaseDatabase.getInstance();
        list=new ArrayList<>();
        chatlist=new ArrayList<>();
        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());
        binding.chatRecyclerview.setLayoutManager(layoutManager);
        adapter=new UsersAdapter(getContext(),list);
        binding.chatRecyclerview.setAdapter(adapter);
        getchatlist();
        binding.contactTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), ContactActivity.class);
                startActivity(intent);
            }
        });
        checkUserStatus();
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        String tokenFresh=task.getResult();
                        updateToken(tokenFresh);
                    }
                });
        return binding.getRoot();
    }
    private void updateToken(String token){
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken=new Token(token);
        ref.child(mUID).setValue(mToken);
    }

    private void getchatlist() {
        database.getReference().child("chats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Users users=dataSnapshot.getValue(Users.class);
                    users.setUid(dataSnapshot.getKey());
                    String chatId=users.getUid();
                    getuserslist(chatId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getuserslist(String chatId) {
        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Users users1=dataSnapshot.getValue(Users.class);
                    users1.setUid(dataSnapshot.getKey());
                    String userId=users1.getUid()+FirebaseAuth.getInstance().getUid();
                    if (userId.equals(chatId)){
                        list.add(users1);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onResume() {
        checkUserStatus();
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
    private void checkUserStatus(){
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null){
            mUID=user.getUid();
            SharedPreferences sp=getContext().getSharedPreferences("SP_USER",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor=sp.edit();
            editor.putString("Current_USERID",mUID);
            editor.apply();
        }
    }
}
