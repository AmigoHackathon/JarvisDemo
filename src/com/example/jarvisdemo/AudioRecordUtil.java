package com.example.jarvisdemo;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.Process;
import android.util.Log;

public class AudioRecordUtil {

	private static final String TAG = "AudioRecordUtil";
	private AudioRecord audioRecorder;
	private DataOutputStream outputStream;
	private Thread recordThread;
	private boolean isStart = false;
	private static AudioRecordUtil mInstance;
	private static int bufferSize;
	
	public AudioRecordUtil() {
		bufferSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
		audioRecorder = new AudioRecord(AudioSource.MIC, 
										8000, 
										AudioFormat.CHANNEL_IN_MONO, 
										AudioFormat.ENCODING_PCM_16BIT, 
										bufferSize * 2);
	}
	/**
	 * Singleton method. 
	 * @return mInstance
	 */
	public synchronized static AudioRecordUtil getInstance() {
		if (mInstance == null) 
			mInstance = new AudioRecordUtil();
		return mInstance;
	}
	
	Runnable recordRunnable = new Runnable() {
		
		@Override
		public void run() {
			try {
				Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
				int bytesRecord;
				byte[] tempBuffer = new byte[bufferSize];
				if (audioRecorder.getState() != AudioRecord.STATE_INITIALIZED) {
					stopRecord();
					return;
				}
				audioRecorder.startRecording();
				Log.d(TAG, "Start recording. ");
				while (isStart) {
					if (audioRecorder != null) {
						bytesRecord = audioRecorder.read(tempBuffer, 0, bufferSize);
						if (bytesRecord == AudioRecord.ERROR_INVALID_OPERATION || bytesRecord == AudioRecord.ERROR_BAD_VALUE) 
							continue;
						else if (bytesRecord != 0 && bytesRecord != -1) {
							// write audio file
							outputStream.write(tempBuffer, 0, bytesRecord);
						} else 
							break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				Log.d(TAG, e.getMessage());
			}
		}
	};
	
	private void setPath(String path) throws Exception {
		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
	}
	
	private void startRecordThread() {
		destroyRecordThread();
		isStart = true;
		if (recordThread == null) {
			recordThread = new Thread(recordRunnable);
			recordThread.start();
		}
	}
	
	private void destroyRecordThread() {
		try {
			isStart = false;
			if (recordThread != null && recordThread.getState() == Thread.State.RUNNABLE) {
				try {
					Thread.sleep(500);
					recordThread.interrupt();
				} catch (Exception e) {
					recordThread = null;
				}
			}
			recordThread = null;
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, e.getMessage());
		} finally {
			recordThread = null;
		}
	}
	
	public void startRecord(String path) {
		try {
			setPath(path);
			startRecordThread();
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, e.getMessage());
		}
	}
	

	public void stopRecord() {
		try {
			isStart = false;
			destroyRecordThread();
			if (audioRecorder != null) {
				if (audioRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
					audioRecorder.stop();
				}
				audioRecorder.release();
			}
			if (outputStream != null) {
				outputStream.flush();
				outputStream.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, e.getMessage());
		}
	}
	
	
}
