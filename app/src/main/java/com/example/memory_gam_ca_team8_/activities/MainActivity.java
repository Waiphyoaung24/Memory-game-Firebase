package com.example.memory_gam_ca_team8_.activities;

import androidx.appcompat.app.AppCompatActivity;

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
        // 设置数据适配器
        adapter = new ImageAdapter(this, imgList, pbHor, tvPb);
        gvImg.setAdapter(adapter);

        mHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                adapter.notifyDataSetChanged();
            }
        };

        // 设置点击图片的事件
        gvImg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // 如果当前是已选中，就取消，如果是没选中，就选择
                if (imgList.get(position).isSel()) {
                    imgList.get(position).setSel(false);
                    selNum--;
                    if (selNum < 6) {
                        // 如果选择图片小于6个，设置不可见
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
                        // 如果选择图片大于6个，设置可见
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
                                    waitingforPlayer2();

                                }
                            }
                        });
                    }
                }
                // 刷新界面
                adapter.notifyDataSetChanged();
            }
        });

    }

    private void initData() {

        // 添加点击事件
        btnFetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 获取输入的图片地址链接
                String url = etUrl.getText().toString();
                if ("".equals(url.trim())) {
                    Toast.makeText(MainActivity.this, "URL is invalid", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 每次点击Fetch按钮后，图片需要还原
                for (int i = 0; i < 20; i++) {
                    imgList.get(i).setImgSrc(null);
                }
                adapter.notifyDataSetChanged();

                // 还原进度条
                pbHor.setProgress(0);
                // 设置文本
                tvPb.setText("Downloading 0 of 20 images");

                // 开启子线程，访问网页
                new Thread(() -> {
                    try {
                        int index = 0;

                        // 获取网页源码
                        Document document = Jsoup.connect(url).timeout(10000).get();
                        // 获取所有图片标签
                        Elements elements = document.select("img");
                        for (Element element : elements) {
                            // 需要判断图片标签是否是正确的图片
                            String imgSrc = element.attr("src");

                            // 判断后缀
                            if (imgSrc.contains(".jpg") || imgSrc.contains(".png")) {
                                // 只需要前20张
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
     * 初始化页面控件
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