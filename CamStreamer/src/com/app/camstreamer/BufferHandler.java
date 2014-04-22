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
	private BufferNotifications		mNtf;
	private DispatchThread			mThread;
	
	private static final int	BUFFER_WHATS = 0xdeadbeef;
	private static final int 	FRAMES_CACHE = (30 * 1); // 30 fps for 2 secs
	private static final String tag = "DispatchThread";

	public BufferHandler () {
		mDispatchHandler = null;
		mDispatchQueue = null;
		mNtf = null;

		mThread = new DispatchThread();
		mThread.start();
		synchronized (mThread) {
			try {
				mThread.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		Log.d(tag, "BufferHandler()");
	}

	public void registerBufferNotifier (BufferNotifications notifier) {
		mNtf = notifier;
	}

	public void createBuffers (int size) {
		Log.d(tag, "Create Buffers");
		mBuffPool = new BufferPool(size, FRAMES_CACHE);
		mDispatchQueue = new ArrayList<ByteBuffer>(FRAMES_CACHE);
		if (mNtf != null) {
			Log.d(tag, "Buffers created");
			mNtf.onBuffersCreated();
		}
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
//					Log.d(tag, "PreviewFrame dispatch");
					ByteBuffer mBuff = null;
					synchronized (mDispatchQueue) {
						mBuff = mDispatchQueue.remove(0);
					}
					if (mBuff != null) {
						// copy the data
						// push back the buffer
//						Log.d(tag, "Frame to Socket length " + mBuff.position());
						
						if (msg.arg2 != mBuff.position()) {
							Log.d(tag, "msg Length " + msg.arg2 + " buffer " + mBuff.position());
						}
						mBuffPool.pushBack(mBuff);
					}
					super.handleMessage(msg);
				}
			};
			Log.d(tag, "@" + mDispatchHandler);
			synchronized (mThread) {
				mThread.notifyAll();
			}
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
			msg.arg1 	= 
			msg.what	= BUFFER_WHATS;
			synchronized (mDispatchQueue) {
				msg.arg1 	= mDispatchQueue.size() + 1;
				msg.arg2	= frameData.length;
				mDispatchQueue.add(mBuff);
			}
			mDispatchHandler.sendMessage(msg);
		}
		else {
			Log.e(tag, "Dequeue failed");
		}
	}
}
