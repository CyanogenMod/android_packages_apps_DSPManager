package com.bel.android.dspmanager;

import com.bel.android.dspmanager.activity.DSPManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * This receiver keeps our HeadsetService running. It listens to UPDATE events which correspond to
 * preferences changes, and BOOT_COMPLETE event, and then consults application's preferences
 * to evaluate whether {@link HeadsetService} needs starting.
 * 
 * @author alankila
 */
public class ServiceLaunchReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences preferencesHeadset = context.getSharedPreferences(DSPManager.SHARED_PREFERENCES_BASENAME + ".headset", 0);
		SharedPreferences preferencesSpeaker = context.getSharedPreferences(DSPManager.SHARED_PREFERENCES_BASENAME + ".speaker", 0);
		SharedPreferences preferencesBluetooth = context.getSharedPreferences(DSPManager.SHARED_PREFERENCES_BASENAME + ".bluetooth", 0);

		boolean serviceNeeded = false;
	    	
		/* check through the config if any of the master checkboxes are set. */
		for (SharedPreferences p : new SharedPreferences[] {
				preferencesHeadset, preferencesSpeaker, preferencesBluetooth
		}) {
			for (String s : new String[] {
					"dsp.compression.enable",
					"dsp.tone.enable",
					"dsp.headphone.enable",
			}) {
				if (p.getBoolean(s, false)) {
					serviceNeeded = true;
				}
			}
		}
		
		if (serviceNeeded) {
			context.startService(new Intent("com.bel.android.dspmanager.HEADSET_PLUG"));
		} else {
			context.stopService(new Intent("com.bel.android.dspmanager.HEADSET_PLUG"));
		}
	}
}
