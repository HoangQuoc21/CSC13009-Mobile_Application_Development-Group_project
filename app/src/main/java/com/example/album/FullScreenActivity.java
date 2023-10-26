package com.example.album;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class FullScreenActivity extends AppCompatActivity {

    ImageView fullImage;
    String image = ""; // as a link
    ScaleGestureDetector scaleGestureDetector;
    float scaleFactor = 1.0f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);

        fullImage = findViewById(R.id.fullImage);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("package");
        image = bundle.getString("parseData");
        Glide.with(this).load(image).into(fullImage);

        scaleGestureDetector = new ScaleGestureDetector(this,
                new ScaleListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener
    {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.1f,Math.min(scaleFactor,10.0f));

            fullImage.setScaleX(scaleFactor);
            fullImage.setScaleY(scaleFactor);

            return true;
        }
    }
}