package com.bel.android.dspmanager.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.bel.android.dspmanager.R;

/**
 * Setting utility for CyanogenMod's DSP capabilities.
 * This page is displays the top-level configurations menu.
 * 
 * @author alankila
 */
public final class DSPManager extends PreferenceActivity {
	public static final String SHARED_PREFERENCES_BASENAME = "com.bel.android.dspmanager";

	public static final int NOTIFY_FOREGROUND_ID = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.main_preferences);
		/* mostly for development: start the service if it isn't started yet. */
		sendBroadcast(new Intent("com.bel.android.dspmanager.UPDATE"));
    }
}
