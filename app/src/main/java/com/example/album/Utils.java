package com.example.album;

import static android.content.Context.MODE_PRIVATE;
import static android.database.sqlite.SQLiteDatabase.openOrCreateDatabase;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.widget.Toast;

import java.io.File;

// 1 số hàm được đưa vào class này để có thể được dùng chung bởi Activity và Service,
// thay vì để trong MainActivity thì chỉ có MainActivity dùng được
public class Utils {
    static SQLiteDatabase dbAlbum;
    static SQLiteDatabase dbTrash;

    // Kiểm tra app có đang được bật không
    public static boolean isAppRunning(Context context, String packageName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            for (ActivityManager.RunningAppProcessInfo processInfo : am.getRunningAppProcesses()) {
                if (processInfo.processName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Xóa ảnh trong thiết bị (khi ảnh được xóa vĩnh viễn trong app)
    public static void deleteImageInDevice(Context context, Uri imageUri) {
        ContentResolver resolver = context.getContentResolver();

        try {
            resolver.delete(imageUri, null, null);
            File imageFile = new File(imageUri.getPath());
            MediaScannerConnection.scanFile(context, new String[]{imageFile.getAbsolutePath()}, null, (path, uri) -> {});
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    //-- Hàm này của Quân, Trúc chuyển qua đây và chỉnh sửa để service và activity đều dùng được --
    // Xóa toàn bộ thông tin của ảnh cụ thể (tên album và tên ảnh) khỏi TrashAlbumImage
    public static void deleteOneDataFromTrashAlbumImage(Context context, String data)
    {
        try {
            dbAlbum = context.openOrCreateDatabase("MyDatabase",MODE_PRIVATE,null);
            String sqlQuery="DELETE FROM TrashAlbumImage where nameImage= '"+data+"'; ";
            dbAlbum.execSQL(sqlQuery);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    // Xóa 1 ảnh khỏi database Trash
    public static void deleteDataInTableTrash(Context context, int id)
    {
        try
        {
            dbTrash = context.openOrCreateDatabase("dbTrash",MODE_PRIVATE,null);
            String sqlQuery="DELETE FROM \"trash\" WHERE id=" + id + ";";
            dbTrash.execSQL(sqlQuery);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    // Update trong database số giờ còn lại của 1 ảnh trong Trash
    public static void updateHourRemainInTableTrash(Context context, int imageId, int newHourRemain)
    {
        try
        {
            dbTrash = context.openOrCreateDatabase("dbTrash",MODE_PRIVATE,null);
            String sqlQuery="UPDATE \"trash\" SET hourRemain=" + newHourRemain + " WHERE id=" + imageId + ";";
            dbTrash.execSQL(sqlQuery);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}
