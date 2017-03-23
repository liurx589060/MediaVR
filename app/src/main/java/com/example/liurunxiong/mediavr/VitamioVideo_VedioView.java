package com.example.liurunxiong.mediavr;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnTimedTextListener;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class VitamioVideo_VedioView extends Activity {
	
	private VideoView mVideoView;
	
	private long mPosition = 0;
	private int mVideoLayout = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (!LibsChecker.checkVitamioLibs(this))
			return;
		setContentView(R.layout.activity_video_videoview);
		
		mVideoView = (VideoView) findViewById(R.id.surface_view);
		
		//String path = "http://live.gslb.letv.com/gslb?stream_id=hunan&tag=live&ext=m3u8&sign=live_tv";
		String path = "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_175k.mov";
		
		mVideoView.setVideoPath(path);
		mVideoView.requestFocus();
		mVideoView.setMediaController(new MediaController(this));
		mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mediaPlayer) {
				// optional need Vitamio 4.0
				mediaPlayer.setPlaybackSpeed(1.0f);
				//mVideoView.addTimedTextSource(subtitle_path);
				mVideoView.setTimedTextShown(true);
				Log.e("yy", "here");

			}
		});
		
		mVideoView.setOnTimedTextListener(new OnTimedTextListener() {

			@Override
			public void onTimedText(String text) {
				//mSubtitleView.setText(text);
				
			}

			@Override
			public void onTimedTextUpdate(byte[] pixels, int width, int height) {

			}
		});
	}
	
	@Override
	protected void onPause() {
		mPosition = mVideoView.getCurrentPosition();
		mVideoView.stopPlayback();
		super.onPause();
	}

	@Override
	protected void onResume() {
		if (mPosition > 0) {
			mVideoView.seekTo(mPosition);
			mPosition = 0;
		}
		super.onResume();
		mVideoView.start();
	}
	
	public void changeLayout(View view) {
		mVideoLayout++;
		if (mVideoLayout == 4) {
			mVideoLayout = 0;
		}
		switch (mVideoLayout) {
		case 0:
			mVideoLayout = VideoView.VIDEO_LAYOUT_ORIGIN;
			//view.setBackgroundResource(R.drawable.mediacontroller_sreen_size_100);
			break;
		case 1:
			mVideoLayout = VideoView.VIDEO_LAYOUT_SCALE;
			//view.setBackgroundResource(R.drawable.mediacontroller_screen_fit);
			break;
		case 2:
			mVideoLayout = VideoView.VIDEO_LAYOUT_STRETCH;
			//view.setBackgroundResource(R.drawable.mediacontroller_screen_size);
			break;
		case 3:
			mVideoLayout = VideoView.VIDEO_LAYOUT_ZOOM;
			//view.setBackgroundResource(R.drawable.mediacontroller_sreen_size_crop);

			break;
		}
		mVideoView.setVideoLayout(mVideoLayout, 0);
	}

}
