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

    // progress bar
    private ProgressBar pbHor;
    // progress text
    private TextView tvPb;

    private Thread bkgdThread;

    // image download record
    private int index = 0;
    // image source url
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
            // download images the same time

            ViewHolder finalViewHolder = viewHolder;
            bkgdThread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        bkgdThread.sleep(0);
                    }
                    catch(Exception e){}
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
                    // back to main thread
                    Bitmap finalBitmap = bitmap;
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            finalViewHolder.photos.setImageBitmap(finalBitmap);
                            // update progress bar the same time with downloading images
                            pbHor.setProgress((index) * 5);
                            // update progress text
                            tvPb.setText("Downloading " + (index) + " of 20 images");
                            // indicate play button after all image download
                            finalViewHolder.check.setVisibility(View.VISIBLE);
                        }
                    });
                }
            };
            bkgdThread.start();

        } else {
            viewHolder.photos.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_img));
        }
        // get selection
        if (item.isSel()) {  // set as selected
            viewHolder.check.setImageResource(R.drawable.sel);
        } else {    // default not selected
            viewHolder.check.setImageResource(R.drawable.no_sel);
        }
        return convertView;
    }

    class ViewHolder {
        ImageView photos;
        ImageView check;
    }

}
