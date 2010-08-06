package com.bel.android.dspmanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Setting utility for CyanogenMod's DSP capabilities.
 * 
 * @author alankila
 */
public final class DSPScreen extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.dsp_preferences);		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.reset:
			clearPrefs();
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}
	
	@Override
	public SharedPreferences getSharedPreferences(String name, int mode) {
		DSPManager.Mode subPage = (DSPManager.Mode) getIntent().getSerializableExtra("mode");
		return super.getSharedPreferences(DSPManager.PREFERENCES_SETTINGS_NAME + "." + subPage.name(), mode);
	}
	
	private void clearPrefs() {
		SharedPreferences preferences = getSharedPreferences(null, 0);
		SharedPreferences.Editor preferencesEditor = preferences.edit();
		
		for (String preference : preferences.getAll().keySet()) {
			preferencesEditor.remove(preference);
		}
		
		preferencesEditor.commit();
		
		/* Now do something to make existing preferences to notice that things changed. */
		finish();
	}
}
