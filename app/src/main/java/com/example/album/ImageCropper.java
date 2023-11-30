

package com.example.album;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.UUID;

public class ImageCropper extends AppCompatActivity {
    String srcUri, desUri;
    Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_cropper);

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            srcUri = intent.getStringExtra("SendImageData");
            imageUri = Uri.parse(srcUri);
        }
        desUri = new StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString();

        UCrop.Options options = new UCrop.Options();
        UCrop.of(imageUri, Uri.fromFile(new File(getCacheDir(), desUri)))
                .withOptions(options)
                .withAspectRatio(0, 0)
                .withMaxResultSize(2000, 2000)
                .start(ImageCropper.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);

            Intent intent = new Intent();
            intent.putExtra("Crop", resultUri+"");
            setResult(101, intent);
            finish();

        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
        }
    }
}