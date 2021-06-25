package com.example.flashchat2.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.flashchat2.Adapter.GroupUsersAdapter;
import com.example.flashchat2.Models.GroupDetails;
import com.example.flashchat2.Models.Users;
import com.example.flashchat2.databinding.FragmentGroupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupFragment extends Fragment {
    public GroupFragment(){}
    FragmentGroupBinding binding;
    ArrayList<GroupDetails> list;
    FirebaseDatabase database;
    GroupUsersAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding=FragmentGroupBinding.inflate(getLayoutInflater());
        database=FirebaseDatabase.getInstance();
        list=new ArrayList<>();
        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());
        binding.chatRecyclerview.setLayoutManager(layoutManager);
        adapter=new GroupUsersAdapter(getContext(),list);
        binding.chatRecyclerview.setAdapter(adapter);
        database.getReference().child("Group").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    GroupDetails groupDetails=dataSnapshot.getValue(GroupDetails.class);
                    database.getReference().child("Group")
                            .child(dataSnapshot.getKey())
                            .child("Participants")
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot dataSnapshot1:snapshot.getChildren()){
                                        String id=dataSnapshot1.getKey();
                                        if (id.equals(FirebaseAuth.getInstance().getUid())) {
                                            list.add(groupDetails);
                                        }
                                    }
                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return binding.getRoot();
    }
}
