package com.bel.android.dspmanager.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.bel.android.dspmanager.HeadsetService;
import com.bel.android.dspmanager.R;

/**
 * This class implements a general PreferencesActivity that we can use to
 * adjust DSP settings. It adds a menu to clear the preferences on this page,
 * and a listener that ensures that our {@link HeadsetService} is running if
 * required.
 * 
 * @author alankila
 */
public final class DSPScreen extends PreferenceActivity {	
	private final OnSharedPreferenceChangeListener serviceLauncher = new OnSharedPreferenceChangeListener() {
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			sendBroadcast(new Intent("com.bel.android.dspmanager.UPDATE"));
		}
	};

	/** Return the last component of the activity. */
	private String getSubPage() {
		String[] action = getIntent().getAction().split("\\.");
		return action[action.length - 1].toLowerCase();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			addPreferencesFromResource(this.getResources().getIdentifier(getSubPage() + "_preferences","xml",this.getPackageName()));
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		/* Register a listener that publishes UPDATE requests to the service starter. */
		SharedPreferences preferences = getSharedPreferences(null, 0);
		preferences.registerOnSharedPreferenceChangeListener(serviceLauncher);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		SharedPreferences preferences = getSharedPreferences(null, 0);
		preferences.unregisterOnSharedPreferenceChangeListener(serviceLauncher);
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
	
	/**
	 * We select the specific SharedPreferences repository based on the details of the
	 * Intent used to reach this action.
	 */
	@Override
	public SharedPreferences getSharedPreferences(String name, int mode) {
		return super.getSharedPreferences(DSPManager.SHARED_PREFERENCES_BASENAME + "." + getSubPage(), mode);
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
