package com.example.flashchat2.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.example.flashchat2.Adapter.MemeAdapter;
import com.example.flashchat2.Models.Memes;
import com.example.flashchat2.databinding.FragmentMemesBinding;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;

public class MemesFragment extends Fragment {
    public MemesFragment() {}
    FragmentMemesBinding binding;
    FirebaseDatabase database;
    ProgressDialog dialog;
    ArrayList<Memes> list;
    MemeAdapter adapter;
    private Parcelable recyclerViewState;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding=FragmentMemesBinding.inflate(getLayoutInflater());
        database=FirebaseDatabase.getInstance();
        dialog=new ProgressDialog(getContext());
        dialog.setMessage("Uploading Memes...");
        dialog.setCancelable(false);
        list=new ArrayList<>();

        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        SnapHelper snapHelper = new PagerSnapHelper();
        binding.memeRecylerview.setLayoutManager(layoutManager);
        snapHelper.attachToRecyclerView(binding.memeRecylerview);
        adapter=new MemeAdapter(getContext(),list);
        binding.memeRecylerview.setAdapter(adapter);
        adapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);

        database.getReference().child("Memes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Memes memes=dataSnapshot.getValue(Memes.class);
                    list.add(memes);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.memesadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,36);
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==36){
            if (data!=null){
                dialog.show();
                if (data.getData()!=null){
                    FirebaseStorage storage=FirebaseStorage.getInstance();
                    Date date=new Date();
                    StorageReference reference=storage.getReference().child("memes").child(date.getTime()+"");
                    reference.putFile(data.getData()).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()){
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String randomkey=database.getReference().push().getKey();
                                        Memes memes=new Memes(FirebaseAuth.getInstance().getUid(),uri.toString(),randomkey);
                                        database.getReference().child("Memes")
                                                .child(randomkey)
                                                .setValue(memes);
                                        dialog.dismiss();
                                    }
                                });
                            }
                        }
                    });
                }
            }
        }
    }


}
