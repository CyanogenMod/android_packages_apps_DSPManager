package com.bel.android.dspmanager;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Setting utility for CyanogenMod's DSP capabilities.
 * This page is displays the top-level configurations menu.
 * 
 * @author alankila
 */
public final class DSPManager extends PreferenceActivity {
	public static final String SHARED_PREFERENCES_BASENAME = "com.bel.android.dspmanager";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.main_preferences);
		/* mostly for development: start the service if it isn't started yet. */
		sendBroadcast(new Intent("com.bel.android.dspmanager.UPDATE"));
    }
}
