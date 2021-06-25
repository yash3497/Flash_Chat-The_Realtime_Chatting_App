package com.example.flashchat2.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.flashchat2.Adapter.GroupProfileDetailsAdapter;
import com.example.flashchat2.Adapter.UsersAdapter;
import com.example.flashchat2.Models.GroupDetails;
import com.example.flashchat2.R;
import com.example.flashchat2.databinding.ActivityGroupProfileDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupProfileDetails extends AppCompatActivity {
    ActivityGroupProfileDetailsBinding binding;
    ArrayList<GroupDetails>list;
    GroupProfileDetailsAdapter adapter;
    FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityGroupProfileDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String groupid=getIntent().getStringExtra("groupid");
        String groupname=getIntent().getStringExtra("groupname");
        String groupimage=getIntent().getStringExtra("groupimage");
        String groupabout=getIntent().getStringExtra("groupabout");
        binding.groupname.setText(groupname);
        binding.groupAbout.setText(groupabout);
        Glide.with(this).load(groupimage).placeholder(R.drawable.nature).into(binding.imageView2);
        Glide.with(this).load(groupimage).placeholder(R.drawable.ic_user).into(binding.userprofile);
        list=new ArrayList<>();
        database=FirebaseDatabase.getInstance();
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        binding.groupusers.setLayoutManager(layoutManager);
        adapter=new GroupProfileDetailsAdapter(this,list);
        binding.groupusers.setAdapter(adapter);
        database.getReference().child("Group")
                .child(groupid)
                .child("Participants")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                            GroupDetails groupDetails=dataSnapshot.getValue(GroupDetails.class);
                            list.add(groupDetails);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        binding.addparticipant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(GroupProfileDetails.this,GroupAdduserActivity.class);
                intent.putExtra("groupid",groupid);
                intent.putExtra("groupname",groupname);
                intent.putExtra("groupimage",groupimage);
                intent.putExtra("groupabout",groupabout);
                startActivity(intent);
            }
        });
        binding.exitgroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              database.getReference().child("Group")
                      .child(groupid)
                      .child("Participants")
                      .child(FirebaseAuth.getInstance().getUid())
                      .removeValue();
            }
        });
    }
}