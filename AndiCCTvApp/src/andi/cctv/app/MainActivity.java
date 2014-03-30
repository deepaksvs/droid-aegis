package andi.cctv.app;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends Activity {
//	private WifiManager		connMan;
	private MediaController		mRecController;
	private static final String Tag = "cctv";
	private SurfaceView mSurfaceView;
//	private ToFile				mFile;
	private Sockit				mSoc;
	private int					mWidth, mHeight;
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.d(Tag,"OnPause for MainActivity");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d(Tag,"onDestroy for MainActivity");
		if (mRecController != null) {
			mRecController.stopMedia();
		}
		if (mSoc != null) {
			mSoc.closeSoc();
	}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		/*
		connMan = (WifiManager) getSystemService (Context.WIFI_SERVICE);
		WifiCheck	wc = new WifiCheck();
		wc.enableWifi(connMan);
		*/
		/*
		mFile = new ToFile();
		Log.d(Tag, "onCreate of MainActivity");
		 mSurfaceView = new MockSurfaceView(this);
		 setContentView(mSurfaceView);
		*/
		Log.d(Tag, "onCreate of MainActivity .. connect ");
		Thread th = new Thread() {
			public void run ( ) {
				Log.d(Tag, "Create and connect to server");
				mSoc = new Sockit();
				mSoc.connectTo("192.168.2.5", 6969);
			}
		};
		th.start();
		mSurfaceView = new MockSurfaceView(this);
		mSurfaceView.getHolder().setFixedSize(320, 240);
		setContentView(mSurfaceView);
		
//		Log.d(Tag, "Calling media controller init");
//		mRecController = new MediaController(mSurfaceView.getHolder());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private class MockSurfaceView extends SurfaceView implements SurfaceHolder.Callback{
		private SurfaceHolder	mHolder;
		public MockSurfaceView(Context context) {
			super(context);
			mHolder = getHolder();
			mHolder.addCallback(this);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			Log.d(Tag, "Surface is created. Calling MediaController");
			/*
			mRecController = new MediaController(mSurfaceView.getHolder(),
									mFile.getOutFD());
									*/
//			mRecController = new MediaController(mSurfaceView.getHolder(),
//					mSoc.getFDfromSockit());
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// TODO Auto-generated method stub
			Log.d(Tag, "on SurfaceChnaged is called");
			Log.d(Tag,"Format " + format);
			Log.d(Tag,"width " + width + "height " + height);
			mWidth = width;
			mHeight = height;
//			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			Thread th = new Thread () {
				public void run () {
					mRecController = new MediaController(mSurfaceView.getHolder());
					mRecController.setFileDescriptor(mSoc.getFDfromSockit());
					mRecController.setPreviewSize(mWidth, mHeight);
					mRecController.startRec();
				}
			};
			th.start();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			
		}
		
	}

}
