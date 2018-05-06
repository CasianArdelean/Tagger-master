package com.example.dm2.tagger;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
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
import android.widget.Button;
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
    ArrayList<Imagen> arrBit ;
    byte[] buffer;
    Context context;

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
        context=this;


    }
    //CLICK BOTON buscar imagenes
    public void buscarImagenes(View v) {
        //inicializamos array y cogemos los valores de los edit text
        arrBit = new ArrayList<>();
        txtBuscarTagg = (TextView) findViewById(R.id.txtBuscarTagg);
        taggBuscar = txtBuscarTagg.getText().toString();
        //ponemos valor a 1 , es el de buscar imagen
        numero = 1;
        //ejecutamos
        myTask mt = new myTask();
        mt.execute();
    }
    //CLICK BOTON seleccionar imagen
    public void seleccionarImagen(View v) {
        showFileChooser();
    }
    //CLICK BOTON subir imagen
    public void subirImagen(View v) {
        //cogemos contenido de los edit text
        txtTaggImagen = (TextView) findViewById(R.id.taggImagen);
        txtTituloImagen = (TextView) findViewById(R.id.tituloImagen);
        titulo = txtTituloImagen.getText().toString();
        tagg = txtTaggImagen.getText().toString();
        //ponemos valor a 2 , es el de subir imagen
        numero = 2;
        //comprobamos que todos los campos estan rellenos
        if (!titulo.equalsIgnoreCase("") && !tagg.equalsIgnoreCase("")) {
            //ejecutamos
            myTask mt = new myTask();
            mt.execute();
        }else{
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_LONG).show();
        }

    }
    //Comprobamos permisos
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

    //Cogemos fotos del movil
    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Comprobamos que a escogido una foto
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageV.setImageBitmap(bitmap);
                Button btn=(Button) findViewById(R.id.btnSubir);
                //Al escoger foto activamos el boton
                btn.setEnabled(true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class myTask extends AsyncTask<String, Void, String> {
        protected void onPostExecute(String s) {

                if(numero==2){
                    Toast.makeText(context, "Foto enviada", Toast.LENGTH_LONG).show();
                }else{
                    //comprobamos que hay fotos recogidas
                    if (arrBit.size() > 0) {
                        Toast.makeText(context, "Fotos recibidas", Toast.LENGTH_LONG).show();
                        Intent Intent = new Intent(context, listarImagenes.class);
                        Bundle b = new Bundle();
                        b.putSerializable("array", (Serializable) arrBit);
                        Intent.putExtra("arr", b);
                        startActivity(Intent);
                    } else {
                        Toast.makeText(context, "No hay ninguna imagen", Toast.LENGTH_LONG).show();
                    }
                }
        }

        protected String doInBackground(String... params) {
            try {
                DataInputStream flujoEntrada = null;

                conexion = new Socket(ip, 6780);
                dos = new DataOutputStream(conexion.getOutputStream());

                if (numero == 2) {
                    //Enviamos fotos
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byteArray = stream.toByteArray();
                    dos.writeInt(numero);
                    dos.writeUTF(titulo);
                    //reemplazamos espacios por #
                    tagg=tagg.replaceAll(" ","#");
                    dos.writeUTF(tagg);
                    dos.write(byteArray);
                } else {
                    //enviamos el numero y los tags.
                    dos.writeInt(numero);
                    dos.writeUTF(taggBuscar);
                    boolean existe = true;
                    //mientras este a TRUE leemos los que nos envia el servidor
                    while (existe) {
                        try {
                            flujoEntrada = new DataInputStream(conexion.getInputStream());
                            String t = flujoEntrada.readUTF();

                            if (!t.equalsIgnoreCase("final")) {
                                //leemos las etiquetas que tiene y el buffer
                                String et = flujoEntrada.readUTF();
                                int buff = flujoEntrada.readInt();

                                //cremos el buffer
                                buffer = new byte[buff];

                                //leemos bytes
                                for (int i = 0; i < buff; i++) {
                                    buffer[i] = flujoEntrada.readByte();
                                }

                                //creamos la foto
                                Bitmap bitmap2 = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
                                //creamos una clase Imagen y la añadimos al array
                                arrBit.add(new Imagen(t, et, bitmap2));
                                Log.i("fin", "fin");

                            } else {
                                //si no hay mas fotos cambiamos a false
                                existe = false;
                            }
                        } catch (EOFException e) {
                            break;
                        }
                    }
                    flujoEntrada.close();


                }


                dos.flush();
                dos.close();
                conexion.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return s;
        }
    }
}
