package com.meetpast.pcm2mp3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("pcm2mp3");
    }

    private static String TAG="recordtest";

    //采样率，现在能够保证在所有设备上使用的采样率是44100Hz, 但是其他的采样率（22050, 16000, 11025）在一些设备上也可以使用。
    public static final int SAMPLE_RATE_INHZ = 44100;
    //声道数。CHANNEL_IN_MONO and CHANNEL_IN_STEREO. 其中CHANNEL_IN_MONO是可以保证在所有设备能够使用的。
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO;
    //返回的音频数据的格式。 ENCODING_PCM_8BIT, ENCODING_PCM_16BIT, and ENCODING_PCM_FLOAT.
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private File recordFile;

    private AudioRecord audioRecord;
    private boolean isRecording;
    private Mp3Encoder mp3Encoder;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);
        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start:
                beginRecord();
                break;
            case R.id.stop:
                endRecord();
                break;
        }
    }

    private void beginRecord() {
        if(isRecording){
            Log.e(TAG, "startRecord: AudioRecorder has been already started");
            return;
        }
        recordFile = FileUtil.createFile(this, FileUtil.getPcmFileName(System.currentTimeMillis()));

        final int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        if(minBufferSize==AudioRecord.ERROR_BAD_VALUE){
            Log.e(TAG,"beginRecord : minBufferSize is error_bad_value");
        }
        Log.d(TAG,"minBufferSize : "+minBufferSize);
//        int bytesPerFrame = 2;
//        //获取的AudioRecord最小缓冲区包含的帧数
//        int frameSize = minBufferSize / bytesPerFrame;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT, minBufferSize);
        final byte[] data = new byte[minBufferSize];
        audioRecord.startRecording();
        isRecording = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                FileOutputStream outputStream=null;
                try {
                    outputStream =new FileOutputStream(recordFile);
                    byte[] temp;
                    while (isRecording) {
                        int read = audioRecord.read(data, 0, minBufferSize);
                        if (read>0) {
                            outputStream.write(data);
                        }
                    }
                    outputStream.flush();
                    outputStream.close();
                    convertFcmToMp3(recordFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private void convertFcmToMp3(File sourceFile) throws IOException {
        String  targetFilePath=FileUtil.createFile(this,FileUtil.getMP3FileName(System.currentTimeMillis())).getAbsolutePath();
        Log.d(TAG,"mp3-path："+targetFilePath);
        Mp3Encoder mp3Encoder=new Mp3Encoder();
       if( mp3Encoder.init(sourceFile.getAbsolutePath(),CHANNEL_CONFIG == AudioFormat.CHANNEL_IN_STEREO ? 2 : 1,128,SAMPLE_RATE_INHZ,targetFilePath)==0){
           Log.d(TAG,"MP3encoder-init:success");
           mp3Encoder.encode();
           mp3Encoder.destroy();
           Log.d(TAG, "MP3encoder-encode finish");
       }else {
           Log.d(TAG, "MP3encoder-encoder-init:failed");
       }
    }

    private void endRecord() {
        isRecording = false;
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }

}
