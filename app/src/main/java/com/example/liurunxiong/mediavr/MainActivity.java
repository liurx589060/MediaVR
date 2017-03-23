package com.example.liurunxiong.mediavr;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	private Button mVideoLocalBn;
	private Button mVideoViewBn;
	private Button mVRButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mVideoLocalBn = (Button) findViewById(R.id.video_local);
		mVideoLocalBn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,VitamioVideo_local.class);
				startActivity(intent);
			}
		});
		
		mVideoViewBn = (Button) findViewById(R.id.videoView);
		mVideoViewBn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,VitamioVideo_VedioView.class);
				startActivity(intent);
			}
		});
		
		mVRButton = (Button) findViewById(R.id.vrPlayer);
		mVRButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this,VRVideoActivity.class);
				startActivity(intent);
			}
		});
	}
}
