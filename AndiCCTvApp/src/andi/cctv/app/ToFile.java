package andi.cctv.app;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.Environment;
import android.util.Log;

public class ToFile {
	private File	mOutFile;
	private FileOutputStream mFos;
	private static final String		mTag = "cctv";

	public ToFile() {
		String extPath = Environment.getExternalStorageDirectory().getPath();
		String absPath = extPath + "/rec.bin";
		Log.d(mTag, "File is " + absPath);
		mOutFile = new File(absPath);
		if (mOutFile.exists()) {
			mOutFile.delete();
		}
		try {
			mOutFile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d(mTag, "File exits " + mOutFile.exists());
		Log.d(mTag, "File can write " + mOutFile.canWrite());
		mFos = null;
	}

	public FileDescriptor getOutFD () {
		try {
			mFos = new FileOutputStream(mOutFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			return mFos.getFD();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void closeFos () {
		try {
			mFos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
