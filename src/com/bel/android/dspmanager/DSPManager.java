package com.bel.android.dspmanager;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

/**
 * Setting utility for CyanogenMod's DSP capabilities.
 * 
 * @author alankila
 */
public final class DSPManager extends PreferenceActivity {
	public static final String SHARED_PREFERENCES_BASENAME = "com.bel.android.dspmanager";

	public enum Mode {
		Headset(R.xml.headset_preferences),
		Speaker(R.xml.speaker_preferences);
		
		private int preferencesId;
		
		private Mode(int preferencesId) {
			this.preferencesId = preferencesId;
		}
		
		public int getPreferencesId() {
			return preferencesId;
		}
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.main_preferences);
        startService(new Intent("com.bel.android.dspmanager.UPDATE"));
    }
    
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
 		Intent dspScreen = new Intent("com.bel.android.dspmanager.EDIT");
		dspScreen.putExtra("mode", Mode.valueOf(preference.getKey()));
		startActivity(dspScreen);
		return true;
    }
}
