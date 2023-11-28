package com.example.album;

// Class dùng để kiểm tra cũng như điểu chỉnh việc ẩn, hiện, disable hay enable các nút.
public class ButtonStatusManager {
    private static ButtonStatusManager instance;
    private boolean isButtonDisabled = false;
    private String nameAlbum="";
    private ButtonStatusManager() {
        // Khởi tạo các giá trị mặc định
    }

    public static ButtonStatusManager getInstance() {
        if (instance == null) {
            instance = new ButtonStatusManager();
        }
        return instance;
    }

    public boolean isButtonDisabled() {
        return isButtonDisabled;
    }

    public void setButtonDisabled(boolean disabled) {
        isButtonDisabled = disabled;
    }

    public void setNameAlbum(String name)
    {
        this.nameAlbum=name;
    }
    public String getNameAlbum()
    {
        return this.nameAlbum;
    }
}
