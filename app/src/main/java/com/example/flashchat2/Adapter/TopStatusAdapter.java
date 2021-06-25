package com.example.flashchat2.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.flashchat2.Models.Status;
import com.example.flashchat2.Models.UsersStatus;
import com.example.flashchat2.R;
import com.example.flashchat2.databinding.StatusviewBinding;
import com.example.flashchat2.fragment.StoriesFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class TopStatusAdapter extends RecyclerView.Adapter<TopStatusAdapter.TopStatusViewHolder>{

    Context context;
    ArrayList<UsersStatus> usersStatuses;

    public TopStatusAdapter(Context context, ArrayList<UsersStatus> usersStatuses) {
        this.context = context;
        this.usersStatuses = usersStatuses;
    }

    @NonNull
    @Override
    public TopStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(context).inflate(R.layout.statusview,parent,false);
        return new TopStatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopStatusViewHolder holder, int position) {
             UsersStatus usersStatus=usersStatuses.get(position);
        if (usersStatus.getStatuses().size()!=0) {
             Long time = usersStatus.getLastUpdated();
             SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
             SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
             String timestamp = simpleDateFormat.format(new Date(time));
             Date c = Calendar.getInstance().getTime();
             SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
             String formattedDate = df.format(c);
             Log.d("current", formattedDate);
             Log.d("time", timestamp);
             String updatedDay;
             if (formattedDate.equals(timestamp)) {
                 updatedDay = "Today";
             } else {
                 updatedDay = "Yesterday";
             }
             holder.binding.storyusername.setText(usersStatus.getName());
             holder.binding.storyupdate.setText(updatedDay + ", " + dateFormat.format(new Date(time)));
             Status laststatus = usersStatus.getStatuses().get(usersStatus.getStatuses().size() - 1);
             Glide.with(context).load(laststatus.getImageUrl()).into(holder.binding.circleImageView);
             holder.binding.circularStatusView.setPortionsCount(usersStatus.getStatuses().size());
             holder.itemView.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     ArrayList<MyStory> myStories = new ArrayList<>();
                     for (Status status : usersStatus.getStatuses()) {
                         myStories.add(new MyStory(status.getImageUrl()));
                     }
                     new StoryView.Builder(((AppCompatActivity) context).getSupportFragmentManager())
                             .setStoriesList(myStories) // Required
                             .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                             .setTitleText(usersStatus.getName()) // Default is Hidden
                             .setSubtitleText("") // Default is Hidden
                             .setTitleLogoUrl(usersStatus.getProfileImage()) // Default is Hidden
                             .setStoryClickListeners(new StoryClickListeners() {
                                 @Override
                                 public void onDescriptionClickListener(int position) {
                                     //your action
                                 }

                                 @Override
                                 public void onTitleIconClickListener(int position) {
                                     //your action
                                 }
                             }) // Optional Listeners
                             .build() // Must be called before calling show method
                             .show();
                 }
             });
         }
    }

    @Override
    public int getItemCount() {
        return usersStatuses.size();
    }

    public class TopStatusViewHolder extends RecyclerView.ViewHolder{
        StatusviewBinding binding;
        public TopStatusViewHolder(@NonNull View itemView) {
            super(itemView);
            binding=StatusviewBinding.bind(itemView);
        }
    }
}
