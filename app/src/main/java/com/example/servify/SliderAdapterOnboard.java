package com.example.servify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.smarteist.autoimageslider.SliderViewAdapter;

public class SliderAdapterOnboard extends SliderViewAdapter<SliderAdapterOnboard.SliderAdapterVH> {

    private Context context;

    public SliderAdapterOnboard(Context context) {
        this.context = context;
    }

    @Override
    public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_slider_layout, null);
        return new SliderAdapterVH(inflate);
    }

    @Override
    public void onBindViewHolder(SliderAdapterVH viewHolder, int position) {
        switch (position) {
            case 0:
                Glide.with(viewHolder.itemView)
                        .load(R.drawable.ic_undraw_network)
                        .into(viewHolder.imageViewBackground);
                viewHolder.textViewDescription.setText("Detect devices on your network");
                break;
            case 1:
                Glide.with(viewHolder.itemView)
                        .load(R.drawable.ic_undraw_camera)
                        .into(viewHolder.imageViewBackground);
                viewHolder.textViewDescription.setText("Detect devices using camera");
                break;
        }

    }


    @Override
    public int getCount() {
        return 2;
    }

    class SliderAdapterVH extends SliderViewAdapter.ViewHolder {
        View itemView;
        ImageView imageViewBackground;
        TextView textViewDescription;

        public SliderAdapterVH(View itemView) {
            super(itemView);
            imageViewBackground = itemView.findViewById(R.id.iv_auto_image_slider);
            textViewDescription = itemView.findViewById(R.id.tv_auto_text_slider);
            this.itemView = itemView;
        }
    }
}
