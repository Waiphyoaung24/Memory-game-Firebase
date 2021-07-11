package com.example.memory_gam_ca_team8_.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.memory_gam_ca_team8_.R;
import com.example.memory_gam_ca_team8_.domains.Image;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class ImageAdapter extends BaseAdapter {

    private Context mContext;
    private List<Image> images;

    // 进度条
    private ProgressBar pbHor;
    // 进度文本
    private TextView tvPb;

    // 全局图片下载记录
    private int index = 0;
    // 记录加载的图片链接
    private Set<String> urls = new HashSet<>();

    @Override
    public void notifyDataSetChanged() {
        index = 0;
        urls.clear();
        super.notifyDataSetChanged();
    }

    public ImageAdapter(Context context, List<Image> images, ProgressBar pbHor, TextView tvPb) {
        mContext = context;
        this.images = images;

        this.pbHor = pbHor;
        this.tvPb = tvPb;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.img_item, null);
            viewHolder = new ViewHolder();
            viewHolder.photos = convertView.findViewById(R.id.iv_photo);
            viewHolder.check = convertView.findViewById(R.id.iv_sel);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Image item = (Image) getItem(position);
        if (item.getImgSrc() != null) {
            // 同步下载图片
            ViewHolder finalViewHolder = viewHolder;
            new Thread() {
                @Override
                public void run() {
                    super.run();

                    Bitmap bitmap = null;
                    String imgSrc = ((Image) getItem(position)).getImgSrc();
                    try {
                        bitmap = Glide.with(mContext)
                                .asBitmap()
                                .load(imgSrc)
                                .submit()
                                .get();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!urls.contains(imgSrc)) {
                        urls.add(imgSrc);
                        index += 1;
                    }
                    // 主线程更新界面
                    Bitmap finalBitmap = bitmap;
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finalViewHolder.photos.setImageBitmap(finalBitmap);
                            // 下载完一张，就需要设置进度条
                            pbHor.setProgress((index) * 5);
                            // 设置文本
                            tvPb.setText("Downloading " + (index) + " of 20 images");
                            // 图片加载完，才显示选择按钮
                            finalViewHolder.check.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }.start();

        } else {
            viewHolder.photos.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_img));
        }
        // 判断图片选中状态
        if (item.isSel()) {  // 设置为选中状态
            viewHolder.check.setImageResource(R.drawable.sel);
        } else {    // 默认不选择
            viewHolder.check.setImageResource(R.drawable.no_sel);
        }
        return convertView;

    }

    class ViewHolder {
        ImageView photos;
        ImageView check;
    }
}
