package com.example.flashchat2.Adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.flashchat2.fragment.ChatFragment;
import com.example.flashchat2.fragment.GroupFragment;
import com.example.flashchat2.fragment.MemesFragment;
import com.example.flashchat2.fragment.StoriesFragment;

public class FragmentAdapter extends FragmentPagerAdapter {

    public FragmentAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                ChatFragment chatFragmnet=new ChatFragment();
                return chatFragmnet;
            case 1:
                StoriesFragment storiesfragment=new StoriesFragment();
                return storiesfragment;
            case 2:
                GroupFragment groupFragment=new GroupFragment();
                return groupFragment;
            case 3:
                MemesFragment memeFragment=new MemesFragment();
                return memeFragment;
            default:return new ChatFragment();
        }
    }

    @Override
    public int getCount() {
        return 4;
    }
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String title=null;
        if(position==0){
            title="CHATS";
        }else if(position==1){
            title="STORIES";
        }else if(position==2){
            title="GROUP";
        }else if (position==3){
            title="MEMES";
        }
        return title;
    }
}