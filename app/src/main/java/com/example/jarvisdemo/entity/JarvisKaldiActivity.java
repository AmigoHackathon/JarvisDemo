package com.example.jarvisdemo.entity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jarvisdemo.R;
import com.example.jarvisdemo.util.AudioRecordUtil;


public class JarvisKaldiActivity extends Activity {

	private AudioRecordUtil aUtil;
	private ImageButton button;
	private TextView jarvisGreetingView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jarvis_kaldi);

    	aUtil = AudioRecordUtil.getInstance();
        jarvisGreetingView = findViewById(R.id.text_greeting);
        button = findViewById(R.id.button_record);
        button.setOnTouchListener(new OnTouchListener() {
			
			@SuppressLint("ClickableViewAccessibility") 
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					button.setSelected(true);
					
					/*
					 * Option 2: Using IFlyTek SDK
					 */
//					startAudioRecordUsingIFlyTek();
					startAudioRecordUsingKaldi();
					break;
				case MotionEvent.ACTION_UP:
					button.setSelected(false);
					processAudio();
//					new RecordButtonRotateAsyncTask().executeOnExecutor(null, null);
					break;
				default:
					break;
				}
				return true;
			}

		});
    }

    
	private void startAudioRecordUsingKaldi() {
		Toast.makeText(getApplicationContext(), "Recording start. ", Toast.LENGTH_SHORT).show();
    	aUtil.startRecord();
    }
    
    private void processAudio() {
    	Toast.makeText(getApplicationContext(), "Released", Toast.LENGTH_SHORT).show();
		aUtil.stopRecord();
//		new UploadAudioAsyncTask().executeOnExecutor(null, null);
	}

}
