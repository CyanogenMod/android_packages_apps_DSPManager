package com.bel.android.dspmanager;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.bel.android.dspmanager.activity.DSPManager;

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

	private boolean inCall;

	private boolean bluetoothAudio;

	/**
	 * Update audio parameters when preferences have been updated.
	 */
    private final BroadcastReceiver preferenceUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "Preferences updated.");
			updateDsp();
		}
	};

	/**
	 * Update audio parameters when headset is plugged/unplugged.
	 */
    private final BroadcastReceiver headsetReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			useHeadphone = intent.getIntExtra("state", 0) != 0;
			Log.i(TAG, "Headset plugged: " + useHeadphone);
			updateDsp();
		}
	};

	private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "Received bluetooth update");
			int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_DISCONNECTED);
			bluetoothAudio = state == AudioManager.SCO_AUDIO_STATE_CONNECTED;
			Log.i(TAG, "Bluetooth plugged: " + bluetoothAudio);
		}
	};

	private final PhoneStateListener mPhoneListener = new PhoneStateListener() {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			default:
				Log.i(TAG, "Disabling DSP during call.");
				inCall = true;
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				Log.i(TAG, "Enabling DSP after call has ended.");
				inCall = false;
				break;
			}

			updateDsp();
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Starting service.");

		startForeground(DSPManager.NOTIFY_FOREGROUND_ID, new Notification());
		
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		tm.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
		
		audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        registerReceiver(headsetReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        registerReceiver(preferenceUpdateReceiver, new IntentFilter("com.bel.android.dspmanager.UPDATE"));
        
        registerReceiver(bluetoothReceiver, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED));
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "Stopping service.");

		stopForeground(true);
		
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
		final String mode;
		
		if (inCall) {
			/* During calls, everything gets disabled; there is no configuration called 'disable' */
			mode = "disable";
		} else if (bluetoothAudio) {
			/* Bluetooth takes precedence over everything else */
			mode = "bluetooth";
		} else {
			/* Wired headset or internal speaker */
			mode = useHeadphone ? "headset" : "speaker";
		}
		SharedPreferences preferences = getSharedPreferences(DSPManager.SHARED_PREFERENCES_BASENAME + "." + mode, 0);
		
		/* Preferences that are boolean flags. */
		for (String s : new String[] {
				"dsp.compression.enable",
				"dsp.tone.enable",
				"dsp.headphone.enable"
		}) {
			audioManager.setParameters(s + "=" + (preferences.getBoolean(s, false) ? "1" : "0"));
		}

		/* Equalizer state is in a single string preference with all values separated by ; */
		String levels[] = preferences.getString("dsp.tone.eq", "0;0;0;0;0").split(";");
		for (int i = 0; i < 5; i ++) {
			audioManager.setParameters("dsp.tone.eq" + (i+1) + "=" + levels[i]);
		}

		/* Preferences that are strings which can be handled by AudioManager directly. */
		for (String s : new String[] {
				"dsp.compression.mode", "dsp.headphone.mode"
		}) {
			audioManager.setParameters(preferences.getString(s, ""));
		}
	}	
}
