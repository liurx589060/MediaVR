package com.example.liurunxiong.mediavr;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.MediaPlayer.OnVideoSizeChangedListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.media.AudioManager;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class VitamioVideo_local extends Activity implements SurfaceHolder.Callback,OnBufferingUpdateListener,OnCompletionListener
,OnPreparedListener,OnVideoSizeChangedListener{
	
	private MediaPlayer mMediaPlayer;
	private SurfaceHolder holder;
	private SurfaceView mSurfaceView;
	
	private int mVideoWidth;
	private int mVideoHeight;
	
	
	private boolean mIsVideoSizeKnown = false;
	private boolean mIsVideoReadyToBePlayed = false;
	    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (!LibsChecker.checkVitamioLibs(this))
			return;
				
		setContentView(R.layout.activity_video_local);
		
		mSurfaceView = (SurfaceView) findViewById(R.id.surface);
		holder = mSurfaceView.getHolder();
		holder.addCallback(this);
		holder.setFormat(PixelFormat.RGBA_8888);
	}
	
	@SuppressLint("NewApi")
	private void playVideo() {
		doCleanUp();
		//String path = Environment.getExternalStorageDirectory().getPath() + "/a.mp4";
		/*String path = "http://static.youku.com/v1.0.0638/v/swf/loader.swf?VideoIDS=XNzYyMDc3MzY4&winType=BDskin" +
				"&embedid=MTgzLjE2LjIuMzcCMTkwNTE5MzQyAgI%3D&wd=&partnerid=0edbfd2e4fc91b72&vext=pid%3D0edbfd2e4fc" +
				"91b72%26emb%3DMTgzLjE2LjIuMzcCMTkwNTE5MzQyAg" +
				"I%3D%26bc%3D%26cna%3Dq1eLD+kVcV4CAbcQnJgjkwZD%26type%3D0%26embsig%3D1_1469155396_0ca47f620bb03444fd7dad" +
				"c8e2ea7341";*/
		String path = "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_175k.mov";
		try {
			mMediaPlayer = new MediaPlayer(this);
			mMediaPlayer.setDataSource(path);
			mMediaPlayer.setDisplay(holder);			
			mMediaPlayer.prepareAsync();
			mMediaPlayer.setOnBufferingUpdateListener(this);
			mMediaPlayer.setOnCompletionListener(this);
			mMediaPlayer.setOnPreparedListener(this);
			mMediaPlayer.setOnVideoSizeChangedListener(this);
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		Log.e("yy", "surfaceCreated called");
		playVideo();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer arg0, int width, int height) {
		// TODO Auto-generated method stub
		Log.e("yy", "onVideoSizeChanged called  width=" + width + "---height=" + height);
		if (width == 0 || height == 0) {
			Log.e("yy", "invalid video width(" + width + ") or height(" + height + ")");
			return;
		}
		mIsVideoSizeKnown = true;
		mVideoWidth = width;
		mVideoHeight = height;
		if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
			startVideoPlayback();
		}
	}

	@Override
	public void onPrepared(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		Log.e("yy", "onPrepared called");
		mIsVideoReadyToBePlayed = true;
		if (mIsVideoReadyToBePlayed && mIsVideoSizeKnown) {
			startVideoPlayback();
		}
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), "已播放完", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	
	private void startVideoPlayback() {
		Log.e("yy", "startVideoPlayback");
		holder.setFixedSize((int)(mVideoWidth*2.0), (int)(mVideoHeight*2.0));
		//holder.setFixedSize((int)(1800), (int)(900));
		mMediaPlayer.start();
		Log.e("yy", "duration=" + mMediaPlayer.getDuration());
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		releaseMediaPlayer();
		doCleanUp();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		releaseMediaPlayer();
		doCleanUp();
	}
	
	private void releaseMediaPlayer() {
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	private void doCleanUp() {
		mVideoWidth = 0;
		mVideoHeight = 0;
		mIsVideoReadyToBePlayed = false;
		mIsVideoSizeKnown = false;
	}
}
