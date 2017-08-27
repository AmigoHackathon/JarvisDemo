package com.example.jarvisdemo.entity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.example.jarvisdemo.R;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;

public class JarvisIFlyTekActivity extends Activity {

    private static final String TAG_IFLYTEK = JarvisKaldiActivity.class.getSimpleName();
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    private SpeechRecognizer mRecognizer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // IFlyTek settings
        SpeechUtility.createUtility(this, SpeechConstant.APPID + getString(R.string.iflytek_appid));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jarvis_ifly_tek);
        setIFlyTekRecognizer();


    }

    private void setIFlyTekRecognizer() {
        mRecognizer = SpeechRecognizer.createRecognizer(this, new InitListener() {

            @Override
            public void onInit(int code) {
                Log.d(TAG_IFLYTEK, "SpeechRecognizer init() code: " + code);
                if (code != ErrorCode.SUCCESS)
                    Toast.makeText(getApplicationContext(),
                            "Fail in initialization with Code " + code, Toast.LENGTH_SHORT).show();
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
                Environment.getExternalStorageDirectory() + String.format("/msc/jarvis_iflytek_%s.wav", System.currentTimeMillis()));

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
