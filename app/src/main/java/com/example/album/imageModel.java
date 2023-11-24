package com.example.album;

import android.content.Context;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class imageModel {
    // ID ảnh (mỗi ảnh khi đọc từ bộ nhớ sẽ được gán 1 ID để dễ xử lý việc khi Restore ảnh
    // từ Trash thì ảnh sẽ trở lại đúng vị trí ban đầu)
    private int id;
    private String dateTaken;
    private Uri path;

    //cac thong tin exifinterface
    String name;
    String savedPlace;
    String imageLength, imageWidth;
    String cameraMake;
    String cameraModel;
    //context nay truyen vao de doc thong tin exif
    private Context context;

    public imageModel(int id, String dateTaken, Uri path)
    {
        this.id = id;
        this.dateTaken = dateTaken;
        this.path = path;
    }

    public imageModel(Uri path, String imagePath, Context context){
        this.context = context;
        //Tạo biến File giúp đọc file
        File file = new File(imagePath);

        //Đọc ten va noi luu anh
        this.name = file.getName();
        this.savedPlace = file.getPath();

        //tạo input stream để đọc vào Uri của ảnh
        InputStream in = null;
        try {


            //đọc uri ảnh
            in = context.getContentResolver().openInputStream(path);

            //tạo biến exifinterface để đọc các thông tin exif
            ExifInterface exif = new ExifInterface(in);

            //lấy dữ liệu exif
            // Get some exif attributes, you can get more from the documentation
            this.imageLength = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);
            this.imageWidth = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);
            this.cameraMake = exif.getAttribute(ExifInterface.TAG_MAKE);
            this.cameraModel = exif.getAttribute(ExifInterface.TAG_MODEL);
        } catch (IOException e) {}
        finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {}
            }
        }
    }

    public int getId() {
        return id;
    }

    public String getDateTaken() {
        return dateTaken;
    }

    public Uri getPath() {
        return path;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDateTaken(String dateTaken) {
        this.dateTaken = dateTaken;
    }

    public void setPath(Uri path) {
        this.path = path;
    }
}