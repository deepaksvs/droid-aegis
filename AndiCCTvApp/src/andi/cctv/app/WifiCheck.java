package andi.cctv.app;

import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiCheck {
	private static final String tag = "WifiCheck";
	
	public boolean enableWifi (WifiManager connMgr) {
		boolean connected = false;
		boolean wifi = connMgr.isWifiEnabled();
		if (wifi) {
			Log.d(tag, "WiFi is enabled. Do nothing");
		}
		else {
			Log.d(tag, "Wifi is disabled. Enable Wifi");
			if (connMgr.setWifiEnabled(true)) {
				Log.d(tag, "Wifi Enabled.");
				connected = true;
			}
		}
		return connected;
	}


}
