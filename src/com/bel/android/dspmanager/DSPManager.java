package com.bel.android.dspmanager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

class EqualizerKnob {
	protected SeekBar knob;
	protected TextView label;
}

/**
 * Setting utility for CyanogenMod's DSP capabilities.
 * 
 * @author alankila
 */
public final class DSPManager extends Activity {
	private static final String PREFERENCES = DSPManager.class.getName();
	
	/** Headset plugged in? */
	boolean headsetPlugged;
	
	final class HeadphoneEnabler extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			headsetPlugged = intent.getIntExtra("state", 0) != 0;
			updateHeadphoneState();
		}
	}
	private HeadphoneEnabler headphoneEnabler;
	
	private AudioManager audioManager;
	
	private CheckBox compressionEnable;
	private SeekBar compressionKnob;
	private TextView compressionLabel;
	
	private CheckBox toneEnable;
	private EqualizerKnob[] toneKnob = {
		new EqualizerKnob(), new EqualizerKnob(), new EqualizerKnob(), new EqualizerKnob(), new EqualizerKnob()
	};

	private CheckBox headphoneEnable;

	private void updateHeadphoneState() {
		boolean enable = headphoneEnable.isChecked() && headsetPlugged;
		audioManager.setParameters("dsp.headphone.enable=" + (enable ? "1" : "0"));
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        headphoneEnabler = new HeadphoneEnabler();
        registerReceiver(headphoneEnabler, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        compressionEnable = (CheckBox) findViewById(R.id.CompressionEnable);
        compressionEnable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        		audioManager.setParameters("dsp.compression.enable=" + (isChecked ? "1" : "0"));
        		compressionKnob.setEnabled(isChecked);
        	}
        });
        compressionKnob = (SeekBar) findViewById(R.id.Ratio);
		compressionLabel = (TextView) findViewById(R.id.RatioLabel);

		compressionKnob.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {			
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				float value = 1f + progress / 10f;
				audioManager.setParameters("dsp.compression.ratio=" + value);
				compressionLabel.setText(String.format("%.1f:1", value));
			}
		});
		compressionKnob.setKeyProgressIncrement(1);
		compressionKnob.setMax(40);
		compressionKnob.setProgress(10);
		
        toneEnable = (CheckBox) findViewById(R.id.ToneEnable);
        toneEnable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        		audioManager.setParameters("dsp.tone.enable=" + (isChecked ? "1" : "0"));
        		for (int i = 0; i < toneKnob.length; i ++) {
        			toneKnob[i].knob.setEnabled(isChecked);
        		}
        	}
        });
        
        for (int _i = 0; _i < toneKnob.length; _i ++) {
        	final int i = _i;
        	
        	String fieldName = "Eq" + (i+1);
        	String labelName = fieldName + "Label";
        	
        	try {
        		toneKnob[i].knob = (SeekBar) findViewById((Integer) R.id.class.getField(fieldName).get(null));
        		toneKnob[i].label = (TextView) findViewById((Integer) R.id.class.getField(labelName).get(null));
        	}
        	catch (Exception e) {
        		throw new RuntimeException(e);
        	}
        	
        	toneKnob[i].knob.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				public void onStopTrackingTouch(SeekBar seekBar) {
				}
				
				public void onStartTrackingTouch(SeekBar seekBar) {
				}
				
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					float value = (progress - 60) / 10f;
					audioManager.setParameters("dsp.tone.eq" + (i+1) + "=" + value);
					toneKnob[i].label.setText(String.format("%+.1f dB", value));
				}
			});
        	
    		toneKnob[i].knob.setKeyProgressIncrement(1);
        	toneKnob[i].knob.setMax(120);
        	toneKnob[i].knob.setProgress(60);
        }
 
        headphoneEnable = (CheckBox) findViewById(R.id.HeadphoneEnable);
        headphoneEnable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        		updateHeadphoneState();
        	}
        });
        
        readPrefs();
    }

    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	unregisterReceiver(headphoneEnabler);
    	writePrefs();
    }
    
    private void readPrefs() {
        SharedPreferences preferences = getSharedPreferences(PREFERENCES, 0);

    	compressionEnable.setChecked(preferences.getBoolean("dsp.compression.enable", false));
    	compressionKnob.setProgress(preferences.getInt("dsp.compression.ratio", 10));
    
    	toneEnable.setChecked(preferences.getBoolean("dsp.tone.enable", false));
    	for (int i = 0; i < toneKnob.length; i ++) {
    		toneKnob[i].knob.setProgress(preferences.getInt("dsp.tone.eq" + (i+1), 60));
    	}
    	
    	headphoneEnable.setChecked(preferences.getBoolean("dsp.headphone.enable", false));
    }
    
    private void writePrefs() {
    	SharedPreferences preferences = getSharedPreferences(PREFERENCES, 0);
    	SharedPreferences.Editor preferencesEditor = preferences.edit();
    	
    	preferencesEditor.putBoolean("dsp.compression.enable", compressionEnable.isChecked());
    	preferencesEditor.putInt("dsp.compression.ratio", compressionKnob.getProgress());
    	
    	preferencesEditor.putBoolean("dsp.tone.enable", toneEnable.isChecked());
    	for (int i = 0; i < toneKnob.length; i ++) {
    		preferencesEditor.putInt("dsp.tone.eq" + (i+1), toneKnob[i].knob.getProgress());
    	}
    	
    	preferencesEditor.putBoolean("dsp.headphone.enable", headphoneEnable.isChecked());
    	
    	preferencesEditor.commit();
    }
}
