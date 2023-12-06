package com.example.album;

import android.annotation.SuppressLint;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.net.Uri;
import android.os.PersistableBundle;

// service tự động xóa ảnh sau 24h
@SuppressLint("SpecifyJobSchedulerIdRange")
public class AutoDeleteService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        PersistableBundle extras = params.getExtras();

        // lấy imageId và imageLink từ bundle
        int imageId = extras.getInt("imageId");
        String imageLink = extras.getString("imageLink");

        // gọi hàm xóa ảnh
        deleteTrash(imageId, imageLink);

        // Trả về false để cho hệ thống biết công việc đã hoàn thành
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        // Trả về true để yêu cầu hệ thống thử lại công việc nếu nó bị gián đoạn
        return true;
    }

    public void deleteTrash(int imageId, String imageLink)
    {
        // nếu app đang được bật thì gửi broadbast cho MainActivity xóa
        // ảnh khỏi danh sách ảnh đang hiển thị
        if (Utils.isAppRunning(getApplicationContext(), "com.example.album")) {
            Intent intentAutoDelete = new Intent("autoDeleteTrash");
            intentAutoDelete.putExtra("imageId", imageId);
            sendBroadcast(intentAutoDelete);
        }

        // Phần của Quân được thêm bởi Trúc
        Utils.deleteOneDataFromTrashAlbumImage(getApplicationContext(), imageLink);
        //

        // xóa trong database Trash
        Utils.deleteDataInTableTrash(getApplicationContext(), imageId);
        // xóa trong thư viện ảnh của thiết bị
        Utils.deleteImageInDevice(getApplicationContext(), Uri.parse(imageLink));
    }
}
