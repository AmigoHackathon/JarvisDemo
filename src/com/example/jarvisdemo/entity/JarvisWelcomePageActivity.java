package com.example.jarvisdemo.entity;

import android.app.Activity;
import android.os.Bundle;

import com.example.jarvisdemo.R;

public class JarvisWelcomePageActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jarvis_welcome_page);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
}
