package com.example.jarvisdemo.task;

import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import android.os.AsyncTask;
import android.util.Log;

public class UploadAudioAsyncTask extends AsyncTask<String, Integer, byte[]>{

	@Override
	protected byte[] doInBackground(String... params) {
		try {
			URL url = new URL(params[0]);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
