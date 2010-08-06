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
	/* FIXME: find out how this is determined. */
	public static final String PREFERENCES_SETTINGS_NAME = "com.bel.android.dspmanager";

	public enum Mode {
		Headset("Headset Mode"),
		Speaker("Speaker Mode");
		
		private String value;
		
		private Mode(String string) {
			this.value = string;
		}
		
		@Override
		public String toString() {
			return value;
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
