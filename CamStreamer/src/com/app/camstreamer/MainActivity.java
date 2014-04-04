package com.app.camstreamer;

import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

public class MainActivity extends Activity {
	private static final String tag = "CameraMain";
	private SurfaceView		mSurfaceView;
	private MockCamera		mCamera;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.container, new PlaceholderFragment())
//                    .commit();
//        }
        
        Log.d(tag, "onCreate of Camera MainActivity");
        mCamera		= new MockCamera();
        mSurfaceView = new MockSurfaceView (this);
        setContentView(mSurfaceView);
    }

    @Override
	protected void onPause() {
		// TODO Auto-generated method stub
		Log.d(tag,"OnPause for MainActivity");
		mCamera.release();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// TODO Auto-generated method stub
		Log.d(tag,"OnDestroy for MainActivity");
		mCamera = null;
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    private class MockSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    	private SurfaceHolder	mHolder;
    	private static final int WIDTH_CONST = 320;
    	private static final int HEIGHT_CONST = 240;

		@SuppressWarnings("deprecation")
		public MockSurfaceView(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
			mHolder = getHolder();
			mHolder.addCallback(this);
			mHolder.setFixedSize(WIDTH_CONST, HEIGHT_CONST);
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// TODO Auto-generated method stub
			Log.d(tag, "on SurfaceChanged");
			Log.d(tag, "width = " + width + " height = " + height);
			mCamera.setMockPreviewSize(width, height);
			Thread mt = new Thread() {
				public void run () {
					mCamera.initCamera();
					mCamera.doPreview(mSurfaceView.getHolder());
				}
			};
			mt.start();
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			Log.d(tag, "on SurfaceCreated");
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			Log.d(tag, "on SurfaceDestroyed");
		}
    	
    }
}
