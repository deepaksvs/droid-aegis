package com.app.camstreamer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class BufferPool {
	ArrayList<ByteBuffer>		mBufferQueue;
	
	public	BufferPool (int size, int maxElements) {
		ByteBuffer		mBuffs;
		mBufferQueue 	= new ArrayList<ByteBuffer>(maxElements);
		for (int n = 0; n < maxElements; n++) {
			mBuffs = ByteBuffer.allocate(size);
			mBuffs.clear();
			mBufferQueue.add(n, mBuffs);
		}
	}

	public ByteBuffer dequeueBuffer () {
		ByteBuffer	mBuff = null;
		synchronized (mBufferQueue) {
			mBuff = mBufferQueue.remove(0);
		}
		return mBuff;
	}

	public void pushBack (ByteBuffer mBuff) {
		synchronized (mBufferQueue) {
			mBuff.clear();
			mBufferQueue.add(mBuff);
		}
	}
	
	public void release () {
		synchronized (mBufferQueue) {
			for(int n = 0; n < mBufferQueue.size(); n++) {
				ByteBuffer	mBuff = mBufferQueue.remove(0);
				mBuff.capacity();
				mBuff = null;
			}
			mBufferQueue.clear();
		}
		mBufferQueue = null;
	}
}
