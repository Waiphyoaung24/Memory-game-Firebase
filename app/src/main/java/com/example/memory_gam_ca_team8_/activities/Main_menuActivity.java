package com.example.memory_gam_ca_team8_.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;


import com.example.memory_gam_ca_team8_.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Main_menuActivity extends AppCompatActivity {

    MaterialButton btnPlay;
    MaterialButton btnHost;
    MaterialButton btnSignOut;
    FirebaseDatabase database;
    private static final String websiteUrl = "https://memory-team8-ca-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        initComponents();

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref =  getSharedPreferences("room",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("multiplayer",0);
                editor.apply();
                Intent intent = new Intent(Main_menuActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences("Users", Context.MODE_PRIVATE);
                String username = preferences.getString("playerName","");
                removeUserfromFirebase(username);
                SharedPreferences.Editor editor = preferences.edit();

                editor.remove("playerName");
                editor.apply();
                finish();
                Intent intent = new Intent(getApplicationContext(),SignInActivity.class);
                startActivity(intent);
            }
        });

        btnHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),RoomActivity.class);
                startActivity(intent);
            }
        });
    }



    public void initComponents(){
        btnPlay = findViewById(R.id.btn_play);
        btnHost = findViewById(R.id.btn_host);
        btnSignOut = findViewById(R.id.btn_signout);
        database = FirebaseDatabase.getInstance(websiteUrl);

    }
    public void removeUserfromFirebase(String username){
        DatabaseReference ref = database.getReference("players/"+username);
        ref.removeValue();

    }
}