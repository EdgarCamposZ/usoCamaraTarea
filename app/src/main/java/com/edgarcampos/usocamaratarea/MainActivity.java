package com.edgarcampos.usocamaratarea;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Button btnTomarFoto, btnGmail, btnWhatsapp;
    ImageView imgFoto;
    String rutaImagenes;
    private static final int REQUEST_CODIGO_CAMARA=200;
    private static final int REQUEST_CODIGO_CAPTURAR_IMAGEN=300;
    Uri rutaFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgFoto = findViewById(R.id.imgFoto);
        btnTomarFoto = findViewById(R.id.btnTomarFoto);
        btnGmail = findViewById(R.id.btnGmail);
        btnWhatsapp = findViewById(R.id.btnWhatsapp);

        btnTomarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                realizarProcesoFotografia();
            }
        });
        
        btnGmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviarImgGmail();
            }
        });
        
        btnWhatsapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviarImgWhatsapp();
            }
        });
    }

    private void enviarImgWhatsapp() {
        Intent intentW = new Intent(Intent.ACTION_SEND);
        intentW.setType("image/*");
        intentW.setPackage("com.whatsapp");

        if (rutaFoto != null) {
            intentW.putExtra(Intent.EXTRA_STREAM, rutaFoto);

            try {
                startActivity(intentW);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Error al enviar\n" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "No se selecciono una imagen", Toast.LENGTH_LONG).show();
        }
    }

    private void enviarImgGmail() {
        Intent intentG = new Intent(Intent.ACTION_SEND);
        intentG.setType("image/*");
        intentG.setPackage("com.google.android.gm");

        if (rutaFoto != null) {
            intentG.putExtra(Intent.EXTRA_STREAM, rutaFoto);

            try {
                startActivity(intentG);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Error al enviar\n" + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "No se selecciono una imagen", Toast.LENGTH_LONG).show();
        }
    }

    private void realizarProcesoFotografia() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if(ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                tomarFoto();
            }else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.CAMERA}, REQUEST_CODIGO_CAMARA);
            }
        }else {
            tomarFoto();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODIGO_CAMARA) {
            if (permissions.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tomarFoto();
            } else {
                Toast.makeText(MainActivity.this, "Se requiere permisos para la camara", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_CODIGO_CAPTURAR_IMAGEN) {
            // Si capturo la foto
            if(resultCode == Activity.RESULT_OK) {
                imgFoto.setImageURI(Uri.parse(rutaImagenes));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void  tomarFoto(){
        Intent intentCamara = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intentCamara.resolveActivity(getPackageManager())!=null) {
            //startActivityForResult(intentCamara, REQUEST_CODIGO_CAPTURAR_IMAGEN);
            File archivoFoto = null;
            archivoFoto = crearArchivo();
            if(archivoFoto!=null) {
                rutaFoto = FileProvider.getUriForFile(
                        MainActivity.this,
                        "com.edgarcampos.usocamaratarea",
                        archivoFoto);
                intentCamara.putExtra(MediaStore.EXTRA_OUTPUT, rutaFoto);
                startActivityForResult(intentCamara, REQUEST_CODIGO_CAPTURAR_IMAGEN);
            }
        }
    }

    private File crearArchivo() {
        String nomenclatura = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        String prefijoArchivo = "APPCAM_" + nomenclatura + "_";
        File directorioImagen = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File miImagen = null;
        try {
            miImagen = File.createTempFile(prefijoArchivo, ".jpg", directorioImagen);
            rutaImagenes = miImagen.getAbsolutePath();
            // Log.i("TAG", "FotoTomada" + rutaImagenes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return miImagen;
    }
}