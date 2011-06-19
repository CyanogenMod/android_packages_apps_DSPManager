package com.bel.android.dspmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This receiver starts our {@link HeadsetService} after system boot.
 * Since Android 2.3, we will always need a persistent process,
 * because we are forced to keep track of all open audio sessions.
 * 
 * @author alankila
 */
public class ServiceLaunchReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		context.startService(new Intent(HeadsetService.NAME));
	}
}
