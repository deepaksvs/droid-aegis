package com.app.camstreamer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class BufferHandler {
	private Handler					mDispatchHandler;
	private	Looper					mDispatchLooper;
	private BufferPool				mBuffPool;
	private ArrayList<ByteBuffer> 	mDispatchQueue;
	private int						mFrameSize;

	private static final int	BUFFER_WHATS = 0xdeadbeef;
	private static final int 	FRAMES_CACHE = (30 * 1); // 30 fps for 2 secs
	private static final String tag = "DispatchThread";

	public BufferHandler () {
		Log.d(tag, "New of Dispatch thread");
		new DispatchThread();
		mDispatchQueue = null;
	}

	public void createBuffers (int size) {
		mFrameSize = size;
		mBuffPool = new BufferPool(size, FRAMES_CACHE);
		mDispatchQueue = new ArrayList<ByteBuffer>(FRAMES_CACHE);
	}

	private class DispatchThread extends Thread {

		@SuppressLint("HandlerLeak")
		@Override
		public void run() {
			Log.d(tag, "Dispatch Thread Created");
			Looper.prepare();
			mDispatchLooper = Looper.myLooper();
			mDispatchHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					Log.d(tag, "PreviewFrame dispatch");
					// TODO Auto-generated method stub
					ByteBuffer mBuff = null;
					synchronized (mDispatchQueue) {
						mBuff = mDispatchQueue.remove(0);
					}
					if (mBuff != null) {
						// copy the data
						// push back the buffer
						Log.d(tag, "Frame to Socket length " + mBuff.position());
						mBuffPool.pushBack(mBuff);
					}
					super.handleMessage(msg);
				}
			};
			Looper.loop();
		}
	}

	public void clean () {
//		mDispatchQueue.clear();
		mDispatchHandler.removeMessages(BUFFER_WHATS);
		mDispatchLooper.quit();
		mBuffPool.release();
		synchronized (mDispatchQueue) {
			for(int n = 0; n < mDispatchQueue.size(); n++) {
				ByteBuffer	mBuff = mDispatchQueue.remove(0);
				mBuff.capacity();
				mBuff = null;
			}
		}
		mDispatchQueue = null;
		mBuffPool = null;
		mDispatchLooper = null;
		mDispatchHandler = null;
	}

	public void DispatchFrame (byte[] frameData) {
		synchronized (mDispatchQueue) {
			if (null == mDispatchQueue) {
				return;
			}
		}

		ByteBuffer		mBuff = mBuffPool.dequeueBuffer();
		if (null != mBuff) {
			// copy the data
			mBuff.put(frameData, 0, frameData.length);
			Message		msg = new Message();
			msg.what	= BUFFER_WHATS;
			synchronized (mDispatchQueue) {
				mDispatchQueue.add(mBuff);
			}
			mDispatchHandler.sendMessage(msg);
		}
	}
}
