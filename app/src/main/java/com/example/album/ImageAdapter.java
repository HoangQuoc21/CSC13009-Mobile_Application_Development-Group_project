package com.example.album;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.Objects;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.viewHolder> {
    private ArrayList<imageModel> list;
    private Context context;
    private String footer;

    public ImageAdapter(String footer, ArrayList<imageModel> list, Context context) {
        this.footer = footer;
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;

        // nếu là ảnh bình thường
        if(Objects.equals(footer, "1"))
        {
            view = LayoutInflater.from(context).inflate(R.layout.list_items, parent, false);
        }
        // nếu là ảnh trong Trash (mỗi ảnh sẽ có thêm TextView hiển thị số giờ còn lại trước khi bị tự động xóa)
        else
        {
            view = LayoutInflater.from(context).inflate(R.layout.list_items_trash, parent, false);
        }

        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageAdapter.viewHolder holder, int position) {
        Glide.with(context).load(list.get(position).getPath()).into(holder.imageView);

        // nếu là ảnh trong Trash (gán nội dung trong TextView là thuộc tính deleteTimeRemain trong imageModel)
        if(Objects.equals(footer, "2"))
        {
            holder.txtTimeRemain.setText(String.valueOf(list.get(position).getDeleteTimeRemain()) + "h");
        }

        // Khi click vào ảnh thì sẽ kích hoạt Activity xem ảnh đơn
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Gửi dữ liệu ảnh (URI path) cho Activity xem ảnh đơn thể render ảnh
                int imageId = list.get(position).getId();
                String imageLink = list.get(position).getPath().toString();
                String dateTaken = list.get(position).getDateTaken();

                Bundle mybundle = new Bundle();
                mybundle.putInt("imageId", imageId);
                mybundle.putString("imageLink", imageLink);
                mybundle.putString("imageDate", dateTaken);
                mybundle.putString("imageIndex", String.valueOf(position));
                mybundle.putString("footer", footer);

                Intent newIntent = new Intent(context, ImageActivity.class);
                newIntent.putExtra("package", mybundle);
                context.startActivity(newIntent, mybundle);
            }
        });
    }

    @Override
    public int getItemCount() {
        //Quoc fix this
        if (list != null)
            return list.size();
        else
            return 0;
    }

    public class viewHolder extends RecyclerView.ViewHolder
    {
        ImageView imageView;
        TextView txtTimeRemain; // TextView hiển thị số giờ còn lại trước khi bị tự động xóa

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);

            // nếu là ảnh trong Trash
            if(Objects.equals(footer, "2"))
            {
                txtTimeRemain = itemView.findViewById(R.id.txtTimeRemain);
            }
        }
    }
}
