package com.example.album;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    // Các instance cần thiết để hiển thị ảnh theo group
    // dựa trên thông số ngày được đưa vào bộ nhớ (DATE_TAKEN)
    RecyclerView recyclerView, recyclerViewTrash;
    ArrayList<imageModel> imageListTrash;
    ImageAdapter adapterTrash;
    GridLayoutManager layoutManager, layoutManagerTrash;
    ArrayList<String> dates; // thông tin ngày cho từng list ảnh có cùng DATE_TAKEN
    // Hashmap có key là DATE_TAKEN, value là list các model ảnh có cùng DATE_TAKEN đó
    HashMap<String, ArrayList<imageModel>> imagesByDate;
    DateAdapter dateAdapter; // Pool chứa thông tin ngày và các ảnh có cùng key DATE_TAKEN
    // Các nút chuyển Activity
    Button btnAll, btnAlbum, btnTrash;
    Button activeButton; // button (tab) đang được chọn
    View frame; // khung để đặt 3 layout tương ứng với 3 tab
    int currentLayout = 0; // layout đang được chọn (có giá trị 1 hoặc 2 hoặc 3)

    // đánh dấu lần đầu tiên bấm vào 1 tab thì n của tab đó = 0 (không cần phải khôi phục trạng thái)
    int nAll = 0;
    int nAlbum = 0;
    int nTrash = 0;

    // 3 mảng lưu trạng thái của mỗi tab trước khi chuyển qua tab khác
    ArrayList<View> savedViewsAll = new ArrayList<>();
    ArrayList<View> savedViewsAlbum = new ArrayList<>();
    ArrayList<View> savedViewsTrash = new ArrayList<>();

    // danh sách ảnh chỉ được đọc từ thư viện ảnh vào lần đầu tiên mở ứng dụng
    boolean isReadSdcardCalled = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // danh sách ảnh trong All và Trash
        //imageList = new ArrayList<>();
        imageListTrash = new ArrayList<>();

        dates = new ArrayList<>();
        imagesByDate = new HashMap<>();

        // khung để đặt 3 layout tương ứng với 3 tab
        frame = findViewById(R.id.frame);

        // load layout trash lên để kết nối với các widget, adapter,... trong đó
        loadLayout(R.layout.trash, 3);
        recyclerViewTrash = findViewById(R.id.recyclerViewTrash);
        layoutManagerTrash = new GridLayoutManager(this, 3);
        recyclerViewTrash.setLayoutManager(layoutManagerTrash);
        adapterTrash = new ImageAdapter(null, imageListTrash,MainActivity.this);
        recyclerViewTrash.setAdapter(adapterTrash);
        nTrash = 1;


        // load layout album lên để kết nối với các widget, adapter,... trong đó
        // ...


        // load layout all lên để kết nối với các widget, adapter,... trong đó
        loadLayout(R.layout.all, 1);
        // Kết nối recycler và init các instance chứa thông tin ảnh khi đọc
        recyclerView = findViewById(R.id.recyclerView);
        // Cài đặt view layout cho recycler chính chứa các pool ảnh theo dạng trượt dọc (VERTICAL)
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        // Tạo và gắn dateAdapter có chức năng tạo các pool ảnh có cùng DATE_TAKEN cho recyclerView chính
        dateAdapter = new DateAdapter(dates, imagesByDate, this);
        recyclerView.setAdapter(dateAdapter);
        nAll = 1;


        // Kết nối các button chuyển Activity
        btnAll = (Button)findViewById(R.id.btnAllTab);
        btnAlbum = (Button)findViewById(R.id.btnAlbumTab);
        btnTrash = (Button)findViewById(R.id.btnTrashTab);

        // custom tiêu đề của tab đang được chọn
        activeButton = btnAll; // tab được chọn mặc định khi vừa mở ứng dụng là All
        Drawable activeDrawable = getResources().getDrawable(R.drawable.custom_button_active,null);
        activeButton.setBackground(activeDrawable);

        if(isReadSdcardCalled ==false)
        {
            // Kiểm tra cho phép truy cập bộ nhớ ngoài bằng Dexter
            Dexter.withContext(this)
                    .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                            ReadSdcard(MainActivity.this);
                            isReadSdcardCalled = true; // đánh dấu là đã đọc
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                            permissionToken.continuePermissionRequest();
                        }
                    }).check();
        }

