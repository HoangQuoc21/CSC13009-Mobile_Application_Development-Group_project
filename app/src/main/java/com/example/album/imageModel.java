package com.example.album;

import android.net.Uri;

public class imageModel {
    // ID ảnh (mỗi ảnh khi đọc từ bộ nhớ sẽ được gán 1 ID để dễ xử lý việc khi Restore ảnh
    // từ Trash thì ảnh sẽ trở lại đúng vị trí ban đầu)
    private int id;
    private String dateTaken;
    private Uri path;

    public imageModel(int id, String dateTaken, Uri path)
    {
        this.id = id;
        this.dateTaken = dateTaken;
        this.path = path;
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