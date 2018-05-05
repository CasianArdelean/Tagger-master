package com.example.dm2.tagger;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    TextView txtBuscarTagg;
    TextView txtTaggImagen;
    TextView txtTituloImagen;
    int numero;
    ImageView imageV;
    private Socket conexion;
    private DataOutputStream dos;
    private String ip = "213.254.95.118";
    private String s = "";
    String titulo;
    String tagg;
    String taggBuscar;
    byte[] byteArray;
    ArrayList<Imagen> arrBit = new ArrayList<>();
    byte[] buffer;

    private static final int STORAGE_PERMISSION_CODE = 1111;
    private static final int PICK_IMAGE_REQUEST = 2222;

    private Uri filePath;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestStoragePermission();
        imageV = (ImageView) findViewById(R.id.Imageprev);


    }

    public void buscarImagenes(View v) {
        txtBuscarTagg = (TextView) findViewById(R.id.txtBuscarTagg);
        taggBuscar = txtBuscarTagg.getText().toString();
        numero = 1;

        myTask mt = new myTask();
        mt.execute();

        if (arrBit.size() > 0) {
            Intent Intent = new Intent(this, listarImagenes.class);
            Bundle b = new Bundle();
            b.putSerializable("array", (Serializable) arrBit);
            Intent.putExtra("arr", b);
            startActivity(Intent);
        } else {
            Toast.makeText(this, "No hay ninguna imagen", Toast.LENGTH_LONG).show();
        }


    }

    public void seleccionarImagen(View v) {
        showFileChooser();
    }

    public void subirImagen(View v) {
        txtTaggImagen = (TextView) findViewById(R.id.taggImagen);
        txtTituloImagen = (TextView) findViewById(R.id.tituloImagen);
        titulo = txtTituloImagen.getText().toString();
        tagg = txtTaggImagen.getText().toString();
        numero = 2;

        myTask mt = new myTask();
        mt.execute();


    }

    private void requestStoragePermission() {
        if (android.support.v4.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            return;
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso concedido", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Permiso denegado", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageV.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class myTask extends AsyncTask<String, Void, String> {


        protected void onPostExecute(String s) {
            //imageV.setImageBitmap(arrBit.get(0).getBit());
        }

        protected String doInBackground(String... params) {
            try {
                DataInputStream flujoEntrada = null;

                conexion = new Socket(ip, 6780);
                dos = new DataOutputStream(conexion.getOutputStream());

                if (numero == 2) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byteArray = stream.toByteArray();
                    bitmap.recycle();

                    dos.writeInt(numero);
                    dos.writeUTF(titulo);
                    dos.writeUTF(tagg);
                    dos.write(byteArray);
                } else {
                    dos.writeInt(numero);
                    dos.writeUTF(taggBuscar);
                    boolean existe = true;
                    while (existe) {
                        try {
                            flujoEntrada = new DataInputStream(conexion.getInputStream());
                            String t = flujoEntrada.readUTF();
                            Log.i("titulo", "titulo: " + t);

                            if (!t.equalsIgnoreCase("final")) {
                                String et = flujoEntrada.readUTF();
                                Log.i("etiqueta", "etiqueta: " + et);
                                int buff = flujoEntrada.readInt();
                                Log.i("buffer", "buffer: " + buff);


                                buffer = new byte[buff];

                                for (int i = 0; i < buff; i++) {
                                    buffer[i] = flujoEntrada.readByte();
                                }

                                bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
                                arrBit.add(new Imagen(t, et, bitmap));
                                Log.i("fin", "fin");

                            } else {
                                existe = false;
                            }
                        } catch (EOFException e) {
                            break;
                        }
                    }

                    //imageV.setImageBitmap(arrBit.get(0));

                    flujoEntrada.close();


                }


                dos.flush();
                dos.close();


            } catch (IOException e) {
                e.printStackTrace();
            }
            return s;
        }
        // protected void onPostExecute(String s2){
        //txtBuscarTagg.setText(s2);
        //}

    }
}
