package com.example.lab11;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE = 1000;
    //Botones
    private Button btnGrabar;
    private Button btnParar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Verifica si se tiene los permisos requeridos
        if (checkPermision()){

        }else{
            requestPermission();
        }

        btnGrabar = (Button)findViewById(R.id.btn_Grabar);
        btnParar = (Button)findViewById(R.id.btn_parar);
        setButtonHandlers();
    }
    private void setButtonHandlers() {
        (btnGrabar).setOnClickListener(btnClick);
        (btnParar).setOnClickListener(btnClick);
    }

    //Funcionalidad al hacer touch en el boton
    private View.OnClickListener btnClick = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_Grabar: {
                    ((TextView)findViewById(R.id.txtview)).setText("Grabando");
                    break;
                }
                case R.id.btn_parar: {
                    ((TextView)findViewById(R.id.txtview)).setText("Grabación Detenida");
                    break;
                }
            }
        }
    };

    //PERMISOS
    private boolean checkPermision(){
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return write_external_storage_result == PackageManager.PERMISSION_GRANTED && record_audio_result == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        },REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_PERMISSION_CODE:
            {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Permiso Concedido", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(this, "Permiso Denegado", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
}