//        // xin quyền ghi dữ liệu
//        checkWritePermission();

        btnAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                customActiveButton(btnAll); // custom tiêu đề của tab All
                loadLayout(R.layout.all, 1); // load layout của tab All lên frame
            }
        });

        btnAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                customActiveButton(btnAlbum); // custom tiêu đề của tab Album
                loadLayout(R.layout.album, 2); // load layout của tab Album lên frame
            }
        });

        btnTrash.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                customActiveButton(btnTrash); // custom tiêu đề của tab Trash
                loadLayout(R.layout.trash, 3); // load layout của tab Trash lên frame
            }
        });

        // đăng ký broadcast receiver để lắng nghe sự kiện xóa 1 ảnh từ ImageActivity
        IntentFilter filter = new IntentFilter("deleteImage");
        registerReceiver(receiver, filter);
    }

//    @Override
//    protected void onPause()
//    {
//        super.onPause();
//
//        // ghi danh sách ảnh trong All và Trash vào external storage
//        writeToExternalStorage(imageList, "all.dat");
//        writeToExternalStorage(imageListTrash, "trash.dat");
//    }

//    @Override
//    protected void onResume()
//    {
//        super.onResume();
//
//        // đọc danh sách ảnh từ file vào tab All
//        File file = new File(getExternalFilesDir(null), "all.dat");
//        if (file.exists())
//        {
//            imageList=readFromExternalStorage("all.dat");
//        }
//
//        // đọc danh sách ảnh từ file vào tab Trash
//        File file2 = new File(getExternalFilesDir(null), "trash.dat");
//        if (file2.exists())
//        {
//            imageListTrash=readFromExternalStorage("trash.dat");
//        }
//    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        // hủy đăng ký broadcast receiver
        unregisterReceiver(receiver);
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
            int i = 0;

            while (cursor.moveToNext()){
                long id = cursor.getLong(idColumn);

                // format date dạng unix timestamp sang dạng dd-MM yyyy và dùng làm key cho hashMap
                String dateTaken_unix = cursor.getString(dateTakenColumn);
                String dateTaken=null;
                if (dateTaken_unix != null) {
                    try {
                        long dv = Long.valueOf(dateTaken_unix);
                        Date df = new java.util.Date(dv);
                        dateTaken = new SimpleDateFormat("dd-MM-yyyy").format(df);
                        // Thực hiện các công việc khác với dateTaken nếu cần
                    } catch (NumberFormatException e) {
                        // Xử lý lỗi khi không thể chuyển đổi thành số
                        e.printStackTrace();
                    }
                }

                Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,id);

                if (!imagesByDate.containsKey(dateTaken)) {
                    dates.add(dateTaken);
                    imagesByDate.put(dateTaken, new ArrayList<imageModel>());
                }
                imagesByDate.get(dateTaken).add(new imageModel(i, contentUri));

                i++;
            }

            dateAdapter.notifyDataSetChanged();
        }
    }

//    // xin cấp quyền ghi dữ liệu
//    private void checkWritePermission()
//    {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//        {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
//            {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 112);
//            }
//        }
//    }

//    // ghi danh sách ảnh vào external storage
//    public void writeToExternalStorage(ArrayList<imageModel> imageModels, String fileName)
//    {
//        String state = Environment.getExternalStorageState();
//
//        if(Environment.MEDIA_MOUNTED.equals(state))
//        {
//            try
//            {
//                File file = new File(android.os.Environment.getExternalStorageDirectory(), fileName);
//                FileOutputStream fileOutputStream = new FileOutputStream(file);
//                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
//                objectOutputStream.writeObject(imageModels);
//                objectOutputStream.close();
//                fileOutputStream.close();
//            }
//            catch (Exception e)
//            {
//                e.printStackTrace();
//            }
//        }
//    }

