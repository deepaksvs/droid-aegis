package andi.cctv.app;

import java.io.FileDescriptor;
import java.io.IOException;

import android.annotation.TargetApi;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;

public class MediaController implements OnErrorListener{
	private static final int 	State_Unknown = 0;
	private static final int	State_Ready   = 1;
	private static final int	State_Play    = 2;
	private static final int	State_Stop    = 3;
	
	private static final String mRecTag = "cctv";
	
	private int				mCurState;
	/* Need to have camera and mediarecord objects */
	private Camera			mCam;
	private MediaRecorder 	mRec;
	private Thread			mThread;
	private SurfaceHolder	mSurfaceHolder;
	private FileDescriptor	mFd;
	private int				mWidth, mHeight;
	
	@Deprecated
	public MediaController(SurfaceHolder  mHolder, FileDescriptor mFileD) {
		mCurState = State_Unknown;
		mSurfaceHolder = mHolder;
		mFd = mFileD;
		mWidth = 176;
		mHeight = 144;
		Log.d(mRecTag, "Creating thread: SurfaceHolder");
		mThread = new ThreadHandler();
		mThread.start();
	}

	public MediaController (SurfaceHolder mHolder) {
		mCurState = State_Unknown;
		mSurfaceHolder = mHolder;

	}
	
	public void setFileDescriptor (FileDescriptor mFileDesc) {
		mFd = mFileDesc;
	}
	
	public void setPreviewSize (int width, int height) {
		mWidth = width;
		mHeight = height;
	}
	
	public void startRec () {
		mThread = new ThreadHandler();
		mThread.start();
	}

	public void stopMedia() {
		Log.d(mRecTag, "StopMedia called");
		mCurState = State_Stop;
		synchronized (mThread) {
			mThread.notifyAll();
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private boolean InitialiseMedia () {
		boolean initSuccess = false;
		/*Log.d(mRecTag, "Camera Open");
		int n = Camera.getNumberOfCameras();
		Log.d(mRecTag, "Cameras = " + n);
		CameraInfo[] mInfo = new CameraInfo[n];
		for (int i = 0; i < n; i++) {
			mInfo[i] = new CameraInfo();
			Log.d(mRecTag, "Calling getinfo");
			Camera.getCameraInfo(i, mInfo[i]);
			Log.d(mRecTag, "facing " + mInfo[i].facing);
			Log.d(mRecTag, "orientation " + mInfo[i].orientation);
		}*/
		/* Try opening the main camera */
		Log.d(mRecTag, "Trying BACK camera");
		mCam = Camera.open(CameraInfo.CAMERA_FACING_BACK);
//		mCam = Camera.open();
		if (null == mCam) {
			Log.d(mRecTag, "Trying FRONT Camera");
			mCam = Camera.open(CameraInfo.CAMERA_FACING_FRONT);
		}
		if (null != mCam) {
			Log.d(mRecTag, "Camera Open Success");
//			CamcorderProfile  myProfile = CamcorderProfile.get(CameraInfo.CAMERA_FACING_BACK, CamcorderProfile.QUALITY_QCIF);
			Camera.Parameters params = mCam.getParameters();
			params.setPreviewSize(mWidth, mHeight);
			mCam.setParameters(params);
//			params.getPreviewSize();
			Log.d(mRecTag, "Surface start");
			try {
				Log.d(mRecTag, "SetPreview: SurfaceHolder");
				mCam.setPreviewDisplay(mSurfaceHolder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mCam.startPreview();
			Log.d(mRecTag, "Started Preview");
			initSuccess = true;
		}
		else {
			Log.d(mRecTag, "Camera Open FAILED for both");
		}
		return initSuccess;
	}
	
	private void CleanupMedia () {
		Log.d(mRecTag, "Stop + release Camera");
		mCam.stopPreview();
		mCam.release();
	}
	
	private void setUpRecorder () {
		Log.d(mRecTag, "Initialising of record");
		mRec = new MediaRecorder();
		mCam.unlock();
		mRec.setCamera(mCam);
		mRec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		mRec.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
		mRec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		mRec.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		mRec.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		mRec.setVideoFrameRate(15);
//		mRec.setVideoSize( , );
		mRec.setOutputFile(mFd);
		try {
			mRec.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private class ThreadHandler extends Thread {

		@Override
		public void run() {
			boolean mRun = true;
			Log.d(mRecTag, "Run of thread");
			if (true == InitialiseMedia()) {
				setUpRecorder();
				Log.d(mRecTag, "Start Record");
				mRec.start();
				mCurState = State_Play;
			}
			else {
				Log.e(mRecTag, "Initialise failed. What to do !?");
				mCurState = State_Unknown;
//				mRun = false;
			}
			
			while (mRun) {
				try {
					synchronized (this) {
						this.wait();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				/* Check if synchronization is required */
				switch (mCurState) {
				case State_Ready:
					/* Do Nothing */

					break;
				case State_Play:
					/* Nothing to do .. Handler errors */
					break;
				case State_Stop:
					Log.d(mRecTag, "stop record");
					mRec.stop();
					mRec.reset();
					mRec.release();
					/* Stop recording and cleanup */
					try {
						mCam.reconnect();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					mRun = false;
					break;
				default:
					break;
				}
				if (mCurState != State_Unknown) {
					CleanupMedia();
				}
				
			}
		}
		
	}

	@Override
	public void onError(MediaRecorder mr, int what, int extra) {
		// TODO Auto-generated method stub
		Log.d(mRecTag, "MediaRecorder OnError: " + what);
		Log.d(mRecTag, "Extra " + extra);
	}
	
}
