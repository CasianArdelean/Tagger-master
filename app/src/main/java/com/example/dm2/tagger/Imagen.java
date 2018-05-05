package com.example.dm2.tagger;

import android.graphics.Bitmap;

public class Imagen {

    private String titulo;
    private String tagg;
    private Bitmap bit;

    public Imagen(String titulo, String tagg, Bitmap bit) {
        this.titulo = titulo;
        this.tagg = tagg;
        this.bit = bit;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTagg() {
        return tagg;
    }

    public void setTagg(String tagg) {
        this.tagg = tagg;
    }

    public Bitmap getBit() {
        return bit;
    }

    public void setBit(Bitmap bit) {
        this.bit = bit;
    }
}
