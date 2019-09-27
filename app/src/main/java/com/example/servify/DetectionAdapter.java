package com.example.servify;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DetectionAdapter extends RecyclerView.Adapter<DetectionAdapter.ObjectViewHolder> {

    private List<ObjectModel> objects;

    public DetectionAdapter(List<ObjectModel> objects) {
        this.objects = objects;
    }

    @NonNull
    @Override
    public DetectionAdapter.ObjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.list_item_detection_view, parent, false);
        return new ObjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ObjectViewHolder holder, int position) {
        final ObjectModel object = objects.get(position);
        switch (object.getType()) {
            case "laptop":
                holder.objectIcon.setImageResource(R.drawable.ic_laptop);
                break;

            case "mouse":
                holder.objectIcon.setImageResource(R.drawable.ic_mouse);
                break;

            case "keyboard":
                holder.objectIcon.setImageResource(R.drawable.ic_keyboard);
                break;

            case "cell phone":
                holder.objectIcon.setImageResource(R.drawable.ic_phone);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    class ObjectViewHolder extends RecyclerView.ViewHolder {

        ImageView objectIcon;

        public ObjectViewHolder(@NonNull View itemView) {
            super(itemView);

            objectIcon = itemView.findViewById(R.id.iv_detection_list_item);
        }
    }
}
