package com.example.flashchat2.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flashchat2.Models.Memes;
import com.example.flashchat2.R;
import com.example.flashchat2.databinding.MemeviewBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class MemeAdapter extends RecyclerView.Adapter<MemeAdapter.MemeViewholder>{
    Context context;
    ArrayList<Memes> memesArrayList;

    public MemeAdapter(Context context, ArrayList<Memes> memesArrayList) {
        this.context = context;
        this.memesArrayList = memesArrayList;
    }

    @NonNull
    @Override
    public MemeViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.memeview,parent,false);
        return new MemeViewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemeViewholder holder, int position) {
        Collections.shuffle(memesArrayList);
        Memes memes=memesArrayList.get(position);
        Glide.with(context).load(memes.getImageurl()).placeholder(R.drawable.blackimage).into(holder.binding.memeimage);
        holder.binding.shareimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StrictMode.VmPolicy.Builder builder=new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());

                BitmapDrawable drawable=(BitmapDrawable)holder.binding.memeimage.getDrawable();
                Bitmap bitmap=drawable.getBitmap();
                File f=new File(context.getExternalCacheDir()+"/"+context.getResources().getString(R.string.app_name)+".png");
                Intent shareint;
                try {
                    FileOutputStream outputStream=new FileOutputStream(f);
                    bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
                    outputStream.flush();
                    outputStream.close();
                    shareint=new Intent(Intent.ACTION_SEND);
                    shareint.setType("image/*");
                    shareint.putExtra(Intent.EXTRA_TEXT,"Hey, Check out this cool meme I got from Flash Chat App");
                    shareint.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
                    shareint.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                }catch (Exception e){
                    throw new RuntimeException(e);
                }
                context.startActivity(Intent.createChooser(shareint, "Share image via"));
            }
        });
    }

    @Override
    public int getItemCount() {
        return memesArrayList.size();
    }

    public class MemeViewholder extends RecyclerView.ViewHolder{
        MemeviewBinding binding;
        public MemeViewholder(@NonNull View itemView) {
            super(itemView);
            binding=MemeviewBinding.bind(itemView);
        }
    }
}
