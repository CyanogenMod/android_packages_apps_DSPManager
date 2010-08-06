package com.bel.android.dspmanager;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.AudioManager;
import android.os.IBinder;

public class HeadsetService extends Service {
	private AudioManager audioManager;

	private boolean useHeadphone;

	private OnSharedPreferenceChangeListener preferenceChangeListener = new OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			
			updateDsp();
		}		
	};
	
	@Override
	public void onCreate() {
		super.onCreate();
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
					useHeadphone = intent.getIntExtra("state", 0) != 0;
				}
				
				updateDsp();		
			}
		}, new IntentFilter(Intent.ACTION_HEADSET_PLUG));

		SharedPreferences preferencesHeadset = getSharedPreferences(DSPManager.PREFERENCES_SETTINGS_NAME + "." + DSPManager.Mode.Headset.name(), 0);
		preferencesHeadset.registerOnSharedPreferenceChangeListener(preferenceChangeListener);

		SharedPreferences preferencesSpeaker = getSharedPreferences(DSPManager.PREFERENCES_SETTINGS_NAME + "." + DSPManager.Mode.Speaker.name(), 0);
		preferencesSpeaker.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private void updateDsp() {
		DSPManager.Mode mode = (useHeadphone ? DSPManager.Mode.Headset : DSPManager.Mode.Speaker);
		SharedPreferences preferences = getSharedPreferences(DSPManager.PREFERENCES_SETTINGS_NAME + "." + mode.name(), 0);
		
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
