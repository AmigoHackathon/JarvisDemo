package com.example.jarvisdemo;

import android.media.MediaRecorder;

public class MediaRecordUtil {

	private MediaRecorder mRecorder;

	public MediaRecordUtil() {
		// TODO Auto-generated constructor stub
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
