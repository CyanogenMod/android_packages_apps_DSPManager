package com.bel.android.dspmanager;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.IBinder;

public class HeadsetService extends Service {
	private AudioManager audioManager;

	private boolean useHeadphone;

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

        registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				updateDsp();		
			}
		}, new IntentFilter("com.bel.android.dspmanager.UPDATE"));
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	private void updateDsp() {
		String prefix = (useHeadphone ? DSPManager.Mode.Headset : DSPManager.Mode.Speaker).name();
		SharedPreferences preferences = getSharedPreferences(DSPManager.PREFERENCES_SETTINGS_NAME + "." + prefix, 0);
		
		for (String s : new String[] {
				"dsp.compression.enable",
				"dsp.reverb.enable", "dsp.reverb.deep", "dsp.reverb.wide",
				"dsp.tone.enable",
				"dsp.headphone.enable"
		}) {
			audioManager.setParameters(s + "=" + (preferences.getBoolean(s, false) ? "1" : "0"));
		}
		
		for (String s : new String[] {
				"dsp.compression.ratio",
				"dsp.reverb.level",
				"dsp.tone.eq1", "dsp.tone.eq2", "dsp.tone.eq3", "dsp.tone.eq4", "dsp.tone.eq5"
		}) {
			audioManager.setParameters(s + "=" + preferences.getFloat(s, 0f));
		}
	}
}
