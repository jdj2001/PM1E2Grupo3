package com.example.pm1e2grupo3;

import android.os.Parcel;
import android.os.Parcelable;

public class Person implements Parcelable {
    private int id;
    private String nombre;
    private String telefono;
    private double latitud;
    private double longitud;
    private String video; // URL del video

    // Constructor principal
    public Person(int id, String nombre, String telefono, double latitud, double longitud, String video) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.latitud = latitud;
        this.longitud = longitud;
        this.video = video;
    }

    // Constructor para Parcelable
    protected Person(Parcel in) {
        id = in.readInt();
        nombre = in.readString();
        telefono = in.readString();
        latitud = in.readDouble();
        longitud = in.readDouble();
        video = in.readString();
    }

    // Creador de Parcelable
    public static final Creator<Person> CREATOR = new Creator<Person>() {
        @Override
        public Person createFromParcel(Parcel in) {
            return new Person(in);
        }

        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    // Métodos de Parcelable
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(nombre);
        dest.writeString(telefono);
        dest.writeDouble(latitud);
        dest.writeDouble(longitud);
        dest.writeString(video);
    }
}
