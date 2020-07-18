package com.example.lab11;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_CODE = 1000;
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;

    //Botones
    private Button btnGrabar;
    private Button btnParar;

    // formato de Sonido
    int BufferElements2Rec = 1024;
    // Formato de 16 bits
    int BytesPerElement = 2;

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
        enableButtons(false);

        int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
    }
    private void setButtonHandlers() {
        (btnGrabar).setOnClickListener(btnClick);
        (btnParar).setOnClickListener(btnClick);
    }

    private void enableButton(Button btn, boolean isEnable) {
        (btn).setEnabled(isEnable);
    }

    private void enableButtons(boolean isRecording) {
        enableButton(btnGrabar, !isRecording);
        enableButton(btnParar, isRecording);
    }

    // Graba
    private void startRecording() {

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

        recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                try {
                    // Guarda el audio en un archivo
                    writeAudioDataToFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    //Convierte de Short[] a Byte[]
    private byte[] short2byte(short[] sData) {
        int shortArrsize = sData.length;
        byte[] bytes = new byte[shortArrsize * 2];
        for (int i = 0; i < shortArrsize; i++) {
            bytes[i * 2] = (byte) (sData[i] & 0x00FF);
            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
            sData[i] = 0;
        }
        return bytes;

    }

    // Escribe el audio de salida en Bytes
    private void writeAudioDataToFile() throws IOException {

        String filePath = "/sdcard/audio-record.pcm";
        // Crear Fichero
       /* File filepath = Environment.getExternalStorageDirectory();
        File path = new File(filepath.getAbsolutePath() + "/LABORATORIO11/");
        //Verifica si el directorio esta creado
        if(!path.exists()){
            path.mkdirs();
        }*/
        short sData[] = new short[BufferElements2Rec];

        FileOutputStream audioR_ = null;
        try {
            audioR_ = new FileOutputStream(filePath);
            //Añadir codigo para convertir archivo PCM a WAV


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while (isRecording) {
            //Obtiene la salida de voz en el formato de short[]
            recorder.read(sData, 0, BufferElements2Rec);

            Log.d("MainActivity ", "Grabando audio" + sData.toString());
            try{
                // Escribe los datos al archivo desde el búfer
                // Almacena el Bufer de voz
                byte bData[] = short2byte(sData);
                //audioR_.write(bData);
                audioR_.write(bData, 0, BufferElements2Rec * BytesPerElement);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        try {
            audioR_.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Detiene la grabación
    private void stopRecording() {
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
    }

    //Funcionalidad al hacer touch en el boton
    private View.OnClickListener btnClick = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_Grabar: {
                    ((TextView)findViewById(R.id.txtview)).setText("Grabando");
                    enableButtons(true);
                    startRecording();
                    break;
                }
                case R.id.btn_parar: {
                    ((TextView)findViewById(R.id.txtview)).setText("Grabación Detenida");
                    enableButtons(false);
                    stopRecording();

                    // COnvertir AUDIO PCM a WAV
                    File audioRecord = new File("/sdcard/audio-record.pcm");
                    File audioWav = new File("/sdcard/audio-record.wav");
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
                    Toast.makeText(this, "PERMISSION GRANTED", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(this, "PERMISSION DENIED", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }
}