package com.example.memory_gam_ca_team8_.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.GridLayout;


import com.example.memory_gam_ca_team8_.R;
import com.example.memory_gam_ca_team8_.domains.MemoryButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GameActivity extends AppCompatActivity implements View.OnClickListener{

    private int numberOfElements;
    private MemoryButton[] buttons;
    private int[] buttonGraphiclocations;
    private int[] buttonGraphics;

    private MemoryButton selectedButton;
    private MemoryButton selectedButton2;

    private boolean isBusy = false;

    ArrayList<String> images = new ArrayList<>();

    Map<Integer,String> imagesMap = new HashMap<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

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
        loadImages(images,numColumns,numRows,gridLayout);


    }
    protected void loadImages(ArrayList<String>images,int numColumns,int numRows,GridLayout gridLayout){
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
}