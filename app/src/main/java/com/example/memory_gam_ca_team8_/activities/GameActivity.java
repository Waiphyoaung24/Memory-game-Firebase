package com.example.memory_gam_ca_team8_.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.example.memory_gam_ca_team8_.R;
import com.example.memory_gam_ca_team8_.components.LoadingDialog;
import com.example.memory_gam_ca_team8_.domains.MemoryButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class GameActivity extends AppCompatActivity implements View.OnClickListener{

    private int numberOfElements;
    private MemoryButton[] buttons;
    private int[] buttonGraphiclocations;
    private int[] buttonGraphics;

    private MemoryButton selectedButton;
    private MemoryButton selectedButton2;
    FirebaseDatabase database;
    private static final String websiteUrl = "https://memory-team8-ca-default-rtdb.asia-southeast1.firebasedatabase.app/";

    private boolean isBusy = false;
    private boolean flag;

    ArrayList<String> images = new ArrayList<>();

    Map<Integer,String> imagesMap = new HashMap<>();

    private TextView timerTV;
    private CountDownTimer timer;
    MediaPlayer mediaPlayer;
    private TextView scoreTV;
    int score = 0;
    String playerType = "";
    String roomName = "";
    int gameType ;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        database = FirebaseDatabase.getInstance(websiteUrl);


        GridLayout gridLayout = (GridLayout) findViewById(R.id.grid_layout_4x3);

        int numColumns = gridLayout.getColumnCount();
        int numRows = gridLayout.getRowCount();

        numberOfElements = numColumns * numRows;

        //setting the array size to store the buttons - 12 in this
        buttons = new MemoryButton[numberOfElements];

        //storing the 6 images
        buttonGraphics = new int[numberOfElements/2];

//        buttonGraphics[0] = R.drawable.camel;
//        buttonGraphics[1] = R.drawable.coala;
//        buttonGraphics[2] = R.drawable.fox;
//        buttonGraphics[3] = R.drawable.lion;
//        buttonGraphics[4] = R.drawable.monkey;
//        buttonGraphics[5] = R.drawable.wolf;

        buttonGraphics[0] = 0;
        buttonGraphics[1] = 1;
        buttonGraphics[2] = 2;
        buttonGraphics[3] = 3;
        buttonGraphics[4] = 4;
        buttonGraphics[5] = 5;

        buttonGraphiclocations = new int[numberOfElements];

        shuffleButtonGraphics();

        Intent intent = getIntent();
        images = intent.getStringArrayListExtra("image");
         playerType = intent.getStringExtra("role");
         roomName = intent.getStringExtra("roomName");

        for(int i = 0; i < images.size();i++)
        {
            imagesMap.put(i,images.get(i));
        }

        for(int r =0; r< numRows; r++)
        {
            for(int c =0; c< numColumns; c++)
            {
                MemoryButton tempButton = new MemoryButton(this, r,c,buttonGraphics[buttonGraphiclocations[r * numColumns + c]],imagesMap);
                tempButton.setId(View.generateViewId());
                tempButton.setOnClickListener(this);
                buttons[r * numColumns +c] = tempButton;
                gridLayout.addView(tempButton);
            }
        }
        timerTV= (TextView) findViewById(R.id.timerTextView);
        CountUpTimer timer = new CountUpTimer(90000) {
            public void onTick(int l) {
                timerTV.setText(String.valueOf(l) + "s");
            }
        };
        timer.start();

        mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.gametheme);
        mediaPlayer.start();

    }

    public abstract class CountUpTimer extends CountDownTimer {
        private static final long INTERVAL_MS = 1000;
        private final long duration;

        protected CountUpTimer(long durationMs) {
            super(durationMs, INTERVAL_MS);
            this.duration = durationMs;
        }

        public abstract void onTick(int second);

        @Override
        public void onTick(long msUntilFinished) {
            int second = (int) ((duration - msUntilFinished) / 1000);
            onTick(second);
        }

        @Override
        public void onFinish() {
            onTick(duration / 1000);
        }
    }

    protected void shuffleButtonGraphics(){

        Random rand = new Random();

        for(int i =0; i< numberOfElements; i++)
        {
            buttonGraphiclocations[i] = i % (numberOfElements/2);
        }

        for(int i =0; i< numberOfElements; i++)
        {
            int temp = buttonGraphiclocations[i];
            int swapIndex = rand.nextInt(12);
            buttonGraphiclocations[i] = buttonGraphiclocations[swapIndex];
            buttonGraphiclocations[swapIndex] = temp;

        }
    }

    @Override
    public void onClick(View v) {
        if(isBusy){
            return;
        }
        MemoryButton button = (MemoryButton) v;
        //i think this is to prevent matched item to react if user accidently selected it
        if(button.isMatched)
            return;
        //first image selected or first image selected after a match is completed
        if(selectedButton == null){
            selectedButton = button;
            selectedButton.flip();
            return;
        }
        //if user select the same image. Do nothing
        if(selectedButton.getId() == button.getId()){
            return;
        }
        //if previously selected photo is the same current selected photo
        if(selectedButton.getFrontDrawableId() == button.getFrontDrawableId()){
            button.flip();
            button.setMatched(true);
            selectedButton.setMatched(true);
            selectedButton.setEnabled(false);
            button.setEnabled(false);
            score+=1;
            scoreTV = (TextView) findViewById(R.id.score);
            scoreTV.setText(String.valueOf("Score " +score));
            if(checkForRoomOrSingleGame() == 0){
            endGame();}
            else {
               flag = chooseWinner();
                if(flag){
                    alertWinner();
                }

            }
            //reset the select button tracker
            selectedButton = null;

            return;

        } else{
            //if photo selected doesn't match
            selectedButton2 = button;
            selectedButton2.flip();
            isBusy = true;

            final Handler handler = new Handler();

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    selectedButton2.flip();
                    selectedButton.flip();
                    selectedButton = null;
                    selectedButton2 = null;
                    isBusy = false;
                }
            },500);
        }

    }

    public void endGame(){
        if(score == 6){


            mediaPlayer.stop();
            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.wintheme);
            mediaPlayer.start();

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GameActivity.this);
            alertDialogBuilder
                    .setMessage("Game is over!" )
                    .setCancelable(false)
                    .setPositiveButton("NEW GAME", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(GameActivity.this, MainActivity.class);
                            startActivity(intent);

                        }
                    })
                    .setNegativeButton("STOP THE GAME", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            finish();

                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    private boolean chooseWinner(){

        if(score == 6){
            DatabaseReference myRef = database.getReference("rooms/"+roomName+"/"+playerType+"/Winner");
            myRef.setValue("True");
            return true;
        }
        return false;

    }
    public int checkForRoomOrSingleGame() {
        SharedPreferences preferences = getSharedPreferences("room", Context.MODE_PRIVATE);
        int flag = preferences.getInt("multiplayer", 0);
        return flag;

    }
    public void alertWinner() {
        DatabaseReference myRef = database.getReference("rooms/" +roomName+ "/"+ playerType+"/Winner");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                if(snapshot.exists()){
                    LoadingDialog dialog = new LoadingDialog(GameActivity.this);
                    dialog.startLoadingWinnerDialog();
                    dialog.onClickPlay();


                } else {

                    LoadingDialog dialog = new LoadingDialog(GameActivity.this);
                    dialog.startLoadingLostDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}