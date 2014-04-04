package com.app.camstreamer;

import java.io.IOException;
import android.hardware.Camera;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;

public class MockCamera {
	private static final String tag = "MockCamera";
	private Camera		mCam;
	private int			mWidth, mHeight;
	private CameraErrors	mCamErr;
	private PreviewHandler	mPrevhandler;
	
	public MockCamera() {
		// TODO Auto-generated constructor stub
		mCam = null;
		mWidth = mHeight = 0;
		mCamErr = new CameraErrors();
		mPrevhandler = new PreviewHandler();
	}
	
	public boolean initCamera () {
		mCam = Camera.open();
		if (mCam == null) {
			Log.d(tag, "Failed to open Camera");
			return false;
		}
		mCam.setErrorCallback(mCamErr);
		return true;
	}
	
	public void setMockPreviewSize (int width, int height) {
		mWidth = width;
		mHeight = height;
	}
	
	@SuppressWarnings("deprecation")
	public void doPreview (SurfaceHolder mHolder) {
		try {
			mCam.setPreviewDisplay(mHolder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Camera.Parameters	mParams = mCam.getParameters();
//		Log.d(tag, "Picture format " + mParams.getPictureFormat());
//		Size = mParams.getPreviewSize();

		mParams.setPictureSize(mWidth, mHeight);
//		mParams.setPictureFormat(ImageFormat.NV16);
//		List<Integer> pitformat = mParams.getSupportedPictureFormats();
//		List<Integer> prvFormat = mParams.getSupportedPreviewFormats();
//		Log.d(tag, "Picture format " + Arrays.toString(picformat.toArray()));
//		Log.d(tag, "items = " + prvFormat.size() + " Preview format " + Arrays.toString(prvFormat.toArray()));
		mParams.setPreviewFrameRate(15);
		mCam.setParameters(mParams);
		mParams = mCam.getParameters();
		mCam.setPreviewCallback(mPrevhandler);
		mCam.startPreview();
//		Size sz = mParams.getPreviewSize();
//		Log.d(tag, "Preview Size w = " + sz.height + " h = " + sz.height);
//		Log.d(tag, "BytesPerPixel = " + ImageFormat.getBitsPerPixel(mParams.getPreviewFormat()));
//		Log.d(tag, "Preview format " + mParams.getPreviewFormat());
//		Log.d(tag, "Picture frame rate " + mParams.getPreviewFrameRate());
	}
	
	public void release () {
		mCam.setPreviewCallback(null);
		mCam.stopPreview();
		mCam.release();
	}
	
	//TODO: This is documented as not optimized for efficiency for preview and
	// improved frame rate. Must move onto setPreviewCallbackWithBuffer
	private class PreviewHandler implements PreviewCallback {

		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			Log.d(tag, "PreviewFrame received");
		}
	}
	
	private class CameraErrors implements ErrorCallback {

		@Override
		public void onError(int error, Camera camera) {
			// TODO Auto-generated method stub
			Log.d(tag, "Camera onError " + error);
		}
		
	}
}
