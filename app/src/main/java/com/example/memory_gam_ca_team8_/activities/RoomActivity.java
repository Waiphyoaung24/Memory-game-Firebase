package com.example.memory_gam_ca_team8_.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.memory_gam_ca_team8_.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RoomActivity extends AppCompatActivity {

    ListView listView;
    MaterialButton btnCreate;

    List<String> roomsList;

    String playerName = "";
    String roomName = "";

    FirebaseDatabase database;
    DatabaseReference roomRef;
    DatabaseReference roomsRef;
    private static final String websiteUrl = "https://memory-team8-ca-default-rtdb.asia-southeast1.firebasedatabase.app/";
    ArrayList<String> prepareImages;
    boolean flag = false;
    boolean fullRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        initComponents();
        initRoom();
        prepareImages = new ArrayList<>();


    }

    public void initComponents() {
        database = FirebaseDatabase.getInstance(websiteUrl);
        listView = findViewById(R.id.lv_rooms);
        btnCreate = findViewById(R.id.btn_create);
    }

    public void initRoom() {
        SharedPreferences preferences = getSharedPreferences("Users", Context.MODE_PRIVATE);
        playerName = preferences.getString("playerName", "");
        roomName = playerName;
        roomsList = new ArrayList<>();

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomName = playerName;
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("roomName", roomName);
                startActivity(intent);


            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                roomName = roomsList.get(position);

              isRoomFull();


            }
        });

        addRoomsEventListener();
    }




    private void addRoomsEventListener() {
        roomsRef = database.getReference("rooms");
        roomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                roomsList.clear();
                Iterable<DataSnapshot> rooms = snapshot.getChildren();
                for (DataSnapshot room : rooms) {
                    roomsList.add(room.getKey());

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(RoomActivity.this, android.R.layout.simple_list_item_1, roomsList);
                    listView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void retrievePhotos(String data) {
        prepareImages = new ArrayList<>();
        DatabaseReference myRef = database.getReference("rooms/" + data + "/photos");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                prepareImages.clear();
                for (int i = 0; i < 6; i++) {
                    prepareImages.add(snapshot.child(String.valueOf(i)).getValue(String.class));

                }
                if (flag == true) {
                    sendGameActivity(prepareImages);
                    flag = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    public void sendGameActivity(ArrayList<String> list) {
        Intent intent = new Intent(getApplicationContext(), GameActivity.class);
        intent.putStringArrayListExtra("image", list);
        intent.putExtra("role", "Player2");
        intent.putExtra("roomName", roomName);
        startActivity(intent);
    }

    public boolean isRoomFull() {
        DatabaseReference myRef = database.getReference("rooms/"+roomName+"/"+"player2/");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(RoomActivity.this, "room occupied", Toast.LENGTH_SHORT).show();
                }else {
                    roomRef = database.getReference("rooms/" + roomName + "/player2");
                    roomRef.setValue(playerName);
                    flag = true;
                    retrievePhotos(roomName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
      return fullRoom;
    }


}