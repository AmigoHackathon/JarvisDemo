package com.example.jarvisdemo.utils;

import android.media.MediaRecorder;

public class MediaRecordUtil {

	private MediaRecorder mRecorder;
	private static MediaRecordUtil mInstance;

	public MediaRecordUtil() {
		// TODO Auto-generated constructor stub
	}
	
	public static MediaRecordUtil getInstance() {
		if (mInstance == null) 
			mInstance = new MediaRecordUtil();
		return mInstance;
	}
	
	public void startRecord() {
		// TODO Auto-generated method stub
		if (mRecorder == null) {
			mRecorder = new MediaRecorder();
		}
		try {
			
		} catch (Exception e) {
			// TODO: handle exception
		}

	}
	
	public void stopRecord() {
		try {
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
}
