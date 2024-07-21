package com.example.pm1e2grupo3;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private Context context;
    private List<Person> contactList;
    private List<Person> contactListFull;

    public ContactAdapter(Context context, List<Person> contactList) {
        this.context = context;
        this.contactList = contactList;
        this.contactListFull = new ArrayList<>(contactList);
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Person contact = contactList.get(position);
        holder.tvName.setText(contact.getNombre());
        holder.tvPhone.setText(contact.getTelefono());

        if (contact.getVideo() != null && !contact.getVideo().isEmpty()) {
            holder.videoView.setVisibility(View.VISIBLE);
            holder.ivPhoto.setVisibility(View.GONE);

            try {
                File tempVideoFile = decodeBase64AndSaveVideo(contact.getVideo());
                Uri videoUri = Uri.fromFile(tempVideoFile);
                holder.videoView.setVideoURI(videoUri);

                holder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                            @Override
                            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                                holder.videoView.start();
                            }
                        });
                    }
                });

                holder.videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Log.e("VideoError", "Error code: " + what + " Extra code: " + extra);
                        return true;
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
                holder.videoView.setVisibility(View.GONE);
                holder.ivPhoto.setVisibility(View.VISIBLE);
                Toast.makeText(context, "Error al cargar el video", Toast.LENGTH_SHORT).show();
            }
        } else {
            holder.videoView.setVisibility(View.GONE);
            holder.ivPhoto.setVisibility(View.VISIBLE);
        }

        holder.btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateContact(contact);
            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteContact(contact);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLocationDialog(contact);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showOptionsDialog(contact);
                return true;
            }
        });

        holder.videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contact.getVideo() != null && !contact.getVideo().isEmpty()) {
                    try {
                        File tempVideoFile = decodeBase64AndSaveVideo(contact.getVideo());
                        Uri videoUri = Uri.fromFile(tempVideoFile);
                        Intent intent = new Intent(context, VideoPlayerActivity.class);
                        intent.putExtra("videoUri", videoUri.toString());
                        context.startActivity(intent);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Error al cargar el video", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "No hay video disponible para este contacto.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private File decodeBase64AndSaveVideo(String base64String) throws IOException {
        File tempVideoFile = File.createTempFile("video", ".mp4", context.getCacheDir());

        byte[] videoBytes = Base64.decode(base64String, Base64.DEFAULT);

        try (FileOutputStream fos = new FileOutputStream(tempVideoFile)) {
            fos.write(videoBytes);
        }

        return tempVideoFile;
    }



    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone;
        VideoView videoView;
        ImageView ivPhoto;
        ImageButton btnUpdate, btnDelete;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            videoView = itemView.findViewById(R.id.videoView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            btnUpdate = itemView.findViewById(R.id.btnUpdate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    private void showLocationDialog(Person contact) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("¿Desea ir a la ubicación de " + contact.getNombre() + "?")
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openLocationInMap(contact);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void openLocationInMap(Person contact) {
        double latitud = contact.getLatitud();
        double longitud = contact.getLongitud();
        String label = contact.getNombre();

        String uri = "geo:" + latitud + "," + longitud + "?q=" + latitud + "," + longitud + "(" + label + ")";
        Uri gmmIntentUri = Uri.parse(uri);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            try {
                context.startActivity(mapIntent);
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(context, "No se pudo abrir Google Maps", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Google Maps no está instalado en este dispositivo", Toast.LENGTH_SHORT).show();
        }
    }

    private void showOptionsDialog(Person contact) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Opciones para " + contact.getNombre())
                .setItems(new CharSequence[]{"Actualizar", "Eliminar"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                updateContact(contact);
                                break;
                            case 1:
                                deleteContact(contact);
                                break;
                        }
                    }
                })
                .show();
    }

    private void updateContact(Person contact) {
        Intent intent = new Intent(context, ActivityUpdate.class);
        intent.putExtra("id", String.valueOf(contact.getId()));
        context.startActivity(intent);
    }

    public void setFullContactList(List<Person> fullContactList) {
        this.contactListFull.clear();
        this.contactListFull.addAll(fullContactList);
    }

    public void filter(String text) {
        contactList.clear();
        if (text.isEmpty()) {
            contactList.addAll(contactListFull);
        } else {
            text = text.toLowerCase();
            for (Person contact : contactListFull) {
                if (contact.getNombre().toLowerCase().contains(text) || contact.getTelefono().toLowerCase().contains(text)) {
                    contactList.add(contact);
                }
            }
        }
        notifyDataSetChanged();
    }

    private void deleteContact(final Person contact) {
        new AlertDialog.Builder(context)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que quieres eliminar este contacto?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("Delete Contact", "ID: " + contact.getId());

                        String url = "http://192.168.58.106/crud_php_examen/delete_persona.php?id=" + contact.getId();
                        Log.d("Delete URL", url);

                        StringRequest deleteRequest = new StringRequest(Request.Method.DELETE, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Log.d("Delete Response", response);
                                        try {
                                            JSONObject jsonResponse = new JSONObject(response);
                                            if (jsonResponse.has("message")) {
                                                contactList.remove(contact);
                                                notifyDataSetChanged();
                                                Toast.makeText(context, "Contacto eliminado correctamente", Toast.LENGTH_SHORT).show();
                                            } else if (jsonResponse.has("error")) {
                                                Toast.makeText(context, "Error: " + jsonResponse.getString("error"), Toast.LENGTH_SHORT).show();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            Toast.makeText(context, "Error de respuesta: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e("Delete Error", error.toString());
                                        String errorMessage = "Error al eliminar el contacto";
                                        if (error.networkResponse != null && error.networkResponse.data != null) {
                                            try {
                                                String errorResponse = new String(error.networkResponse.data, "UTF-8");
                                                JSONObject jsonResponse = new JSONObject(errorResponse);
                                                if (jsonResponse.has("error")) {
                                                    errorMessage = "Error: " + jsonResponse.getString("error");
                                                }
                                            } catch (UnsupportedEncodingException | JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );

                        deleteRequest.setRetryPolicy(new DefaultRetryPolicy(
                                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                        ));

                        RequestQueue queue = Volley.newRequestQueue(context);
                        queue.add(deleteRequest);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}


