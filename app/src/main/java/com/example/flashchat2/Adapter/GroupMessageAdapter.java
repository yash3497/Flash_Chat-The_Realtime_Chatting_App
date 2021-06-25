                                                                                                                                                                                                                     package com.example.flashchat2.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flashchat2.Activities.ImageViewAction;
import com.example.flashchat2.Models.Message;
import com.example.flashchat2.Models.Users;
import com.example.flashchat2.R;
import com.example.flashchat2.databinding.GrouprecievemessageBinding;
import com.example.flashchat2.databinding.GroupsendmessageBinding;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;



public class GroupMessageAdapter extends RecyclerView.Adapter{
    Activity activity;
    Context context;
    ArrayList<Message> messages;
    String GroupId;
    final int send_message_count=1;
    final int receive_message_count=2;

    public GroupMessageAdapter(Context context, ArrayList<Message> messages, String groupId,Activity activity) {
        this.context = context;
        this.messages = messages;
        this.GroupId = groupId;
        this.activity=activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == send_message_count) {
            View view = LayoutInflater.from(context).inflate(R.layout.groupsendmessage, parent, false);
            return new sentviewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.grouprecievemessage, parent, false);
            return new RecieverViewHolder(view);
        }
    }
    @Override
    public int getItemViewType(int position) {
        Message message=messages.get(position);
        if (FirebaseAuth.getInstance().getUid().equals(message.getSenderId())){
            return send_message_count;
        }else{
            return receive_message_count;
        }
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message=messages.get(position);
        int[] reaction =new int[]{
                R.drawable.ic_fb_like,
                R.drawable.ic_fb_love,
                R.drawable.ic_fb_laugh,
                R.drawable.ic_fb_wow,
                R.drawable.ic_fb_sad,
                R.drawable.ic_fb_angry
        };
        ReactionsConfig config = new ReactionsConfigBuilder(context)
                .withReactions(reaction).build();
        ReactionPopup popup = new ReactionPopup(context, config, (pos) -> {
            if (pos !=-1)
                if (holder.getClass() == sentviewHolder.class) {
                    sentviewHolder viewHolder = (sentviewHolder) holder;
                    viewHolder.binding.feelings.setImageResource(reaction[pos]);
                    viewHolder.binding.feelings.setVisibility(View.VISIBLE);
                } else {
                    RecieverViewHolder viewHolder = (RecieverViewHolder) holder;
                    viewHolder.binding.feeling.setImageResource(reaction[pos]);
                    viewHolder.binding.feeling.setVisibility(View.VISIBLE);
                }
            notifyDataSetChanged();
            message.setFelling(pos);
            FirebaseDatabase.getInstance().getReference()
                    .child("Groupchat")
                    .child(GroupId)
                    .child("messages")
                    .child(message.getMessageId()).setValue(message);
            return true; // true is closing popup, false is requesting a new selection
        });
        if (holder.getClass()== sentviewHolder.class){
            sentviewHolder viewHolder=(sentviewHolder)holder;
            if (message.getMessage().equals("PHOTO")) {
                viewHolder.binding.simage.setVisibility(View.VISIBLE);
                viewHolder.binding.smessage.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl()).placeholder(R.drawable.imageplaceholder).into(viewHolder.binding.simage);
            }else {
                viewHolder.binding.smessage.setText(message.getMessage());}

            if (message.getFelling()>=0){
                // message.setFelling(reaction[(int)message.getFelling()]);
                viewHolder.binding.feelings.setImageResource(reaction[message.getFelling()]);
                viewHolder.binding.feelings.setVisibility(View.VISIBLE);
            }else{
                viewHolder.binding.feelings.setVisibility(View.GONE);
            }

            viewHolder.binding.smessage.setOnTouchListener(new View.OnTouchListener() {
                GestureDetector detector=new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        View v = new View(context);
                        popup.onTouch(v,e);
                        return super.onDoubleTap(e);
                    }


                    @Override
                    public void onLongPress(MotionEvent e) {
                        new AlertDialog.Builder(context)
                                .setTitle("Delete messages?")
                                .setNeutralButton("cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).setNegativeButton("DELETE FOR EVERYONE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseDatabase.getInstance().getReference()
                                        .child("Groupchat")
                                        .child(GroupId)
                                        .child("messages")
                                        .child(message.getMessageId()).child("message").setValue("This message was deleted");
                            }
                        }).show();
                        super.onLongPress(e);
                    }
                });
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    detector.onTouchEvent(event);
                    return true;
                }
            });
            viewHolder.binding.simage.setOnTouchListener(new View.OnTouchListener() {
                GestureDetector detector=new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        View v = new View(context);
                        popup.onTouch(v,e);
                        return super.onDoubleTap(e);
                    }

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        Intent imageIntent=new Intent(activity, ImageViewAction.class);
                        imageIntent.putExtra("image",message.getImageUrl());
                        Pair[] p =new Pair[1];
                        p[0]=Pair.create(viewHolder.binding.simage, "logo");
                        ActivityOptions activityOptions=ActivityOptions.makeSceneTransitionAnimation(activity,p);
                        context.startActivity(imageIntent,activityOptions.toBundle());
                        return super.onSingleTapConfirmed(e);
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        new AlertDialog.Builder(context)
                                .setTitle("Delete messages?")
                                .setNeutralButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setNegativeButton("DELETE FOR EVERYONE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseDatabase.getInstance().getReference()
                                        .child("Groupchat")
                                        .child(GroupId)
                                        .child("messages")
                                        .child(message.getMessageId()).child("message").setValue("This message was deleted");
                            }
                        }).show();
                        super.onLongPress(e);
                    }
                });
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    detector.onTouchEvent(event);
                    return true;
                }
            });
            FirebaseDatabase.getInstance().getReference()
                    .child("Users")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot dataSnapshot:snapshot.getChildren()) {
                                Users users=dataSnapshot.getValue(Users.class);
                                if (users.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                                    viewHolder.binding.sendusername.setText(users.getName());
                                    Glide.with(context).load(users.getProfileImage()).placeholder(R.drawable.ic_user).into(viewHolder.binding.senduserimage);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }else {
           RecieverViewHolder viewHolder=(RecieverViewHolder)holder;
            if (message.getMessage().equals("PHOTO")){
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(context).load(message.getImageUrl()).placeholder(R.drawable.imageplaceholder).into(viewHolder.binding.image);
            }else{
                viewHolder.binding.message.setText(message.getMessage());
            }

            if (message.getFelling()>=0){
                viewHolder.binding.feeling.setImageResource(reaction[message.getFelling()]);
                viewHolder.binding.feeling.setVisibility(View.VISIBLE);
            }else{
                viewHolder.binding.feeling.setVisibility(View.GONE);
            }
            viewHolder.binding.message.setOnTouchListener(new View.OnTouchListener() {
                GestureDetector.OnGestureListener listener;
                GestureDetector gestureDetector=new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        View view =new View(context);
                        popup.onTouch(view,e);
                        return super.onDoubleTap(e);
                    }
                });
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return true;
                }
            });
            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
                GestureDetector.OnGestureListener listener;
                GestureDetector gestureDetector=new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        View view =new View(context);
                        popup.onTouch(view,e);
                        return super.onDoubleTap(e);
                    }

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        Intent imageIntent=new Intent(activity, ImageViewAction.class);
                        imageIntent.putExtra("image",message.getImageUrl());
                        Pair[] p =new Pair[1];
                        p[0]=Pair.create(viewHolder.binding.image, "logo");
                        ActivityOptions activityOptions=ActivityOptions.makeSceneTransitionAnimation(activity,p);
                        context.startActivity(imageIntent,activityOptions.toBundle());
                        return super.onSingleTapConfirmed(e);
                    }
                });
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return true;
                }
            });

            FirebaseDatabase.getInstance().getReference()
                    .child("Groupchat")
                    .child(GroupId)
                    .child("messages")
                    .child(message.getMessageId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String senderid=(String)snapshot.child("senderId").getValue();
                    FirebaseDatabase.getInstance().getReference()
                            .child("Users")
                            .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot snapshot1:snapshot.getChildren()) {
                                Users users = snapshot1.getValue(Users.class);
                                if (users.getUid().equals(senderid)) {
                                    viewHolder.binding.recieveuserimageusername.setText(users.getName());
                                    Glide.with(context).load(users.getProfileImage()).placeholder(R.drawable.ic_user).into(viewHolder.binding.recieveuserimage);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class sentviewHolder extends RecyclerView.ViewHolder {
        GroupsendmessageBinding binding;

        public sentviewHolder(@NonNull View itemView) {
            super(itemView);
            binding = GroupsendmessageBinding.bind(itemView);
        }
    }

    public class RecieverViewHolder extends RecyclerView.ViewHolder {
        GrouprecievemessageBinding binding;

        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = GrouprecievemessageBinding.bind(itemView);
        }
    }
}
