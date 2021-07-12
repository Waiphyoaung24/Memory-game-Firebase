package com.example.memory_gam_ca_team8_.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.memory_gam_ca_team8_.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class SignInActivity extends AppCompatActivity {

    TextInputEditText etText;
    MaterialButton btnSignin;
    DatabaseReference playerRef;
    FirebaseDatabase database;
    String playerName = "";
    private static final String websiteUrl = "https://memory-team8-ca-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_room);
        initComponents();
        checkExistingUser();


        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerName = etText.getText().toString();
                etText.setText("");
                if (!playerName.equals("")) {
                    btnSignin.setText("Logging in");
                    btnSignin.setEnabled(false);
                    playerRef = database.getReference("players/" + playerName);
                    addEventListener();
                    playerRef.setValue("");
                }
            }
        });

    }


    public void initComponents() {
        btnSignin = findViewById(R.id.btn_signin);
        etText = findViewById(R.id.et_text);
        database = FirebaseDatabase.getInstance(websiteUrl);
    }

    public void addEventListener() {

        playerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!playerName.equals("")) {
                    SharedPreferences preferences = getSharedPreferences("Users", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("playerName", playerName);
                    editor.commit();

                    Intent intent = new Intent(SignInActivity.this, Main_menuActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                btnSignin.setText("Sign in");
                btnSignin.setEnabled(true);
                Toast.makeText(SignInActivity.this, "Error!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void checkExistingUser() {
        SharedPreferences pref = getSharedPreferences("Users", Context.MODE_PRIVATE);
        String username = pref.getString("playerName", "");
        if (!username.isEmpty()) {

            Intent intent = new Intent(SignInActivity.this, Main_menuActivity.class);
            startActivity(intent);
        }

    }
}

