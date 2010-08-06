package com.bel.android.dspmanager;

import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * Setting utility for CyanogenMod's DSP capabilities.
 * 
 * @author alankila
 */
public final class DSPManager extends Activity {
	public static final String PREFERENCES_SETTINGS_NAME = DSPManager.class.getName();

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
        setContentView(R.layout.main);

        startService(new Intent("com.bel.android.dspmanager.UPDATE"));
        
        ListView modeSelect = (ListView) findViewById(R.id.ModeSelect);
        final ArrayAdapter<Mode> items = new ArrayAdapter<Mode>(this, android.R.layout.simple_list_item_1);
        items.add(Mode.Headset);
        items.add(Mode.Speaker);
        modeSelect.setAdapter(items);
        	
        modeSelect.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int idx,
					long arg3) {
				Mode mode = items.getItem(idx);
				Intent dspScreen = new Intent("com.bel.android.dspmanager.EDIT");
				dspScreen.putExtra("mode", mode);
				startActivity(dspScreen);
			}
        });
    }        
}
