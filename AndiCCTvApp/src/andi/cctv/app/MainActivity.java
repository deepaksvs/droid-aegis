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
	private ToFile		mFile;
	
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
		mFile.closeFos();
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
		mFile = new ToFile();
		Log.d(Tag, "onCreate of MainActivity");
		 mSurfaceView = new MockSurfaceView(this);
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
			mRecController = new MediaController(mSurfaceView.getHolder(),
									mFile.getOutFD());
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
		
	}

}
