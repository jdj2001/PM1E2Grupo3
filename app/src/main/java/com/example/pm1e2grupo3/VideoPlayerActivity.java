package com.example.pm1e2grupo3;

import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class VideoPlayerActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoView = findViewById(R.id.videoView);

        String videoUrl = getIntent().getStringExtra("videoUrl");
        if (videoUrl != null) {
            videoView.setVideoURI(Uri.parse(videoUrl));
            videoView.start();
        }
    }
}
