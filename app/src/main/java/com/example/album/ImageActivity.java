package com.example.album;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ImageActivity extends AppCompatActivity {
    private static final int REQUEST_WRITE_STORAGE = 112;

    //Khai báo các nút Button
    Button btnAddAlbum, btnAddFavorite, btnDelete, btnInfo, btnBack;

    //Khai báo ImageView

    ImageView imageView;

    //Khai báo ScaleGestureDetector dùng để scale ảnh (Zoom in, Zoom out)
    private ScaleGestureDetector scaleGestureDetector;

    // Khai báo giá trị Factor (giá trị scale)
    private float Factor = 1.0f;

    //Khai báo tọa độ lastX, last Y dùng để di chuyển ảnh
    private float lastX = 0.0f;
    private float lastY = 0.0f;

    Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        //Lấy dữ liệu từ Main Activity
        Intent myIntent = getIntent();

        //Lấy bundle ra hỏi intent
        Bundle myBundle = myIntent.getBundleExtra("mypackage");
        String imagePath = myBundle.getString("linkImage");

        
        try {
            // parse String path to Uri to create bitmap
            imageUri = Uri.parse(imagePath);
            Bitmap bm = BitmapFactory.decodeStream(
                    getContentResolver().openInputStream(imageUri));
            if (bm != null) {
                //Kết nối với layout
                imageView = findViewById(R.id.imageView);

                // Đặt ảnh vào ImageView
                imageView.setImageBitmap(bm);

                //Xử lý scale ảnh (Zoom in, Zoom out);
                scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

                //thông báo để kiểm tra
                //Toast.makeText(this, " Đọc được ảnh từ đường dẫn", Toast.LENGTH_SHORT).show();
            }
            else {
                //thông báo để kiểm tra
                //Toast.makeText(this, "Không thể đọc ảnh từ đường dẫn.", Toast.LENGTH_SHORT).show();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ;

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

            }
        });

        //Nút Delete
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Your code here.

            }
        });

        //Nút Xem thông tin
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Your code here.
                //Tạo biến File giúp đọc file
                File file = new File(imagePath);

                //tạo input stream để đọc vào Uri của ảnh
                InputStream in = null;
                try {
                    //Đọc thông tin ảnh
                    String name = file.getName();
                    String place = file.getPath();

                    //đọc uri ảnh
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(ImageActivity.this);
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
    //Xử lý scale ảnh (Zoom in, Zoom out);

    //Xử lý event chạm vòa ảnh (Move)
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        scaleGestureDetector.onTouchEvent(event);


        // Xử lý di chuyển ảnh
        // Xử lý này để sau khi Zoom thì ta có thể kéo chuột để xem ảnh

        // Lưu giá trị tọa độ chuột hiện tại vào lastX và lastY
        float currentX = event.getX();
        float currentY = event.getY();

        // Xử lý sự kiện kéo chuột
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float deltaX = currentX - lastX;
            float deltaY = currentY - lastY;
            imageView.scrollBy((int) -deltaX, (int) -deltaY);
        }

        // Cập nhật lastX và lastY cho sự kiện tiếp theo
        lastX = currentX;
        lastY = currentY;

        return super.onTouchEvent(event);
    }

    // Xử lý Scale ảnh
    class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // Lấy độ Zoom
            Factor *= detector.getScaleFactor();
            Factor = Math.max(0.1f, Math.min(Factor, 10.f));
            // Zoom ảnh (Zoom in hoặc out)
            // Để kiểm tra, nếu dùng điện thoại thì dùng 2 ngón tay để zoom.
            // Nếu dùng máy ảo trong laptop thì nhấn giữ phím Ctrl rồi lăn con lăn của chuột, hoặc dùng touchpad để Zoom
            imageView.setScaleX(Factor);
            imageView.setScaleY(Factor);
            return true;
        }
    }
}