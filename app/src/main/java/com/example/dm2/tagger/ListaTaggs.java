package com.example.dm2.tagger;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListaTaggs extends AppCompatActivity {

    ListView lista;
    TextView etiquetas;
    ArrayList<String> arrTags;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_taggs);
        Tagger [] taggers= new Tagger[]{
                new Tagger("prueba 1",R.drawable.reddit),
                new Tagger("prueba 2",R.drawable.reddit),
                new Tagger("prueba 3",R.drawable.reddit)
        };

        AdaptadorTagg adaptador =
                new AdaptadorTagg(this,taggers);
        lista = (ListView)findViewById(R.id.lstTaggs);
        lista.setAdapter(adaptador);
        etiquetas=(TextView)findViewById(R.id.txtTag);

        arrTags=new ArrayList<String>();

        lista.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long id)
            {
                Tagger t=(Tagger)a.getItemAtPosition(position);
                etiquetas.setText("");

                if(t.getElegido()==false){
                    arrTags.add(t.getTexto());
                    t.setElegido(true);
                }else{
                    arrTags.remove(t.getTexto());
                    t.setElegido(false);
                }

                for(int i=0;i<arrTags.size();i++){
                    etiquetas.append("#"+arrTags.get(i));
                }
            }
        });


    }

    public void cancelar(View v){
        finish();
    }

    public void aplicar(View v){
        Intent intent = new Intent();
        String t=etiquetas.getText().toString();
        intent.putExtra("resultado", t);
        setResult(RESULT_OK, intent);
        finish();

    }

    class Tagger
    {
        private String texto;
        private int icono;
        private boolean elegido=false;

        public Tagger(String texto, int icono)
        {
            this.texto = texto;
            this.icono = icono;
        }

        public int getIcono() {
            return icono;
        }

        public String getTexto() {
            return texto;
        }
        public boolean getElegido() {
            return elegido;
        }

        public void setIcono(int icono) {
            this.icono = icono;
        }

        public void setTexto(String texto) {
            this.texto = texto;
        }
        public void setElegido(boolean elegido) {
            this.elegido = elegido;
        }


    }

    class AdaptadorTagg extends ArrayAdapter<Tagger> {

        public AdaptadorTagg(Context context, Tagger[] dat) {
            super(context, R.layout.formalista,dat);

        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View item = inflater.inflate(R.layout.formalista, null);

            TextView lblNombre = (TextView) item.findViewById(R.id.lblNombre);
            lblNombre.setText(super.getItem(position).getTexto());

            ImageView icon=(ImageView) item.findViewById(R.id.iconTag);
            icon.setImageResource(super.getItem(position).getIcono());
            return (item);
        }

    }
}
