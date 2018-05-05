package com.example.dm2.tagger;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class listarImagenes extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_imagenes);

        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("arr");
        ArrayList<Imagen> object = (ArrayList<Imagen>) args.getSerializable("array");
        Log.i("arr","llega al otro lado");
        for (int i=0;i<object.size();i++){
            Log.i("arr","arr: "+object.get(i).getTitulo());
        }
    }
}
