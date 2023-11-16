package com.example.album;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Current_Album extends AppCompatActivity {
    TextView txtcurrentAlbum;
    Button btnBack;
    RecyclerView recyclerViewAlbum;

    ArrayList<imageModel> imageListCurentAlbum;
    ImageAdapter adapterImageAlbum;

    GridLayoutManager layoutManagerAlbum;

    Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_album);
        txtcurrentAlbum= findViewById(R.id.txtNameCurrentAlbum);
        btnBack= findViewById(R.id.btnCurrentAlbumBack);
        imageListCurentAlbum= new ArrayList<>();

        // Nhận intent
        Intent albumIntent= getIntent();
        String name= albumIntent.getStringExtra("name");
        ArrayList<String> stringList = albumIntent.getStringArrayListExtra("listLink");
        for(int i=0;i<stringList.size();i++)
        {
            String imagePath=stringList.get(i);
            if(imagePath.equals(""))
            {

            }
            else
            {
                imageUri = Uri.parse(imagePath);
                try {
                    Bitmap bm = BitmapFactory.decodeStream(
                            getContentResolver().openInputStream(imageUri));
                    if (bm != null) {
                        imageModel imageuri= new imageModel(0,imageUri);
                        imageListCurentAlbum.add(imageuri);
                    }
                }
                catch  (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        txtcurrentAlbum.setText(name);
        recyclerViewAlbum = findViewById(R.id.recyclerViewAlbum);
        layoutManagerAlbum = new GridLayoutManager(this, 3);
        recyclerViewAlbum.setLayoutManager(layoutManagerAlbum);
        adapterImageAlbum = new ImageAdapter(null, imageListCurentAlbum,Current_Album.this);
        recyclerViewAlbum.setAdapter(adapterImageAlbum);
        adapterImageAlbum.notifyDataSetChanged();
        // Chỉnh ẩn nút delete của Ảnh khi mở ảnh trong album.
        ButtonStatusManager.getInstance().setButtonDisabled(true);
        // Xử lý sự kiện click Back.
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        ButtonStatusManager.getInstance().setButtonDisabled(false);
    }
}