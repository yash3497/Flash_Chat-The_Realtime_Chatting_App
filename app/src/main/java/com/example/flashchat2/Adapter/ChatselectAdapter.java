package com.example.flashchat2.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flashchat2.Interface.Chatselectlistener;
import com.example.flashchat2.Models.Selectchat;
import com.example.flashchat2.R;
import com.example.flashchat2.databinding.ItemChatselectBinding;

import java.nio.channels.Selector;
import java.util.ArrayList;

public class ChatselectAdapter extends RecyclerView.Adapter<ChatselectAdapter.viewholder>{
    Context context;
    private ArrayList<Selectchat> userlist;
    private Chatselectlistener chatselectlistener;

    public ChatselectAdapter(Context context, ArrayList<Selectchat> userlist, Chatselectlistener chatselectlistener) {
        this.context = context;
        this.userlist = userlist;
        this.chatselectlistener = chatselectlistener;
    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_chatselect,parent,false);
        return new viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewholder holder, int position) {
        Selectchat selectchat=userlist.get(position);
        holder.binding.chatusername.setText(selectchat.getUsername());
        holder.binding.lastmessage.setText(selectchat.getAbout());
        Glide.with(context).load(selectchat.getProfileimageurl()).placeholder(R.drawable.ic_user).into(holder.binding.circleImageView);
        if (selectchat.getSelected()){
            holder.binding.checkbox.setVisibility(View.VISIBLE);
        }else {
            holder.binding.checkbox.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectchat.getSelected()){
                    holder.binding.checkbox.setVisibility(View.GONE);
                    selectchat.setSelected(false);
                    if (getSelectedList().size()==0){
                        chatselectlistener.onChatSelectAction(false);
                    }
                }else {
                    holder.binding.checkbox.setVisibility(View.VISIBLE);
                    selectchat.setSelected(true);
                    chatselectlistener.onChatSelectAction(true);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userlist.size();
    }
    public ArrayList<Selectchat> getSelectedList(){
        ArrayList<Selectchat> selectchats=new ArrayList<>();
        for (Selectchat selected:userlist){
            if (selected.getSelected()){
                selectchats.add(selected);
            }
        }
        return selectchats;
    }

    public class viewholder extends RecyclerView.ViewHolder{
         ItemChatselectBinding binding;
        public viewholder(@NonNull View itemView) {
            super(itemView);
            binding=ItemChatselectBinding.bind(itemView);
        }
    }
}
