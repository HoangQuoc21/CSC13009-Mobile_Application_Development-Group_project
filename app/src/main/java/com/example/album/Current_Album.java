package com.example.album;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Current_Album extends AppCompatActivity {
    TextView txtcurrentAlbum;
    Button btnBack, btnDelete;
    RecyclerView recyclerViewAlbum;

    ArrayList<imageModel> imageListCurentAlbum;
    ImageAdapter adapterImageAlbum;

    GridLayoutManager layoutManagerAlbum;

    Uri imageUri;

    String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_album);
        txtcurrentAlbum= findViewById(R.id.txtNameCurrentAlbum);
        btnBack= findViewById(R.id.btnCurrentAlbumBack);
        btnDelete= findViewById(R.id.btnCurrentAlbumDelete);
        imageListCurentAlbum= new ArrayList<>();

        // Nhận intent
        Intent albumIntent= getIntent();
        name= albumIntent.getStringExtra("name");

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
                        imageModel imageuri= new imageModel(0, null, imageUri);
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
        adapterImageAlbum = new ImageAdapter("1", imageListCurentAlbum,Current_Album.this);
        recyclerViewAlbum.setAdapter(adapterImageAlbum);
        adapterImageAlbum.notifyDataSetChanged();
        // Chỉnh ẩn nút delete của Ảnh khi mở ảnh trong album.
        ButtonStatusManager.getInstance().setButtonDisabled(true);
        ButtonStatusManager.getInstance().setNameAlbum(name);
        // Xử lý sự kiện click Back.
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // Kiểm tra tên album có phải là Favorite hay không?, nếu phải thì không cho xóa bằng cách ẩn đi nút xóa
        if(name.equals("Favorite"))
        {
            btnDelete.setVisibility(View.GONE);
        }
        // Xử lý sự kiện delete Album
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(Current_Album.this, "Check", Toast.LENGTH_SHORT).show();
                openDialogAddAlbum(Gravity.CENTER);
            }
        });
        IntentFilter filter_deleteInAlbum = new IntentFilter("deleteInAlbum");
        registerReceiver(receiver, filter_deleteInAlbum);

    }

    // add by Quân, receiver dùng để nhận dữ liều từ ImageActivity sau đó xóa ảnh khỏi album đang chọn
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if("deleteInAlbum".equals(intent.getAction()))
            {
                String linkImage=intent.getStringExtra("imageLink");
                Uri imageLinkUri = Uri.parse(linkImage);
                imageListCurentAlbum.removeIf(obj -> obj.getPath().equals(imageLinkUri));
                adapterImageAlbum.notifyDataSetChanged();
            }

        }
    };

    // Đưa giá trị set up của button DeleteInAlbum trong ImageActivity về mặc định để khi không mở trong album
    // thì sẽ không hiện nút Delete In Album
            @Override
    protected void onDestroy() {
        super.onDestroy();
        ButtonStatusManager.getInstance().setButtonDisabled(false);
        ButtonStatusManager.getInstance().setNameAlbum("");
    }
    // Set up dialog yêu cầu Xóa album
    private void openDialogAddAlbum(int gravity)
    {
        final Dialog dialog= new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_check_delete);

        Window window= dialog.getWindow();
        if(window==null)
        {
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttributes= window.getAttributes();
        windowAttributes.gravity=gravity;
        window.setAttributes(windowAttributes);

        if(Gravity.CENTER== gravity)
        {
            dialog.setCancelable(true);
        }
        else
        {
            dialog.setCancelable(false);
        }

        Button btnCurrentAlbumBack= dialog.findViewById(R.id.btnDialogCheckDeleteBack);
        Button btnCurrentAlbumDelete= dialog.findViewById(R.id.btnDialogCheckDeleteConfirm);


        btnCurrentAlbumBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Xóa Album
        btnCurrentAlbumDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // gửi index của ảnh trong danh sách cho MainActivity bằng broadcast

                Intent intentAddFavorite= new Intent("deleteAlbum");
                intentAddFavorite.putExtra("nameAlbum",name);
                sendBroadcast(intentAddFavorite);
                Toast.makeText(Current_Album.this, "Delete Album was successful", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                finish();
            }
        });

        // gọi lệnh Show để hiện Dialog
        dialog.show();
    }
}