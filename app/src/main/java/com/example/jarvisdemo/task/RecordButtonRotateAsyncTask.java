package com.example.jarvisdemo.task;

import com.example.jarvisdemo.R;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;

public class RecordButtonRotateAsyncTask extends AsyncTask<String, Integer, Bitmap>{

	@Override
	protected Bitmap doInBackground(String... params) {
//		Animation rotateAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate);
//		rotateAnimation.setInterpolator(new LinearInterpolator());
		return null;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
	}
	
}
