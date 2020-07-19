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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {
    // variables para audio y permisos
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
    // habilitar el boton respectivo segun el estado de isRecording
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
        short sData[] = new short[BufferElements2Rec];
        FileOutputStream audioR_ = null;
        try {
            audioR_ = new FileOutputStream(filePath);
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
                    try {
                        rawToWave(audioRecord, audioWav);
                    } catch (IOException e){
                        e.printStackTrace();
                    }
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
    // Espera los permisos del usuario
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
                    Toast.makeText(this, "PERMISION GRANTED", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(this, "PERMISION DENIED", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    //------------------------------------------------------------------------------------//
    //Codigo para convertir de PCM a WAV y que se pueda reproducir

    private void rawToWave(final File rawFile, final File waveFile) throws IOException {

        byte[] rawData = new byte[(int) rawFile.length()];
        DataInputStream input = null;
        try {
            input = new DataInputStream(new FileInputStream(rawFile));
            input.read(rawData);
        } finally {
            if (input != null) {
                input.close();
            }
        }

        DataOutputStream output = null;
        try {
            output = new DataOutputStream(new FileOutputStream(waveFile));
            // WAVE header
            // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + rawData.length); // chunk size
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 1); // number of channels
            writeInt(output, 44100); // sample rate
            writeInt(output, RECORDER_SAMPLERATE * 2); // byte rate
            writeShort(output, (short) 2); // block align
            writeShort(output, (short) 16); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, rawData.length); // subchunk 2 size
            // Audio data (conversion big endian -> little endian)
            short[] shorts = new short[rawData.length / 2];
            ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
            ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
            for (short s : shorts) {
                bytes.putShort(s);
            }
            output.write(fullyReadFileToBytes(rawFile));
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    // Recibe un archivo y este método divide el archivo en un array de bytes
    // para ser procesado en la salida del método principal.
    byte[] fullyReadFileToBytes(File f) throws IOException {
        int size = (int) f.length();
        byte bytes[] = new byte[size];
        byte tmpBuff[] = new byte[size];
        FileInputStream fis= new FileInputStream(f);
        try {

            int read = fis.read(bytes, 0, size);
            if (read < size) {
                int remain = size - read;
                while (remain > 0) {
                    read = fis.read(tmpBuff, 0, remain);
                    System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
                    remain -= read;
                }
            }
        }  catch (IOException e){
            throw e;
        } finally {
            fis.close();
        }
        return bytes;
    }
    // Estos metodos permiten cambiar el formato de PCM a WAV segun los valores de los array int
    private void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    private void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    private void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }
}