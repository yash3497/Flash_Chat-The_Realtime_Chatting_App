package com.example.flashchat2.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Toast;

import com.example.flashchat2.Adapter.ContactAdapter;
import com.example.flashchat2.Adapter.UsersAdapter;
import com.example.flashchat2.Models.Users;
import com.example.flashchat2.Permission.permission;
import com.example.flashchat2.Utils.CountryToPhonePrefix;
import com.example.flashchat2.databinding.ActivityContactBinding;
import com.example.flashchat2.databinding.FragmentChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static java.security.AccessController.getContext;

public class ContactActivity extends AppCompatActivity {
    ActivityContactBinding binding;
    FirebaseDatabase database;
    ContactAdapter muserlistAdapter;
    ArrayList<Users> contactlist,userlist;
    private permission permission;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        database = FirebaseDatabase.getInstance();
        contactlist=new ArrayList<>();
        userlist=new ArrayList<>();
        permission=new permission();
        initializeRecyclerView();
        getContactList();
    }
    private void getContactList(){

        if (permission.isContactOk(this)) {
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
                    Users users = new Users();
                    users.setName(name);
                    users.setPhoneNumber(phone);
                    contactlist.add(users);
                    muserlistAdapter.notifyDataSetChanged();
                    getUserDetails(users);
                }
                Set<Users> set = new HashSet<>(contactlist);
                contactlist.clear();
                contactlist.addAll(set);
            } catch (Exception e) {
                e.printStackTrace();
            }
            phones.close();
        }else permission.requestContact(this);
    }
    private void getUserDetails(Users users) {
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

                            Users mUser = new Users();
                            mUser.setUid(childSnapshot.getKey());
                            mUser.setName(name);
                            mUser.setPhoneNumber(phone);
                            mUser.setProfileImage(image);
                            mUser.setAbout(about);
                            if (name.equals(phone))
                                for (Users mContactIterator : contactlist) {
                                    if (mContactIterator.getPhoneNumber().equals(mUser.getPhoneNumber())) {
                                        mUser.setName(mContactIterator.getName());
                                    }
                                }
                            if (!phone.equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber()))
                                userlist.add(mUser);
                            muserlistAdapter.notifyDataSetChanged();
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
    private void initializeRecyclerView() {
        binding.contactrecylerview.setNestedScrollingEnabled(false);
        binding.contactrecylerview.setHasFixedSize(false);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        binding.contactrecylerview.setLayoutManager(layoutManager);
        muserlistAdapter = new ContactAdapter(this,userlist);
        binding.contactrecylerview.setAdapter(muserlistAdapter);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 2000:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContactList();
                } else
                    Toast.makeText(getApplicationContext(), "Contact Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}