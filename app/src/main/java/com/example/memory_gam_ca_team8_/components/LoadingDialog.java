package com.example.memory_gam_ca_team8_.components;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.memory_gam_ca_team8_.R;
import com.example.memory_gam_ca_team8_.activities.GameActivity;
import com.example.memory_gam_ca_team8_.activities.MainActivity;
import com.example.memory_gam_ca_team8_.activities.RoomActivity;
import com.example.memory_gam_ca_team8_.activities.SignInActivity;
import com.example.memory_gam_ca_team8_.delegates.IDelegateDialog;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoadingDialog {

    Activity activity;
    AlertDialog dialog;
    MaterialButton btnPlay;
    TextView tvText;
    FirebaseDatabase database;
    MaterialButton btnQuit;
    private static final String websiteUrl = "https://memory-team8-ca-default-rtdb.asia-southeast1.firebasedatabase.app/";


    public LoadingDialog(Activity myActivity) {
        activity = myActivity;
        database = FirebaseDatabase.getInstance(websiteUrl);


    }

    public void startloadingAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_dialog, null));
        builder.setCancelable(true);

        dialog = builder.create();
        dialog.show();
    }

    public void startLoadingWinnerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_dialog_winner, null));
        builder.setCancelable(true);
        dialog = builder.create();
        dialog.show();
    }

    public void startLoadingLostDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_dialog_lost, null));
        builder.setCancelable(true);
        dialog = builder.create();
        dialog.show();
    }

    public void onClickPlay(String roomName) {

        btnPlay = dialog.findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = database.getReference("rooms/"+roomName);
                reference.setValue(null);
                Intent intent = new Intent(activity, RoomActivity.class);
                activity.startActivity(intent);


            }
        });
    }
    public void clickOnQuit(String roomName) {

        btnQuit = dialog.findViewById(R.id.btn_quit);
        btnQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = database.getReference("rooms/"+roomName);
                reference.setValue(null);
                activity.finishAffinity();
                System.exit(0);

            }
        });
    }


    public void dismissDialog() {
        dialog.dismiss();
    }
}
