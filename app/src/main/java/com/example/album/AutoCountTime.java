package com.example.album;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;


// đếm số giờ còn lại trước khi bị xóa của ảnh trong Trash để hiển thị lên TextView của mỗi ảnh
public class AutoCountTime extends Worker {
    public AutoCountTime(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    // đối với 1 ảnh trong Trash thì hàm này được gọi 1 lần trong 1h
    @NonNull
    @Override
    public Result doWork() {
        // lấy thông tin được truyền vào
        int jobId = getInputData().getInt("deleteId", -1);
        int imageId = getInputData().getInt("imageId", -1);
        int nRemain = getInputData().getInt("nRemain", 0);

        // nếu số giờ còn lại lớn hơn 0 (khởi tạo là 24h)
        if (nRemain > 0) {
            // nếu app đang được bật
            if (Utils.isAppRunning(getApplicationContext(), "com.example.album")) {
                // gửi broadcast cho MainActivity cập nhật số giờ trên TextView của ảnh
                Intent intentCountTime = new Intent("autoCountTime");
                intentCountTime.putExtra("imageId", imageId);
                intentCountTime.putExtra("timeRemain", nRemain);
                getApplicationContext().sendBroadcast(intentCountTime);
            }

            // cập nhật số giờ còn lại của ảnh trong database Trash
            Utils.updateHourRemainInTableTrash(getApplicationContext(), imageId, nRemain);


            // gọi lần cập nhật tiếp theo (giống như đệ quy vậy)
            // truyền thông tin vào
            Data imageData = new Data.Builder()
                    .putInt("deleteId",jobId)
                    .putInt("nRemain", nRemain-1) // số giờ còn lại giảm 1
                    .putInt("imageId", imageId)
                    .build();

            // tạo request
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(AutoCountTime.class)
                    .setInputData(imageData) // push thông tin vào request
                    .setInitialDelay(1, TimeUnit.HOURS) // thời gian gọi là 1h sau
                    .addTag(String.valueOf(jobId)) // tag là jobId (là idDelete) để ảnh nào đang dùng request này
                    .build();

            //enqueue request
            WorkManager.getInstance(getApplicationContext()).enqueue(workRequest);
        }

        return Result.success();
    }
}
