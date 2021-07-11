package com.example.memory_gam_ca_team8_.components;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.memory_gam_ca_team8_.R;
import com.example.memory_gam_ca_team8_.activities.RoomActivity;
import com.google.android.material.button.MaterialButton;

public class LoadingDialog {

    Activity activity;
    AlertDialog dialog;
    MaterialButton btnPlay;
    TextView tvText;

    public LoadingDialog(Activity myActivity){
        activity = myActivity;
       

    }
    public void startloadingAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_dialog,null));
        builder.setCancelable(true);

        dialog = builder.create();
        dialog.show();
    }
    public void startLoadingWinnerDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_dialog_winner,null));
        builder.setCancelable(true);
        dialog = builder.create();
        dialog.show();
    }
    public void startLoadingLostDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_dialog_lost,null));
        builder.setCancelable(true);
        dialog = builder.create();
        dialog.show();
    }
    public void onClickPlay(View view){
     btnPlay =   dialog.findViewById(R.id.btnPlay);
     btnPlay.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             Toast.makeText(activity, "You go back to activity haha", Toast.LENGTH_SHORT).show();
         }
     });
    }
    public void dismissDialog(){
        dialog.dismiss();
    }
}
