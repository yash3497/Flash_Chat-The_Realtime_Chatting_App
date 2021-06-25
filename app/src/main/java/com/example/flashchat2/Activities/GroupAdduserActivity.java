package com.example.flashchat2.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.View;

import com.example.flashchat2.Adapter.ChatselectAdapter;
import com.example.flashchat2.Interface.Chatselectlistener;
import com.example.flashchat2.Models.GroupDetails;
import com.example.flashchat2.Models.Selectchat;
import com.example.flashchat2.Utils.CountryToPhonePrefix;
import com.example.flashchat2.databinding.ActivityGroupAdduserBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static java.security.AccessController.getContext;

public class GroupAdduserActivity extends AppCompatActivity implements Chatselectlistener {
    ActivityGroupAdduserBinding binding;
    FirebaseDatabase database;
    ChatselectAdapter chatselectAdapter;
    ArrayList<Selectchat> contactlist,userlist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityGroupAdduserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        String groupId=getIntent().getStringExtra("groupid");
        String groupname=getIntent().getStringExtra("groupname");
        String groupimage=getIntent().getStringExtra("groupimage");
        String groupabout=getIntent().getStringExtra("groupabout");
        database=FirebaseDatabase.getInstance();
        contactlist=new ArrayList<>();
        userlist=new ArrayList<>();
        binding.selectrecyclerview.setNestedScrollingEnabled(false);
        binding.selectrecyclerview.setHasFixedSize(false);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        binding.selectrecyclerview.setLayoutManager(layoutManager);
        chatselectAdapter = new ChatselectAdapter(this,userlist,this);
        binding.selectrecyclerview.setAdapter(chatselectAdapter);
        getContactList();
        binding.toolbar4.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(GroupAdduserActivity.this,GroupChatActivity.class);
                startActivity(intent);
                finishAffinity();
            }
        });
        binding.selectbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<Selectchat> selectchats=chatselectAdapter.getSelectedList();
                Date date = new Date();
                for (int i=0;i<selectchats.size();i++) {
                    String userUid = selectchats.get(i).getUid();
                    GroupDetails ParticipantsDetails = new GroupDetails();
                    ParticipantsDetails.setUid(userUid);
                    ParticipantsDetails.setTimestamp(date.getTime() + "");
                    ParticipantsDetails.setRole("");
                    database.getReference().child("Group").child(groupId)
                            .child("Participants").child(userUid)
                            .setValue(ParticipantsDetails).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                          Intent intent=new Intent(GroupAdduserActivity.this,GroupChatActivity.class);
                          intent.putExtra("groupid",groupId);
                          intent.putExtra("groupname",groupname);
                          intent.putExtra("groupimage",groupimage);
                          intent.putExtra("groupabout",groupabout);
                          startActivity(intent);
                        }
                    });
                }
            }
        });
    }
    private void getContactList(){
        String ISOPrefix = getCountryISO();
        ContentResolver cr = this.getContentResolver();
        Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        try {
            contactlist.clear();
            userlist.clear();
            while (phones.moveToNext()) {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                phone = phone.replace(" ", "");
                phone = phone.replace("-", "");
                phone = phone.replace("(", "");
                phone = phone.replace(")", "");

                if (!String.valueOf(phone.charAt(0)).equals("+"))
                    phone = ISOPrefix + phone;
                Selectchat users = new Selectchat();
                users.setUsername(name);
                users.setPhoneNumber(phone);
                contactlist.add(users);
                chatselectAdapter.notifyDataSetChanged();
                getUserDetails(users);
            }
            Set<Selectchat> set = new HashSet<>(contactlist);
            contactlist.clear();
            contactlist.addAll(set);
        } catch (Exception e) {
            e.printStackTrace();
        }
        phones.close();
    }
    private void getUserDetails(Selectchat users) {
        DatabaseReference mUserDB = FirebaseDatabase.getInstance().getReference().child("Users");
        Query query = mUserDB.orderByChild("phoneNumber").equalTo(users.getPhoneNumber());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()){
                    String  phone = "",
                            name = "";
                    //userlist.clear();
                    try {
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            if (childSnapshot.child("phoneNumber").getValue() != null)
                                phone = childSnapshot.child("phoneNumber").getValue().toString();
                            if (childSnapshot.child("name").getValue() != null) {
                                name = childSnapshot.child("name").getValue().toString();
                            }
                            String image = childSnapshot.child("profileImage").getValue().toString();
                            String about= childSnapshot.child("about").getValue().toString();

                            Selectchat mUser = new Selectchat();
                            mUser.setUid(childSnapshot.getKey());
                            mUser.setUsername(name);
                            mUser.setPhoneNumber(phone);
                            mUser.setProfileimageurl(image);
                            mUser.setAbout(about);
                            if (name.equals(phone))
                                for (Selectchat mContactIterator : contactlist) {
                                    if (mContactIterator.getPhoneNumber().equals(mUser.getPhoneNumber())) {
                                        mUser.setUsername(mContactIterator.getUsername());
                                    }
                                }
                            if (!phone.equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()))
                                userlist.add(mUser);
                            chatselectAdapter.notifyDataSetChanged();
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private String getCountryISO(){
        String iso = null;

        getContext();
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if(telephonyManager.getNetworkCountryIso()!=null)
            if (!telephonyManager.getNetworkCountryIso().toString().equals(""))
                iso = telephonyManager.getNetworkCountryIso().toString();

        return CountryToPhonePrefix.getPhone(iso);
    }

    @Override
    public void onChatSelectAction(Boolean isSelected) {

    }
}