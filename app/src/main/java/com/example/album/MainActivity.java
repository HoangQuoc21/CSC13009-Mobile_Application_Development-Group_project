package com.example.album;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<imageModel> imageList;
    ImageAdapter adapter;
    Button btnAll, btnAlbum, btnTrash;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // connect recycler view and create a storage for images' model
        recyclerView = findViewById(R.id.recyclerView);
        imageList = new ArrayList<>();

        // set grid view layout for recycler view with number of column = 3
        GridLayoutManager layoutManager = new
                GridLayoutManager(this,3);
        recyclerView.setLayoutManager(layoutManager);

        // set custom image adapter for recycler view
        adapter = new ImageAdapter(imageList,MainActivity.this);
        recyclerView.setAdapter(adapter);

        // connect buttons
        btnAll = (Button)findViewById(R.id.btnAllTab);
        btnAlbum = (Button)findViewById(R.id.btnAlbumTab);
        btnTrash = (Button)findViewById(R.id.btnTrashTab);

        // processing permission using Dexter
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

    // load images in storage
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
            Collections.sort(cursor, new Comparator<Cursor>() {
                @Override
                public int compare(Cursor cursor1, Cursor cursor2) {
                    long date1 = cursor1.getLong(cursor1.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
                    long date2 = cursor2.getLong(cursor2.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN));
                    return Long.compare(date2, date1);
                }
            });
            HashMap<String, ArrayList<String>> imagesByDate = new HashMap<>();
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int dateTaken = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN));
            while (cursor.moveToNext()){
                long id = cursor.getLong(idColumn);
                String dateTaken = cursor.getString(dateTaken);
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,id);
                if (!imagesByDate.containsKey(dateTaken)) {
                    imagesByDate.put(dateTaken, new ArrayList<String>());
                }
                imagesByDate.get(dateTaken).add(contentUri);
                imageList.add(new imageModel(contentUri));
            }
            adapter.notifyDataSetChanged();
            cursor.close();
        }
    }
}
