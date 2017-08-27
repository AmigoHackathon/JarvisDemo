package com.example.jarvisdemo.ui.main;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jarvisdemo.R;
import com.example.jarvisdemo.R.id;
import com.example.jarvisdemo.R.layout;
import com.example.jarvisdemo.R.string;
import com.example.jarvisdemo.utils.AudioRecordUtil;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;


public class JarvisEntityActivity extends Activity {

	private static final String TAG_IFLYTEK = JarvisEntityActivity.class.getSimpleName();
	private static final String TAG_KALDI = TAG_IFLYTEK;
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	
	private AudioRecordUtil aUtil;
	private SpeechRecognizer mRecognizer;
	private ImageButton button;
	private TextView jarvisGreetingView;

	private static final int RECORDER_SAMPLERATE = 8000;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private AudioRecord recorder = null;
	private Thread recordingThread = null;
	private boolean isRecording = false;
	
	BroadcastReceiver receiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO receive processed result from server
			
		}
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	// IFlyTek settings
    	SpeechUtility.createUtility(this, SpeechConstant.APPID + getString(R.string.iflytek_appid));
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        setIflyTekRecognizer();
    	aUtil = AudioRecordUtil.getInstance();
        jarvisGreetingView = findViewById(R.id.text_greeting);
        button = findViewById(R.id.button_record);
		int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
				RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
		
		button.setOnTouchListener(new OnTouchListener() {

			@SuppressLint("ClickableViewAccessibility") 
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					button.setSelected(true);
					/*
					 * Option 1: Using Google Android speech recognition service
					 */
					
					/*
					 * Option 2: Using IFlyTek SDK
					 */
//					startAudioRecordUsingIFlyTek();
					
					/*
					 * Option 3
					 */
					startAudioRecordUsingKaldi();
					break;
				case MotionEvent.ACTION_UP:
					button.setSelected(false);
					processAudio();
					break;
				default:
					break;
				}
				return true;
			}

		});
    }

    private void setIflyTekRecognizer() {
    	mRecognizer = SpeechRecognizer.createRecognizer(this, new InitListener() {
			
			@Override
			public void onInit(int code) {
				Log.d(TAG_IFLYTEK, "SpeechRecognizer init() code: " + code);
				if (code != ErrorCode.SUCCESS) 
					Toast.makeText(getApplicationContext(), 
							"Failure in initialization with Code " + code, Toast.LENGTH_SHORT).show();
			}
		});
    	
    	mRecognizer.setParameter(SpeechConstant.PARAMS, null);
    	mRecognizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
    	mRecognizer.setParameter(SpeechConstant.RESULT_TYPE, "json");
    	mRecognizer.setParameter(SpeechConstant.LANGUAGE, "en_us");
    	// set timeout after user pressed the button
    	mRecognizer.setParameter(SpeechConstant.VAD_BOS, "4000");
    	// set timeout after user finish speaking
    	mRecognizer.setParameter(SpeechConstant.VAD_EOS, "1000");
    	mRecognizer.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
    	mRecognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH, 
    			Environment.getExternalStorageDirectory() + String.format("/msc/iat_%s.wav", System.currentTimeMillis()));
    	
	}

    private void startAudioRecordUsingIFlyTek() {
    	
    	if (mRecognizer == null) 
    		Toast.makeText(getApplicationContext(), "Failure on creating SpeechRecognizer. ", Toast.LENGTH_SHORT).show();
    	mRecognizer.startListening(new RecognizerListener() {
    		
    		@Override
    		public void onVolumeChanged(int volume, byte[] data) {
    			Toast.makeText(getApplicationContext(), "Speeking in Volume " + volume, Toast.LENGTH_SHORT).show();
    			Log.d(TAG_IFLYTEK, "Audio data: " + data.length);
    		}
    		
    		@Override
    		public void onResult(RecognizerResult result, boolean isLast) {
    			// TODO 
    		}
    		
    		@Override
    		public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
    			// TODO Auto-generated method stub
    			
    		}
    		
    		@Override
    		public void onError(SpeechError error) {
    			Toast.makeText(getApplicationContext(), error.getPlainDescription(true), Toast.LENGTH_SHORT).show();
    		}
    		
    		@Override
    		public void onEndOfSpeech() {
    			Toast.makeText(getApplicationContext(), "Speech over", Toast.LENGTH_SHORT).show();
    		}
    		
    		@Override
    		public void onBeginOfSpeech() {
    			Toast.makeText(getApplicationContext(), "Recording", Toast.LENGTH_SHORT).show();
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
	}
    
    @Override
    protected void onDestroy() {
    	// TODO release all resources
    	super.onDestroy();
    	if (mRecognizer != null) {
			mRecognizer.cancel();
			mRecognizer.destroy();
		}
    	
    }
}
