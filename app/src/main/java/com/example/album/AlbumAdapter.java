package com.example.album;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class AlbumAdapter extends ArrayAdapter<Album> {
    Activity context;
    int IdLayout;
    ArrayList<Album> listAlbum;
    // Tạo constructor và truyền các tham số

    public AlbumAdapter(@NonNull Activity context, int idLayout, ArrayList<Album> listAlbum) {
        super(context, idLayout,listAlbum);
        this.context = context;
        IdLayout = idLayout;
        this.listAlbum = listAlbum;
    }
    // Gọi hàm getView để tiến hành sắp xếp dữ liệu

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Tạo đế để chưa layout
        LayoutInflater myflater= context.getLayoutInflater();
        // Đặt IDlayout lên đế để tạo thành một đối tượng View
        convertView= myflater.inflate(IdLayout,null);
        // Lấy một phần tử trong mảng
        Album myAlbum= listAlbum.get(position);
        // Khai báo tham chiếu ID và hiển thị tên album lên TextView
        TextView name_album= convertView.findViewById(R.id.txtNameAlbum);
        name_album.setText(myAlbum.getName());
        return convertView;
    }
}
