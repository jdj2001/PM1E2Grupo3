package com.example.pm1e2grupo3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ActivityUpdate extends AppCompatActivity {
    static final int PETICION_ACCESO_CAMARA = 101;
    static final int PETICION_CAPTURA_VIDEO = 102;

    private EditText etNombre, etTelefono, etLatitud, etLongitud;
    private VideoView videoView; // Cambiado de ImageView a VideoView
    private Button btnTakeVideo, btnSave;
    private String id, nombre, telefono, latitud, longitud, video;
    private Uri videoUri;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        etLatitud = findViewById(R.id.etLatitud);
        etLongitud = findViewById(R.id.etLongitud);
        videoView = findViewById(R.id.videoView);
        btnTakeVideo = findViewById(R.id.btnTakeVideo);
        btnSave = findViewById(R.id.btnSave);

        requestQueue = Volley.newRequestQueue(this);

        if (getIntent().hasExtra("id")) {
            id = getIntent().getStringExtra("id");
            fetchContactDetails(id);
        } else {
            Toast.makeText(this, "No se recibió el ID del contacto", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnTakeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(ActivityUpdate.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ActivityUpdate.this, new String[]{Manifest.permission.CAMERA}, PETICION_ACCESO_CAMARA);
                } else {
                    openCamera();
                }
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nombre = etNombre.getText().toString();
                telefono = etTelefono.getText().toString();
                latitud = etLatitud.getText().toString();
                longitud = etLongitud.getText().toString();

                if (videoUri != null) {
                    video = encodeVideo(videoUri);
                }

                updateData();
            }
        });
    }

    private void fetchContactDetails(String id) {
        String url = "http://192.168.58.106/crud_php_examen/get_persona.php?id=" + id;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            nombre = jsonObject.getString("nombre");
                            telefono = jsonObject.getString("telefono");
                            latitud = jsonObject.getString("latitud");
                            longitud = jsonObject.getString("longitud");
                            video = jsonObject.getString("video");

                            etNombre.setText(nombre);
                            etTelefono.setText(telefono);
                            etLatitud.setText(latitud);
                            etLongitud.setText(longitud);

                            if (video != null && !video.isEmpty()) {
                                // Reproducir el video
                                videoView.setVideoURI(Uri.parse(video));
                                videoView.start();
                            } else {
                                videoView.setVideoURI(null);
                                // Aquí puedes poner un mensaje o una imagen predeterminada si es necesario
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(ActivityUpdate.this, "Error al obtener los detalles del contacto", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ActivityUpdate.this, "Error al obtener los detalles del contacto", Toast.LENGTH_SHORT).show();
                        Log.e("Volley", error.toString());
                    }
                });

        requestQueue.add(stringRequest);
    }

    private void updateData() {
        String url = "http://192.168.58.106/crud_php_examen/update_persona.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(ActivityUpdate.this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ActivityUpdate.this, "Error al actualizar datos", Toast.LENGTH_SHORT).show();
                Log.e("Volley", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", id);
                params.put("nombre", nombre);
                params.put("telefono", telefono);
                params.put("latitud", latitud);
                params.put("longitud", longitud);
                params.put("video", video); // Cambiado a 'video'
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, PETICION_CAPTURA_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PETICION_CAPTURA_VIDEO && resultCode == RESULT_OK) {
            videoUri = data.getData();
            // Reproducir el video capturado
            videoView.setVideoURI(videoUri);
            videoView.start();
        }
    }

    private String encodeVideo(Uri videoUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(videoUri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
            inputStream.close();
            byte[] videoBytes = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(videoBytes, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
