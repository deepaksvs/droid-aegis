package com.app.camstreamer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class StreamSock {
	Socket			mSock;
	
	public StreamSock () {
		mSock = new Socket();
	}
	
	public boolean connectSock (String host, int port) {
		boolean				connected = false;
		SocketAddress		mAddr = null;
		try {
			mAddr = new InetSocketAddress(InetAddress.getByName(host), port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (mAddr != null) {
			try {
				mSock.connect(mAddr);
				connected = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return connected;
	}
	
	
	public boolean sendData (BufferHandler mFrame) {
		OutputStream		mOut = null;
		try {
			mOut = mSock.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
