package com.example.album;

//Class dùng để lưu trữ các thông tin về album
public class Album {
    //tên albume
    private String name;
    //getter của tên album
    public String getName()
    {
        return this.name;
    }
    //setter của tên album
    public void setName(String name)
    {
        this.name=name;
    }
    //constructor của album
    public Album(String name) {
        this.name = name;
    }

}
