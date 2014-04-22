package com.app.camstreamer;

public interface BufferNotifications {
	void onBuffersCreated ();
	void onError (int error, int what);
}
