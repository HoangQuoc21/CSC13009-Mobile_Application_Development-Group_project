package com.example.album;

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
    // Tạo constructor

    public Album(String name) {
        this.name = name;
    }

}
