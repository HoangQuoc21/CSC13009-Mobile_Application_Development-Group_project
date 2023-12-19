package com.example.album;

//Class dùng để lưu trữ các thông tin về album
public class Album {
    private String name;
    public String getName()
    {
        return this.name;
    }
    public void setName(String name)
    {
        this.name=name;
    }
    public Album(String name) {
        this.name = name;
    }

}