//    // đọc danh sách ảnh từ external storage
//    public ArrayList<imageModel> readFromExternalStorage(String fileName)
//    {
//        ArrayList<imageModel> imageModels = new ArrayList<>();
//
//        String state = Environment.getExternalStorageState();
//
//        if (Environment.MEDIA_MOUNTED.equals(state)|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
//        {
//            try {
//                File file = new File(android.os.Environment.getExternalStorageDirectory(), fileName);
//                FileInputStream fileInputStream = new FileInputStream(file);
//                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
//                imageModels = (ArrayList<imageModel>) objectInputStream.readObject();
//                objectInputStream.close();
//                fileInputStream.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        return imageModels;
//    }

    // broadcast receiver khi có sự kiện xóa 1 ảnh thì di chuyển ảnh đó từ imageList sang imageListTrash
    BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if ("deleteImage".equals(intent.getAction()))
            {
                String imageIndex = intent.getStringExtra("imageIndex");
                String imageDate = intent.getStringExtra("imageDate");

                ArrayList<imageModel> containerList = imagesByDate.get(imageDate);
                imageModel imgModel = containerList.remove(Integer.parseInt(imageIndex));
                imageListTrash.add(imgModel);

                adapterTrash.notifyDataSetChanged();
                dateAdapter.notifyDataSetChanged();
            }
        }
    };

    // custom tiêu đề của tab đang được chọn
    private void customActiveButton(Button clickedButton)
    {
        Drawable normalDrawable = getResources().getDrawable(R.drawable.custom_button,null);
        activeButton.setBackground(normalDrawable);

        Drawable activeDrawable = getResources().getDrawable(R.drawable.custom_button_active,null);
        clickedButton.setBackground(activeDrawable);

        activeButton = clickedButton;
    }

    private void saveCurrentLayoutState()
    {
        switch (currentLayout)
        {
            case 1:
                saveViews((ViewGroup) frame, savedViewsAll);
                break;
            case 2:
                saveViews((ViewGroup) frame, savedViewsAlbum);
                break;
            case 3:
                saveViews((ViewGroup) frame, savedViewsTrash);
        }
    }

    // lưu trạng thái của tab hiện tại (ví dụ: vị trí scroll,...) trước khi chuyển tab
    private void saveViews(ViewGroup frame, ArrayList<View> savedViews)
    {
        savedViews.clear();
        int childCount = frame.getChildCount();

        for (int i = 0; i < childCount; i++)
        {
            savedViews.add(frame.getChildAt(i));
        }
    }

    // khôi phục trạng thái của tab (ví dụ: vị trí scroll,...)
    private void restoreViews(ViewGroup container, ArrayList<View> savedViews)
    {
        for (View savedView : savedViews)
        {
            container.addView(savedView);
        }
    }

    // load layout tương ứng với 1 trong 3 tab vào frame
    private void loadLayout(int layoutResId, int layoutType)
    {
        // lần load layout đầu tiên thì không cần lưu trạng thái layout trước nó
        if(nTrash!=0)
        {
            saveCurrentLayoutState();
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View tabContent = inflater.inflate(layoutResId, (ViewGroup) frame, false);

        ((ViewGroup) frame).removeAllViews();
        ((ViewGroup) frame).addView(tabContent);

        switch (layoutType)
        {
            case 1:
                if(nAll!=0)
                {
                    restoreViews((ViewGroup) frame, savedViewsAll);
                }
                break;
            case 2:
                if(nAlbum!=0)
                {
                    restoreViews((ViewGroup) frame, savedViewsAlbum);
                }
                break;
            case 3:
                if(nTrash!=0)
                {
                    restoreViews((ViewGroup) frame, savedViewsTrash);
                }
                break;
        }

        currentLayout = layoutType;
    }
}
