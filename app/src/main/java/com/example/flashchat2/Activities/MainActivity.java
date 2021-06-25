package com.example.flashchat2.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.flashchat2.Adapter.FragmentAdapter;
import com.example.flashchat2.Models.Users;
import com.example.flashchat2.R;
import com.example.flashchat2.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database=FirebaseDatabase.getInstance();
        binding.Viewpager.setAdapter(new FragmentAdapter(getSupportFragmentManager()));
        binding.Tablayout.setupWithViewPager(binding.Viewpager);

     //   binding.toolbar.inflateMenu(R.menu.mainmenu);
          setSupportActionBar(binding.toolbar);
          database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                  Users users=snapshot.getValue(Users.class);
                  Glide.with(getApplicationContext()).load(users.getProfileImage()).placeholder(R.drawable.ic_user).into(binding.userprofile);
                  binding.userprofile.setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View v) {
                          Intent intent=new Intent(getApplicationContext(),Profiledetails.class);
                          intent.putExtra("username",users.getName());
                          intent.putExtra("phoneno",users.getPhoneNumber());
                          intent.putExtra("about",users.getAbout());
                          intent.putExtra("image",users.getProfileImage());
                          intent.putExtra("uid",users.getUid());
                          Pair p[]=new Pair[1];
                          p[0]=Pair.create(binding.userprofile, "logo");
                          ActivityOptions activityOptions=ActivityOptions.makeSceneTransitionAnimation(MainActivity.this,p);
                          startActivity(intent,activityOptions.toBundle());
                      }
                  });
              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {

              }
          });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.group:
                Intent intent=new Intent(MainActivity.this,GroupSelectActivity.class);
                startActivity(intent);
                break;
            case R.id.setting:
                Intent setting=new Intent(MainActivity.this,SettingActivity.class);
                startActivity(setting);
                break;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                Intent intent2=new Intent(this,PhoneloginActivity.class);
                startActivity(intent2);
                finish();
                Toast.makeText(getApplicationContext(),"Logout",Toast.LENGTH_SHORT).show();
                break;
            case R.id.about:
                Intent intent1=new Intent(MainActivity.this,AboutActivity.class);
                startActivity(intent1);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu,menu);
        return true;
    }


}