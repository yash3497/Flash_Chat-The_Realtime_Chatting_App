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
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flashchat2.Activities.ImageViewAction;
import com.example.flashchat2.Activities.MainActivity;
import com.example.flashchat2.Models.Message;
import com.example.flashchat2.R;
import com.example.flashchat2.databinding.RecievemessageBinding;
import com.example.flashchat2.databinding.SendmessageBinding;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Objects;

public class MessagesAdapter extends RecyclerView.Adapter{

    Activity activity;
    Context context;
    ArrayList<Message> messages;
    final int send_message_count=1;
    final int receive_message_count=2;
    String senderRoom;
    String receiverRoom;

    public MessagesAdapter(Context context, ArrayList<Message> messages, String senderRoom, String receiverRoom,Activity activity) {
        this.context = context;
        this.messages = messages;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
        this.activity=activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==send_message_count){
            View view=LayoutInflater.from(context).inflate(R.layout.sendmessage,parent,false);
            return new sentviewHolder(view);
        }else {
            View view=LayoutInflater.from(context).inflate(R.layout.recievemessage,parent,false);
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
                    .child("chats")
                    .child(String.valueOf(senderRoom))
                    .child("messages")
                    .child(message.getMessageId()).setValue(message);
            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(String.valueOf(receiverRoom))
                    .child("messages")
                    .child(message.getMessageId()).setValue(message);
            return true; // true is closing popup, false is requesting a new selection
        });
        if (holder.getClass()==sentviewHolder.class){
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
                GestureDetector gestureDetector=new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        View view = new View(context);
                        popup.onTouch(view,e);
                        return super.onDoubleTap(e);
                    }
                });
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return false;
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
                        Pair p[]=new Pair[1];
                        p[0]=Pair.create(viewHolder.binding.simage, "logo");
                        ActivityOptions activityOptions=ActivityOptions.makeSceneTransitionAnimation(activity,p);
                        context.startActivity(imageIntent,activityOptions.toBundle());
                        return super.onSingleTapConfirmed(e);
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        new AlertDialog.Builder(context)
                                .setTitle("Delete messages?")
                                .setPositiveButton("Delete for me", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        FirebaseDatabase.getInstance().getReference()
                                                .child("chats")
                                                .child(String.valueOf(senderRoom))
                                                .child("messages")
                                                .child(message.getMessageId()).removeValue();
                                    }
                                }).setNeutralButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setNegativeButton("DELETE FOR EVERYONE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseDatabase.getInstance().getReference()
                                        .child("chats")
                                        .child(String.valueOf(senderRoom))
                                        .child("messages")
                                        .child(message.getMessageId()).child("message").setValue("This message was deleted");
                                FirebaseDatabase.getInstance().getReference()
                                        .child("chats")
                                        .child(String.valueOf(receiverRoom))
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
                viewHolder.binding.smessage.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        new AlertDialog.Builder(context)
                                .setTitle("Delete messages?")
                                .setPositiveButton("Delete for me", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        FirebaseDatabase.getInstance().getReference()
                                                .child("chats")
                                                .child(String.valueOf(senderRoom))
                                                .child("messages")
                                                .child(message.getMessageId()).removeValue();
                                    }
                                }).setNeutralButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setNegativeButton("DELETE FOR EVERYONE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseDatabase.getInstance().getReference()
                                        .child("chats")
                                        .child(String.valueOf(senderRoom))
                                        .child("messages")
                                        .child(message.getMessageId()).child("message").setValue("This message was deleted");
                                FirebaseDatabase.getInstance().getReference()
                                        .child("chats")
                                        .child(String.valueOf(receiverRoom))
                                        .child("messages")
                                        .child(message.getMessageId()).child("message").setValue("This message was deleted");
                            }
                        }).show();
                        return false;
                    }
                });
        }else {
            RecieverViewHolder viewHolder=(RecieverViewHolder)holder;
                    if (message.getMessage().equals("PHOTO")){
                        viewHolder.binding.image.setVisibility(View.VISIBLE);
                        viewHolder.binding.message.setVisibility(View.GONE);
                        Glide.with(context).load(message.getImageUrl()).placeholder(R.drawable.imageplaceholder).into(viewHolder.binding.image);
                        viewHolder.binding.image.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent imageIntent=new Intent(context, ImageViewAction.class);
                                imageIntent.putExtra("image",message.getImageUrl());
                                context.startActivity(imageIntent);
                            }
                        });
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
                final GestureDetector gestureDetector=new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        View view = new View(context);
                        popup.onTouch(view,e);
                        return super.onDoubleTap(e);
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        new AlertDialog.Builder(context)
                                .setTitle("Delete messages?")
                                .setPositiveButton("Delete for me", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        FirebaseDatabase.getInstance().getReference()
                                                .child("chats")
                                                .child(String.valueOf(senderRoom))
                                                .child("messages")
                                                .child(message.getMessageId()).removeValue();
                                    }
                                }).setNeutralButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                        super.onLongPress(e);
                    }
                });
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return true;
                }
            });
            viewHolder.binding.image.setOnTouchListener(new View.OnTouchListener() {
                final GestureDetector gestureDetector=new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        View view = new View(context);
                        popup.onTouch(view,e);
                        return super.onDoubleTap(e);
                    }

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        Intent imageIntent=new Intent(context, ImageViewAction.class);
                        imageIntent.putExtra("image",message.getImageUrl());
                        Pair p[]=new Pair[1];
                        p[0]=Pair.create(viewHolder.binding.image, "logo");
                        ActivityOptions activityOptions=ActivityOptions.makeSceneTransitionAnimation(activity,p);
                        context.startActivity(imageIntent,activityOptions.toBundle());
                        return super.onSingleTapConfirmed(e);
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        new AlertDialog.Builder(context)
                                .setTitle("Delete messages?")
                                .setPositiveButton("Delete for me", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        FirebaseDatabase.getInstance().getReference()
                                                .child("chats")
                                                .child(String.valueOf(senderRoom))
                                                .child("messages")
                                                .child(message.getMessageId()).removeValue();
                                    }
                                }).setNeutralButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                        super.onLongPress(e);
                    }
                });
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return true;
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class sentviewHolder extends RecyclerView.ViewHolder{

        SendmessageBinding binding;
        public sentviewHolder(@NonNull View itemView) {
            super(itemView);
            binding=SendmessageBinding.bind(itemView);
        }
    }
    public class RecieverViewHolder extends RecyclerView.ViewHolder{

        RecievemessageBinding binding;
        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding=RecievemessageBinding.bind(itemView);
        }
    }

}
