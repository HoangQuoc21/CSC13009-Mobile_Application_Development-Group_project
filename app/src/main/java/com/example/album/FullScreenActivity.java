package com.example.album;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FullScreenActivity extends AppCompatActivity {

    ImageView fullImage;
    String imageLink = ""; // as a link
    String imageDate = "";
    String imageIndex = "";
    Uri imageUri;
    ScaleGestureDetector scaleGestureDetector;
    float scaleFactor = 1.0f;
    Button btnAddAlbum, btnAddFavorite, btnDelete, btnInfo, btnBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        fullImage = findViewById(R.id.imageView);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("package");
        imageLink = bundle.getString("imageLink");
        imageDate = bundle.getString("imageDate");
        imageIndex = bundle.getString("imageIndex");

        Glide.with(this).load(imageLink).into(fullImage);

        scaleGestureDetector = new ScaleGestureDetector(this,
                new ScaleListener());

        // Kết nối các nút Button với layout
        btnAddAlbum = (Button) findViewById(R.id.btnAddAlbum);
        btnAddFavorite = (Button) findViewById(R.id.btnAddFavorite);
        btnDelete = (Button) findViewById(R.id.btnDelete);
        btnInfo = (Button) findViewById(R.id.btnInfo);
        btnBack = (Button) findViewById(R.id.btnBack);

        // -----------------Xử lý sự kiện click-------------

        //Nút Back để trở về Activity chính.
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Nút thêm Album
        btnAddAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Your code here.
            }
        });

        //Nút thêm vào Album yêu thích
        btnAddFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Your code here.
                // gửi index của ảnh trong danh sách cho MainActivity bằng broadcast

                Intent intentAddFavorite= new Intent("addFavorite");
                intentAddFavorite.putExtra("imageLink",imageLink);
                sendBroadcast(intentAddFavorite);
            }
        });

        //Nút Delete
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // gửi index của ảnh trong danh sách cho MainActivity bằng broadcast
                Intent intentDelete = new Intent("deleteImage");
                intentDelete.putExtra("imageIndex", imageIndex);
                intentDelete.putExtra("imageDate", imageDate);
                sendBroadcast(intentDelete);
            }
        });

        //Nút Xem thông tin
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Your code here.
                //Tạo biến File giúp đọc file
                File file = new File(imageLink);

                //tạo input stream để đọc vào Uri của ảnh
                InputStream in = null;
                try {
                    //Đọc thông tin ảnh
                    String name = file.getName();
                    String place = file.getPath();

                    //đọc uri ảnh
                    imageUri = Uri.parse(imageLink);
                    in = getContentResolver().openInputStream(imageUri);

                    //tạo biến exifinterface để đọc các thông tin exif
                    ExifInterface exif = new ExifInterface(in);
                    // Now you can extract any Exif tag you want
                    // Assuming the image is a JPEG or supported raw format

                    //lấy dữ liệu exif
                    // Get some exif attributes, you can get more from the documentation
                    String dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME);
                    String imageLength = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
                    String imageWidth = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
                    String make = exif.getAttribute(ExifInterface.TAG_MAKE);
                    String model = exif.getAttribute(ExifInterface.TAG_MODEL);


                    //tạo string chứa các thông tin exif
                    // Create a StringBuilder to format the exif information
                    StringBuilder sb = new StringBuilder();
                    sb.append("Name: ").append(name).append("\n");
                    sb.append("Saved place: ").append(place).append("\n");
                    sb.append("Date and time: ").append(dateTime).append("\n");
                    sb.append("Image length: ").append(imageLength).append(" pixels\n");
                    sb.append("Image width: ").append(imageWidth).append(" pixels\n");
                    sb.append("Camera make: ").append(make).append("\n");
                    sb.append("Camera model: ").append(model).append("\n");

                    //tạo hộp thoại dialog để hiển thị thông tin exif
                    // Create an AlertDialog.Builder object to build the dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(FullScreenActivity.this);
                    builder.setTitle("Image Information");
                    builder.setMessage(sb.toString());
                    builder.setPositiveButton("OK", null);

                    //hiển thị hộp thoại dialog
                    // Create and show the dialog
                    AlertDialog dialog = builder.create();
                    dialog.show();

                } catch (IOException e) {
                    // Handle any errors
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException ignored) {}
                    }
                }
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.1f,Math.min(scaleFactor,10.0f));

            fullImage.setScaleX(scaleFactor);
            fullImage.setScaleY(scaleFactor);

            return true;
        }
    }
}