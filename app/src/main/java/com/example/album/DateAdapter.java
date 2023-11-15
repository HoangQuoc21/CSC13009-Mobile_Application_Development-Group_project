package com.example.album;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.album.ImageAdapter;
import com.example.album.R;
import com.example.album.imageModel;

import java.util.ArrayList;
import java.util.HashMap;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.ViewHolder> {
    private Context context;
    private ArrayList<String> dates;
    private HashMap<String, ArrayList<imageModel>> imagesByDate;

    public DateAdapter(ArrayList<String> dates, HashMap<String, ArrayList<imageModel>> imagesByDate, Context context) {
        this.dates = dates;
        this.imagesByDate = imagesByDate;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.date_images, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String date = dates.get(position);
        ArrayList<imageModel> images = imagesByDate.get(date);

        holder.dateTextView.setText(date);
        GridLayoutManager layoutManager = new GridLayoutManager(context,3);
        holder.imageRecyclerView.setLayoutManager(layoutManager);
        holder.imageRecyclerView.setAdapter(new ImageAdapter(date, images, this.context));
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView dateTextView;
        public RecyclerView imageRecyclerView;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            imageRecyclerView = itemView.findViewById(R.id.imageRecyclerView);
        }
    }
}