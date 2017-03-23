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
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class VRVideoActivity extends Activity implements OnBufferingUpdateListener,OnCompletionListener
,OnPreparedListener,OnVideoSizeChangedListener,OnFrameAvailableListener{
	
	private TextView mTextView;
	
	private MediaPlayer mMediaPlayer;	
	
	private boolean mIsVideoSizeKnown = false;
	private boolean mIsVideoReadyToBePlayed = false;
	
	
	
	private boolean frameAvailable = false;
	private GLSurfaceView glView; 
	private SurfaceTexture videoTexture;
	 int textureParamHandle;
	    int textureCoordinateHandle;
	    int positionHandle;
	    int textureTranformHandle;

	    /**
	     *
	     */
	    private static float squareSize = 1.0f;
	    private static float squareCoords[] = {
	            -squareSize, squareSize,   // top left
	            -squareSize, -squareSize,   // bottom left
	            squareSize, -squareSize,    // bottom right
	            squareSize, squareSize}; // top right

	    private static short drawOrder[] = {0, 1, 2, 0, 2, 3};

	    private Context context;

	    // Texture to be shown in backgrund
	    private FloatBuffer textureBuffer;
	    private float textureCoords[] = {
	            0.0f, 1.0f, 0.0f, 1.0f,
	            0.0f, 0.0f, 0.0f, 1.0f,
	            1.0f, 0.0f, 0.0f, 1.0f,
	            1.0f, 1.0f, 0.0f, 1.0f};
	    private int[] textures = new int[1];

	    private int width, height;

	    private int shaderProgram;
	    private FloatBuffer vertexBuffer;
	    private ShortBuffer drawListBuffer;
	    private float[] videoTextureTransform = new float[16];
	    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if (!LibsChecker.checkVitamioLibs(this))
			return;
		
		context = this;
		glView = new GLSurfaceView(this);
		glView.setEGLContextClientVersion(2);
		glView.setRenderer(new VRStereoRender());
		setContentView(glView);
	}
	
	@SuppressLint("NewApi")
	private void playVideo() {
		doCleanUp();
		String path = Environment.getExternalStorageDirectory().getPath() + "/a.mp4";
		/*String path = "http://static.youku.com/v1.0.0638/v/swf/loader.swf?VideoIDS=XNzYyMDc3MzY4&winType=BDskin" +
				"&embedid=MTgzLjE2LjIuMzcCMTkwNTE5MzQyAgI%3D&wd=&partnerid=0edbfd2e4fc91b72&vext=pid%3D0edbfd2e4fc" +
				"91b72%26emb%3DMTgzLjE2LjIuMzcCMTkwNTE5MzQyAg" +
				"I%3D%26bc%3D%26cna%3Dq1eLD+kVcV4CAbcQnJgjkwZD%26type%3D0%26embsig%3D1_1469155396_0ca47f620bb03444fd7dad" +
				"c8e2ea7341";*/
		
		//String path = "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_175k.mov";
		try {
			mMediaPlayer = new MediaPlayer(this);
			mMediaPlayer.setDataSource(path);
			mMediaPlayer.prepareAsync();
			mMediaPlayer.setOnBufferingUpdateListener(this);
			mMediaPlayer.setOnCompletionListener(this);
			mMediaPlayer.setOnPreparedListener(this);
			mMediaPlayer.setOnVideoSizeChangedListener(this);
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			
			Surface surface = new Surface(videoTexture);
			mMediaPlayer.setSurface(surface);
		} catch (Exception e) {
			// TODO: handle exception
		}
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
		
		View view = LayoutInflater.from(this).inflate(R.layout.activity_vr_controlview, null);
		mTextView = (TextView) view.findViewById(R.id.textView1);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		addContentView(view, params);
	}

	@Override
	public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	
	private void startVideoPlayback() {
		Log.e("yy", "startVideoPlayback");
		mMediaPlayer.start();
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
		mIsVideoReadyToBePlayed = false;
		mIsVideoSizeKnown = false;
	}
	
	
	
	public class VRStereoRender implements Renderer {

		@Override
		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			// TODO Auto-generated method stub
			setupGraphics();
	        setupVertexBuffer();
	        setupTexture();
			playVideo();
		}

		@Override
		public void onSurfaceChanged(GL10 gl, int width, int height) {
			// TODO Auto-generated method stub
			VRVideoActivity.this.width = width;
			VRVideoActivity.this.height = height;
			
//			playVideo();
		}

		@Override
		public void onDrawFrame(GL10 gl) {
			// TODO Auto-generated method stub
			synchronized (this) {
	            if (frameAvailable) {
	                videoTexture.updateTexImage();
	                videoTexture.getTransformMatrix(videoTextureTransform);
	                frameAvailable = false;
	            }
	            
	            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
	            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

	            GLES20.glViewport(0, 0, width/2, height);
	            drawTexture();

	            GLES20.glViewport(width/2, 0, width/2, height);
	            drawTexture();
	        }
		}
		
	}
	
	 private void setupGraphics() {
	        final String vertexShader = RawResourceReader.readTextFileFromRawResource(context, R.raw.vetext_sharder);
	        final String fragmentShader = RawResourceReader.readTextFileFromRawResource(context, R.raw.fragment_sharder);

	        final int vertexShaderHandle = ShaderHelper.compileShader(GLES20.GL_VERTEX_SHADER, vertexShader);
	        final int fragmentShaderHandle = ShaderHelper.compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
	        shaderProgram = ShaderHelper.createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle,
	                new String[]{"texture", "vPosition", "vTexCoordinate", "textureTransform"});

	        GLES20.glUseProgram(shaderProgram);
	        textureParamHandle = GLES20.glGetUniformLocation(shaderProgram, "texture");
	        textureCoordinateHandle = GLES20.glGetAttribLocation(shaderProgram, "vTexCoordinate");
	        positionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition");
	        textureTranformHandle = GLES20.glGetUniformLocation(shaderProgram, "textureTransform");
	 }
	 
	 private void setupVertexBuffer() {
	        // Draw list buffer
	        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
	        dlb.order(ByteOrder.nativeOrder());
	        drawListBuffer = dlb.asShortBuffer();
	        drawListBuffer.put(drawOrder);
	        drawListBuffer.position(0);

	        // Initialize the texture holder
	        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
	        bb.order(ByteOrder.nativeOrder());

	        vertexBuffer = bb.asFloatBuffer();
	        vertexBuffer.put(squareCoords);
	        vertexBuffer.position(0);
	    }
	 
	 @SuppressLint("NewApi")
	private void setupTexture() {
	        ByteBuffer texturebb = ByteBuffer.allocateDirect(textureCoords.length * 4);
	        texturebb.order(ByteOrder.nativeOrder());

	        textureBuffer = texturebb.asFloatBuffer();
	        textureBuffer.put(textureCoords);
	        textureBuffer.position(0);

	        // Generate the actual texture
	        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	        GLES20.glGenTextures(1, textures, 0);
	        checkGlError("Texture generate");

	        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
	        checkGlError("Texture bind");

	        videoTexture = new SurfaceTexture(textures[0]);
	        videoTexture.setOnFrameAvailableListener(this);
	    }

	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture) {
		// TODO Auto-generated method stub
		synchronized (this) {
            frameAvailable = true;
        }
	}
	
	 public void checkGlError(String op) {
	        int error;
	        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
	            Log.e("SurfaceTest", op + ": glError " + GLUtils.getEGLErrorString(error));
	        }
	    }
	 
	 private void drawTexture() {
	        // Draw texture

	        GLES20.glEnableVertexAttribArray(positionHandle);
	        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);

	        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
	        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	        GLES20.glUniform1i(textureParamHandle, 0);

	        GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
	        GLES20.glVertexAttribPointer(textureCoordinateHandle, 4, GLES20.GL_FLOAT, false, 0, textureBuffer);

	        GLES20.glUniformMatrix4fv(textureTranformHandle, 1, false, videoTextureTransform, 0);

	        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
	        GLES20.glDisableVertexAttribArray(positionHandle);
	        GLES20.glDisableVertexAttribArray(textureCoordinateHandle);
	    }

}
