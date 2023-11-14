package com.example.album;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import kotlinx.coroutines.debug.internal.HashedWeakRef;

public class MainActivity extends AppCompatActivity {

    // Các instance cần thiết để hiển thị ảnh theo group
    // dựa trên thông số ngày được đưa vào bộ nhớ (DATE_TAKEN)
    RecyclerView recyclerView;
    ArrayList<String> dates; // thông tin ngày cho từng list ảnh có cùng DATE_TAKEN
    // Hashmap có key là DATE_TAKEN, value là list các model ảnh có cùng DATE_TAKEN đó
    HashMap<String, ArrayList<imageModel>> imagesByDate;
    DateAdapter dateAdapter; // Pool chứa thông tin ngày và các ảnh có cùng key DATE_TAKEN
    // Các nút chuyển Activity
    Button btnAll, btnAlbum, btnTrash;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Kết nối recycler và init các instance chứa thông tin ảnh khi đọc
        recyclerView = findViewById(R.id.recyclerView);
        dates = new ArrayList<>();
        imagesByDate = new HashMap<>();

        // Cài đặt view layout cho recycler chính chứa các pool ảnh theo dạng trượt dọc (VERTICAL)
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        // Tạo và gắn dateAdapter có chức năng tạo các pool ảnh có cùng DATE_TAKEN cho recyclerView chính
        dateAdapter = new DateAdapter(dates, imagesByDate, this);
        recyclerView.setAdapter(dateAdapter);

        // Kết nối các button chuyển Activity
        btnAll = (Button)findViewById(R.id.btnAllTab);
        btnAlbum = (Button)findViewById(R.id.btnAlbumTab);
        btnTrash = (Button)findViewById(R.id.btnTrashTab);

        // Kiểm tra cho phép truy cập bộ nhớ ngoài bằng Dexter
        Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        ReadSdcard(MainActivity.this);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
        btnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Open another activity", Toast.LENGTH_SHORT).show();
            }
        });
        btnAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Open album activity", Toast.LENGTH_SHORT).show();
            }
        });
        btnTrash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Open trash activity", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Tải dữ liệu ảnh URI trong bộ nhớ
    private void ReadSdcard(Context context){
        Uri collection;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        }else
        {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        String projection[] = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_TAKEN
        };

        try(Cursor cursor = MainActivity.this.getContentResolver().query(
                collection,
                projection,
                null,
                null
        )){
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN);
            while (cursor.moveToNext()){
                long id = cursor.getLong(idColumn);

                // format date dạng unix timestamp sang dạng dd-MM yyyy và dùng làm key cho hashMap
                String dateTaken_unix = cursor.getString(dateTakenColumn);
                long dv = Long.valueOf(dateTaken_unix);
                Date df = new java.util.Date(dv);
                String dateTaken = new SimpleDateFormat("dd-MM yyyy").format(df);

                Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,id);
                if (!imagesByDate.containsKey(dateTaken)) {
                    dates.add(dateTaken);
                    imagesByDate.put(dateTaken, new ArrayList<imageModel>());
                }
                imagesByDate.get(dateTaken).add(new imageModel(contentUri));
            }
            dateAdapter.notifyDataSetChanged();
        }
    }
}
