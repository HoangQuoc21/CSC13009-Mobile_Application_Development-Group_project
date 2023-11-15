package com.example.album;

import android.net.Uri;

public class imageModel {
//    private int id;
    private Uri path;

    public imageModel(int id, Uri path)
    {
//        this.id = id;
        this.path = path;
    }

//    public int getId() {
//        return id;
//    }

    public Uri getPath() {
        return path;
    }

//    public void setId(int id) {
//        this.id = id;
//    }
    public void setPath(Uri path) {
        this.path = path;
    }
}