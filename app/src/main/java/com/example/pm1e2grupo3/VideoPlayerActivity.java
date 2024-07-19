package com.example.pm1e2grupo3;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

public class VideoPlayerActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoView = findViewById(R.id.videoView);

        // Obtener la URI del video desde el Intent
        String videoUriString = getIntent().getStringExtra("videoUri");
        if (videoUriString != null) {
            Uri videoUri = Uri.parse(videoUriString);
            videoView.setVideoURI(videoUri);
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    videoView.start();
                }
            });
        } else {
            Toast.makeText(this, "No se pudo cargar el video", Toast.LENGTH_SHORT).show();
        }
    }

    private void decodeAndPlayVideo(final String base64Video) {
        new DecodeAndPlayVideoTask(this, videoView).execute(base64Video);
    }

    private static class DecodeAndPlayVideoTask extends AsyncTask<String, Void, Uri> {
        private WeakReference<Context> contextRef;
        private WeakReference<VideoView> videoViewRef;

        DecodeAndPlayVideoTask(Context context, VideoView videoView) {
            this.contextRef = new WeakReference<>(context);
            this.videoViewRef = new WeakReference<>(videoView);
        }

        @Override
        protected Uri doInBackground(String... params) {
            String base64Video = params[0];
            try {
                byte[] videoBytes = Base64.decode(base64Video, Base64.DEFAULT);
                File tempVideoFile = File.createTempFile("video", ".mp4", contextRef.get().getCacheDir());
                FileOutputStream fos = new FileOutputStream(tempVideoFile);
                fos.write(videoBytes);
                fos.close();
                return Uri.fromFile(tempVideoFile);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Uri videoUri) {
            Context context = contextRef.get();
            VideoView videoView = videoViewRef.get();
            if (context != null && videoView != null) {
                if (videoUri != null) {
                    videoView.setVideoURI(videoUri);
                    videoView.start();
                } else {
                    Toast.makeText(context, "Error al cargar el video.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}




