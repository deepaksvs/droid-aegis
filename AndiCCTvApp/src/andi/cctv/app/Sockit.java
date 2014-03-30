package andi.cctv.app;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import android.os.ParcelFileDescriptor;

public class Sockit {
	private Socket	mSoc;
	
	public Sockit(String host, int port) {
		// TODO Auto-generated constructor stub
		mSoc = null;
		try {
			InetAddress		addr = null;
			addr = InetAddress.getByName(host);
			mSoc = new Socket(addr, port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			mSoc = null;
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			mSoc = null;
			e.printStackTrace();
		}
	}
	
	public Sockit () {
		mSoc = new Socket();
	}
	
	public boolean connectTo (String addr, int port) {
		boolean result = false;
		SocketAddress remoteAddr = null;
		try {
			remoteAddr = new InetSocketAddress(InetAddress.getByName(addr), port);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			mSoc.connect(remoteAddr);
			result = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}
	public FileDescriptor getFDfromSockit () {
		if (null != mSoc) {
			ParcelFileDescriptor pFD = ParcelFileDescriptor.fromSocket(mSoc);
			return (pFD.getFileDescriptor());
		}
		return null;
	}
	
	public void closeSoc () {
		if (null != mSoc) {
			try {
				mSoc.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mSoc = null;
		}
	}
}
