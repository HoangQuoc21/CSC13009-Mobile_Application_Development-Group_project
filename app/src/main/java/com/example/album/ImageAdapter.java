package com.example.album;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.viewHolder> {
    private String dateTaken;
    private ArrayList<imageModel> list;
    private Context context;

    public ImageAdapter(String dateTaken, ArrayList<imageModel> list, Context context) {
        this.dateTaken = dateTaken;
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_items, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageAdapter.viewHolder holder, int position) {
        Glide.with(context).load(list.get(position).getPath()).into(holder.imageView);

        // Khi click vào ảnh thì sẽ kích hoạt Activity xem ảnh đơn
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Gửi dữ liệu ảnh (URI path) cho Activity xem ảnh đơn thể render ảnh
                String imageLink = list.get(position).getPath().toString();
                Bundle mybundle = new Bundle();
                mybundle.putString("imageLink", imageLink);
                mybundle.putString("imageDate", dateTaken);
                mybundle.putString("imageIndex", String.valueOf(position));

                Intent newIntent = new Intent(context, ImageActivity.class);
                newIntent.putExtra("package", mybundle);
                context.startActivity(newIntent, mybundle);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder
    {
        ImageView imageView;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}