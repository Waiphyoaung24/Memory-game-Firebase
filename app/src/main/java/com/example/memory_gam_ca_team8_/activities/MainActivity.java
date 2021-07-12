package com.example.memory_gam_ca_team8_.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.memory_gam_ca_team8_.R;
import com.example.memory_gam_ca_team8_.adapters.ImageAdapter;
import com.example.memory_gam_ca_team8_.components.LoadingDialog;
import com.example.memory_gam_ca_team8_.domains.Image;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private EditText etUrl;
    private Button btnFetch;
    private GridView gvImg;
    private ProgressBar pbHor;
    private TextView tvPb;
    private Handler mHandler;
    private List<Image> imgList;
    private ArrayList<String> prepareImages;
    private ImageAdapter adapter;
    private int selNum = 0;
    private Button btnTo;
    String playerName = "";
    FirebaseDatabase database;
    private static final String websiteUrl = "https://memory-team8-ca-default-rtdb.asia-southeast1.firebasedatabase.app/";
    LoadingDialog dialog = new LoadingDialog(MainActivity.this);


    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponents();
        initData();

        btnTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        imgList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            imgList.add(new Image());
        }
        // initiate image adapter
        adapter = new ImageAdapter(this, imgList, pbHor, tvPb);
        gvImg.setAdapter(adapter);

        mHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                adapter.notifyDataSetChanged();
            }
        };

        // set onclick listener for gridview
        gvImg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // 如果当前是已选中，就取消，如果是没选中，就选择
                //if selected, cancel it, verse versa
                if (imgList.get(position).isSel()) {
                    imgList.get(position).setSel(false);
                    selNum--;
                    if (selNum < 6) {
                        // 如果选择图片小于6个，设置不可见
                        //if the selection less than 6, the button is set as invisible
                        btnTo.setVisibility(View.GONE);
                    }
                } else {
                    if (selNum >= 6) {
                        Toast.makeText(MainActivity.this, "You can only choose up to 6", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    imgList.get(position).setSel(true);
                    selNum++;
                    if (selNum >= 6) {
                        // if selection >=6, set the button visible
                        btnTo.setVisibility(View.VISIBLE);
                        btnTo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                prepareImages();
                                if (checkForRoomOrSingleGame() == 0) {

                                    Intent intent = new Intent(MainActivity.this, GameActivity.class);
                                    intent.putStringArrayListExtra("image", prepareImages);
                                    startActivity(intent);
                                } else {
                                    DatabaseReference myRef = database.getReference("rooms/"+playerName+"'s Room/"+"photos/");
                                    myRef.setValue(prepareImages);
                                    DatabaseReference reference = database.getReference("rooms/" + playerName + "'s Room/" + "player1");
                                    reference.setValue(playerName);
                                    waitingforPlayer2();

                                }
                            }
                        });
                    }
                }
                // 刷新界面
                //refresh the view
                adapter.notifyDataSetChanged();
            }
        });

    }

    private void initData() {

        // add click event
        btnFetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adapter.getBkgdThread()!=null) {
                    adapter.getBkgdThread().interrupt();
                }
                // get url
                String url = etUrl.getText().toString();
                if ("".equals(url.trim())) {
                    Toast.makeText(MainActivity.this, "URL is invalid", Toast.LENGTH_SHORT).show();
                    return;
                }

                //everytime the fetch button clicked, the imglist,progress bar would be reset
                for (int i = 0; i < 20; i++) {
                    imgList.get(i).setImgSrc(null);
                }
                adapter.notifyDataSetChanged();

                // rst progress bar
                pbHor.setProgress(0);
                // reset progress bar text
                tvPb.setText("Downloading 0 of 20 images");

                // new thread, request http
                new Thread(() -> {
                    try {
                        int index = 0;

                        // get http response
                        Document document = Jsoup.connect(url).timeout(10000).get();
                        // get all <img> tags from http response
                        Elements elements = document.select("img");
                        for (Element element : elements) {
                            // determine whether the img tag is correct or not
                            String imgSrc = element.attr("src");

                            // determine whether the image is .jpg file
                            if (imgSrc.contains(".jpg") || imgSrc.contains(".png")) {
                                // get the first 20 images
                                if (index >= 20) {
                                    break;
                                }
                                imgList.get(index).setImgSrc(imgSrc);
                                index++;
                            }
                        }

                        // 通知图片
                        Message msg = new Message();
                        msg.obj = imgList;
                        mHandler.sendMessage(msg);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();

            }
        });
    }

    /**
     * initiate the ui
     */
    private void initComponents() {

        etUrl = findViewById(R.id.et_url);
        btnFetch = findViewById(R.id.btn_fetch);
        gvImg = findViewById(R.id.gv_img);
        pbHor = findViewById(R.id.pb_hor);
        tvPb = findViewById(R.id.tv_pb);
        btnTo = findViewById(R.id.btn_to);
        database = FirebaseDatabase.getInstance(websiteUrl);
        SharedPreferences preferences = getSharedPreferences("Users", Context.MODE_PRIVATE);
        playerName = preferences.getString("playerName","");


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void prepareImages() {
        prepareImages = new ArrayList<>();
        List<Image> images = imgList.stream().filter(Image::isSel).collect(Collectors.toList());
        for (Image i : images) {
            prepareImages.add(i.getImgSrc());
        }
    }

    public int checkForRoomOrSingleGame() {
        SharedPreferences preferences = getSharedPreferences("room", Context.MODE_PRIVATE);
        int flag = preferences.getInt("multiplayer", 0);
        return flag;

    }
    public void waitingforPlayer2(){
        DatabaseReference myRef = database.getReference("rooms/"+playerName+"'s Room/"+"player2");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    dialog.dismissDialog();
                    Log.e("player arrived","player arrived");
                    Intent intent = new Intent(getApplicationContext(),GameActivity.class);
                    intent.putStringArrayListExtra("image", prepareImages);
                    intent.putExtra("role","Player1");
                    intent.putExtra("roomName",playerName+"'s Room");
                    startActivity(intent);

                } else {

                    dialog.startloadingAlertDialog();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}