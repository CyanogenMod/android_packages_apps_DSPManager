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

class Knob {
	protected SeekBar knob;
	protected TextView label;	
}

abstract class KnobTrack implements OnSeekBarChangeListener {
	public void onStopTrackingTouch(SeekBar seekBar) {
	}
	public void onStartTrackingTouch(SeekBar seekBar) {
	}
}

/**
 * Setting utility for CyanogenMod's DSP capabilities.
 * 
 * @author alankila
 */
public final class DSPManager extends Activity {
	private static final String PREFERENCES_SETTINGS_NAME = DSPManager.class.getName();
	
	private AudioManager audioManager;

	private CheckBox compressionEnable;
	private Knob compressionKnob = new Knob();

	private CheckBox reverbEnable;
	private CheckBox reverbDeep;
	private CheckBox reverbWide;
	private Knob reverbKnob = new Knob();
	
	private CheckBox toneEnable;
	private Knob[] toneKnob = {
		new Knob(), new Knob(), new Knob(), new Knob(), new Knob()
	};

	private CheckBox headphoneEnable;
	boolean headsetPlugged;
	
	private BroadcastReceiver headphoneEnabler = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			headsetPlugged = intent.getIntExtra("state", 0) != 0;
			updateHeadphoneState();
		}
	};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        registerReceiver(headphoneEnabler, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        bindCompression();
        bindReverb();
        bindTone();
        bindHeadphone();

        readPrefs();
    }
        
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	unregisterReceiver(headphoneEnabler);
    	writePrefs();
    }
    
    private void bindCompression() {
        compressionEnable = (CheckBox) findViewById(R.id.CompressionEnable);
        compressionEnable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        		audioManager.setParameters("dsp.compression.enable=" + (isChecked ? "1" : "0"));
        		compressionKnob.knob.setEnabled(isChecked);
        	}
        });
        compressionKnob.knob = (SeekBar) findViewById(R.id.CompressionRatio);
		compressionKnob.label = (TextView) findViewById(R.id.CompressionRatioLabel);

		compressionKnob.knob.setOnSeekBarChangeListener(new KnobTrack() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				float value = 1f + progress / 10f;
				audioManager.setParameters("dsp.compression.ratio=" + value);
				compressionKnob.label.setText(String.format("%.1f:1", value));
			}
		});
		compressionKnob.knob.setKeyProgressIncrement(1);
		compressionKnob.knob.setMax(40);
		compressionKnob.knob.setProgress(10);
    }
    
    private void bindReverb() {
    	reverbEnable = (CheckBox) findViewById(R.id.ReverbEnable);
        reverbEnable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        		audioManager.setParameters("dsp.reverb.enable=" + (isChecked ? "1" : "0"));
        		reverbDeep.setEnabled(isChecked);
        		reverbWide.setEnabled(isChecked);
        		reverbKnob.knob.setEnabled(isChecked);
        	}
        });
    	
        reverbDeep = (CheckBox) findViewById(R.id.ReverbDeep);
        reverbDeep.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        		audioManager.setParameters("dsp.reverb.deep=" + (isChecked ? "1" : "0"));
        	}
        });
    	
        reverbWide = (CheckBox) findViewById(R.id.ReverbWide);
        reverbWide.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        		audioManager.setParameters("dsp.reverb.wide=" + (isChecked ? "1" : "0"));
        	}
        });

        reverbKnob.knob = (SeekBar) findViewById(R.id.ReverbLevel);
    	reverbKnob.label = (TextView) findViewById(R.id.ReverbLevelLabel);
       	reverbKnob.knob.setOnSeekBarChangeListener(new KnobTrack() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				float value = (progress - 100) / 10f;
				audioManager.setParameters("dsp.reverb.level=" + value);
				reverbKnob.label.setText(String.format("%+.1f dB", value));
			}
		});
		reverbKnob.knob.setKeyProgressIncrement(1);
    	reverbKnob.knob.setMax(200);
    	reverbKnob.knob.setProgress(100);
    }

    private void bindTone() {
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
        	
        	String fieldName = "ToneEq" + (i+1);
        	String labelName = fieldName + "Label";
        	
        	try {
        		toneKnob[i].knob = (SeekBar) findViewById((Integer) R.id.class.getField(fieldName).get(null));
        		toneKnob[i].label = (TextView) findViewById((Integer) R.id.class.getField(labelName).get(null));
        	}
        	catch (Exception e) {
        		throw new RuntimeException(e);
        	}
        	
        	toneKnob[i].knob.setOnSeekBarChangeListener(new KnobTrack() {
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
    }

    private void bindHeadphone() {
        headphoneEnable = (CheckBox) findViewById(R.id.HeadphoneEnable);
        headphoneEnable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        		updateHeadphoneState();
        	}
        });
    }

	private void updateHeadphoneState() {
		boolean enable = headphoneEnable.isChecked() && headsetPlugged;
		audioManager.setParameters("dsp.headphone.enable=" + (enable ? "1" : "0"));
	}
	
    private void readPrefs() {
        SharedPreferences preferences = getSharedPreferences(PREFERENCES_SETTINGS_NAME, 0);

    	compressionEnable.setChecked(preferences.getBoolean("dsp.compression.enable", false));
    	compressionKnob.knob.setProgress(preferences.getInt("dsp.compression.ratio", 10));
    
    	reverbEnable.setChecked(preferences.getBoolean("dsp.reverb.enable", false));
    	reverbDeep.setChecked(preferences.getBoolean("dsp.reverb.deep", true));
    	reverbWide.setChecked(preferences.getBoolean("dsp.reverb.wide", true));
    	reverbKnob.knob.setProgress(preferences.getInt("dsp.reverb.level", 100));
    	
    	toneEnable.setChecked(preferences.getBoolean("dsp.tone.enable", false));
    	for (int i = 0; i < toneKnob.length; i ++) {
    		toneKnob[i].knob.setProgress(preferences.getInt("dsp.tone.eq" + (i+1), 60));
    	}
    	
    	headphoneEnable.setChecked(preferences.getBoolean("dsp.headphone.enable", false));
    }
    
    private void writePrefs() {
    	SharedPreferences preferences = getSharedPreferences(PREFERENCES_SETTINGS_NAME, 0);
    	SharedPreferences.Editor preferencesEditor = preferences.edit();
    	
    	preferencesEditor.putBoolean("dsp.compression.enable", compressionEnable.isChecked());
    	preferencesEditor.putInt("dsp.compression.ratio", compressionKnob.knob.getProgress());

    	preferencesEditor.putBoolean("dsp.reverb.enable", reverbEnable.isChecked());
    	preferencesEditor.putBoolean("dsp.reverb.deep", reverbDeep.isChecked());
    	preferencesEditor.putBoolean("dsp.reverb.wide", reverbWide.isChecked());
    	preferencesEditor.putInt("dsp.reverb.level", reverbKnob.knob.getProgress());
    	
    	preferencesEditor.putBoolean("dsp.tone.enable", toneEnable.isChecked());
    	for (int i = 0; i < toneKnob.length; i ++) {
    		preferencesEditor.putInt("dsp.tone.eq" + (i+1), toneKnob[i].knob.getProgress());
    	}
    	
    	preferencesEditor.putBoolean("dsp.headphone.enable", headphoneEnable.isChecked());
    	
    	preferencesEditor.commit();
    }
}
