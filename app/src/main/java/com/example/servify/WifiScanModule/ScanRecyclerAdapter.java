package com.example.servify.WifiScanModule;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.servify.ObjectModel;
import com.example.servify.R;

import java.util.ArrayList;
import java.util.List;

public class ScanRecyclerAdapter extends RecyclerView.Adapter<ScanRecyclerAdapter.ViewHolder> {
    private List<ObjectModel> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    private List<ObjectModel> result = new ArrayList<ObjectModel>();

    ScanRecyclerAdapter(Context context, List<ObjectModel> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.wifi_scan_adapter, parent, false);
        return new ViewHolder(view);
    }


    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ObjectModel dat = mData.get(position);
        holder.mac.setText(dat.getMacAddress());
        holder.ip.setText(dat.getIpAddress());
        holder.host.setText(dat.getVendor());
        holder.ven.setText(dat.getHost());

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                result.add(dat);
            }
        });
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mac, ip, host, ven;
        CheckBox checkBox;

        ViewHolder(View itemView) {
            super(itemView);
            mac = itemView.findViewById(R.id.mac);
            ip = itemView.findViewById(R.id.ip);
            host = itemView.findViewById(R.id.hostname);
            ven = itemView.findViewById(R.id.vendor);
            checkBox = itemView.findViewById(R.id.check_box);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public List<ObjectModel> getSelected()
    {
        return result;
    }



}