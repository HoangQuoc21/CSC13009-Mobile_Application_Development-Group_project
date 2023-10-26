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
    private ArrayList<imageModel> list;
    private Context context;

    public ImageAdapter(ArrayList<imageModel> list, Context context) {
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

        // when click on an image, open new full-iamge intent
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // prepare image as a link and put it to intent
//                String parseData = list.get(position).getPath().toString();
//                Bundle mybundle = new Bundle();
//                mybundle.putString("parseData", parseData);
//
//                Intent newIntent = new Intent(context, FullScreenActivity.class);
//                newIntent.putExtra("package", mybundle);


                // send data to Image activity
                String imageLink = list.get(position).getPath().toString();
                Bundle mybundle = new Bundle();
                mybundle.putString("linkImage", imageLink);

                Intent newIntent = new Intent(context, Image.class);
                newIntent.putExtra("mypackage", mybundle);
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