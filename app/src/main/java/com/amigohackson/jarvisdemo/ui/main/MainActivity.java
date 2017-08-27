package com.amigohackson.jarvisdemo.ui.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.Toast;


import com.amigohackson.jarvisdemo.R;
import com.amigohackson.jarvisdemo.utils.AudioRecordUtil;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class MainActivity extends Activity {
    public static final int REQUEST_PERMISSION_CODE = 1;
	
	private AudioRecordUtil recordUtil;
	private ImageButton voiceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    	recordUtil = AudioRecordUtil.getInstance();
        voiceButton = findViewById(R.id.button_record);
        voiceButton.setOnTouchListener(new OnTouchListener() {
			
			@SuppressLint("ClickableViewAccessibility") 
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					voiceButton.setSelected(true);
					if(!checkPermission()){
                        requestPermission();
                    }
					startAudioRecording();
					break;
				case MotionEvent.ACTION_UP:
					voiceButton.setSelected(false);
                    stopAudioRecording();
					processAudio();
					break;
				default:
					break;
				}
				return true;
			}

		});
    }
    
	private void startAudioRecording() {
    	recordUtil.startRecording();
        Toast.makeText(getApplicationContext(), "Recording start. ", Toast.LENGTH_SHORT).show();
    }

    private void stopAudioRecording(){
        recordUtil.stopRecording();
	}

    private void processAudio() {
        Thread playThread = new Thread(new Runnable() {
            public void run() {
                recordUtil.playAudio();
                recordUtil.transcodeRawToWav();
            }
        }, "AudioRecorder Thread");
        playThread.start();
    	Toast.makeText(getApplicationContext(), "Released", Toast.LENGTH_SHORT).show();
	}

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, REQUEST_PERMISSION_CODE);
    }
}
