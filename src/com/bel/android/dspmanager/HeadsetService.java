package com.bel.android.dspmanager;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;

/**
 * <p>
 * This calls listen to two kinds of events:
 * <ol>
 * <li>headset plug / unplug events. The android framework only delivers
 *     these events to a running process, so there must be a service listening
 *     to them.
 * <li>preference update events.
 * </ol>
 * <p>
 * When new events arrive, they are pushed into the audio stack
 * using AudioManager.setParameters().
 * 
 * @author alankila
 */
public class HeadsetService extends Service {
	private static final String TAG = HeadsetService.class.getSimpleName();

	private AudioManager audioManager;

	private boolean useHeadphone;
	
	/**
	 * Update audio parameters when headset is plugged/unplugged.
	 */
    private BroadcastReceiver headsetReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			useHeadphone = intent.getIntExtra("state", 0) != 0;
			Log.i(TAG, "Headset plugged: " + useHeadphone);
			updateDsp();
		}
	};

    private BroadcastReceiver preferenceUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "Preferences updated.");
			updateDsp();
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Starting service.");

		audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        registerReceiver(headsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        registerReceiver(preferenceUpdateReceiver, new IntentFilter("com.bel.android.dspmanager.UPDATE"));
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Stopping service.");

		unregisterReceiver(headsetReceiver);
		unregisterReceiver(preferenceUpdateReceiver);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * Push new configuration to audio stack.
	 */
	private void updateDsp() {
		String mode = useHeadphone ? "headset" : "speaker";
		SharedPreferences preferences = getSharedPreferences(DSPManager.SHARED_PREFERENCES_BASENAME + "." + mode, 0);
		
		/* Preferences that are boolean flags. */
		for (String s : new String[] {
				"dsp.compression.enable",
				"dsp.tone.enable",
				"dsp.headphone.enable"
		}) {
			audioManager.setParameters(s + "=" + (preferences.getBoolean(s, false) ? "1" : "0"));
		}

		/* Preferences that are floating point values. */
		for (String s : new String[] {
				"dsp.tone.eq1", "dsp.tone.eq2", "dsp.tone.eq3", "dsp.tone.eq4", "dsp.tone.eq5",
		}) {
			audioManager.setParameters(s + "=" + preferences.getFloat(s, 0));
		}

		/* Preferences that are strings which can be handled by AudioManager directly. */
		for (String s : new String[] {
				"dsp.compression.mode", "dsp.headphone.mode"
		}) {
			audioManager.setParameters(preferences.getString(s, ""));
		}
	}	
}
