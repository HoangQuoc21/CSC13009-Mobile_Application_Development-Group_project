package com.example.album;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ArrayList<File> myImageFile;
    CustomAdapter customAdapter;
    RecyclerView recyclerView;
    List<String> mList;

    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        checkPermissions();
        mList = new ArrayList<>();
    }

    private void checkPermissions() {
        // check for sdk in 23 - 29
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            int result = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
            if (result == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show();
                display();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }

        // check for sdk >= 30
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    intent.addCategory("android.intent.category.DEFAULT");
                    intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                    startActivityIfNeeded(intent, 100);
                }catch (Exception exception){
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityIfNeeded(intent, 100);
                }
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantedResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantedResults);
        // at least one permission is granted
        if (grantedResults.length > 0) {
            boolean accepted = (grantedResults[0] == PackageManager.PERMISSION_GRANTED);
            if (accepted) {
                Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show();
                display();
            }else {
                Toast.makeText(this, "Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // find all images in external storage
    private ArrayList<File> findImage(File file) {
        ArrayList<File> imageList = new ArrayList<>();
        File[] imageFile = file.listFiles();

        for (File singleImage : imageFile) {
            if (singleImage.isDirectory() && !singleImage.isHidden()) {
                imageList.addAll(findImage(singleImage));
            }else {
                if (singleImage.getName().endsWith(".jpg") ||
                    singleImage.getName().endsWith(".png")) {
                    imageList.add(singleImage);
                }
            }
        }

        return imageList;
    }

    // display gallery
    private void display() {
        myImageFile = findImage(Environment.getExternalStorageDirectory());
        for (int i = 0; i < myImageFile.size(); ++i) {
            mList.add(String.valueOf(myImageFile.get(i)));
            customAdapter = new CustomAdapter(mList);
            recyclerView.setAdapter(customAdapter);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        }
    }
}