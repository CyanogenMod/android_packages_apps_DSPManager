package com.bel.android.dspmanager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

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
public final class DSPScreen extends Activity {
	private DSPManager.Mode mode;
	
	private CheckBox compressionEnable;
	private Knob compressionKnob = new Knob();

	private CheckBox reverbEnable;
	private CheckBox reverbDeep;
	private CheckBox reverbWide;
	private Knob reverbKnob = new Knob();

	private CheckBox toneEnable;
	private Knob[] toneKnob = {
			new Knob(), new Knob(), new Knob(), new Knob(),	new Knob()
	};

	private CheckBox headphoneEnable;

	private boolean disablePreferencesWriting;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_effects);

		disablePreferencesWriting = true;
		bindCompression();
		bindReverb();
		bindTone();
		bindHeadphone();
		disablePreferencesWriting = false;
		
		readPrefs(getIntent());
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		readPrefs(intent);
	}

	private void bindCompression() {
		compressionEnable = (CheckBox) findViewById(R.id.CompressionEnable);
		compressionEnable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				compressionKnob.knob.setEnabled(isChecked);
				writePrefs();
			}
		});
		compressionKnob.knob = (SeekBar) findViewById(R.id.CompressionRatio);
		compressionKnob.label = (TextView) findViewById(R.id.CompressionRatioLabel);

		compressionKnob.knob.setOnSeekBarChangeListener(new KnobTrack() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				float value = 1f + progress / 10f;
				compressionKnob.label.setText(String.format("%.1f:1", value));
				writePrefs();
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
				reverbDeep.setEnabled(isChecked);
				reverbWide.setEnabled(isChecked);
				reverbKnob.knob.setEnabled(isChecked);
				writePrefs();
			}
		});

		reverbDeep = (CheckBox) findViewById(R.id.ReverbDeep);
		reverbDeep.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				writePrefs();
			}
		});

		reverbWide = (CheckBox) findViewById(R.id.ReverbWide);
		reverbWide.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				writePrefs();
			}
		});

		reverbKnob.knob = (SeekBar) findViewById(R.id.ReverbLevel);
		reverbKnob.label = (TextView) findViewById(R.id.ReverbLevelLabel);
		reverbKnob.knob.setOnSeekBarChangeListener(new KnobTrack() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				float value = (progress - reverbKnob.knob.getMax() / 2) / 10f;
				reverbKnob.label.setText(String.format("%+.1f dB", value));
				writePrefs();
			}
		});
		reverbKnob.knob.setKeyProgressIncrement(1);
		reverbKnob.knob.setMax(200);
		reverbKnob.knob.setProgress(reverbKnob.knob.getMax() / 2);
	}

	private void bindTone() {
		toneEnable = (CheckBox) findViewById(R.id.ToneEnable);
		toneEnable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				for (int i = 0; i < toneKnob.length; i ++) {
					toneKnob[i].knob.setEnabled(isChecked);
				}
				writePrefs();
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
					float value = (progress - toneKnob[i].knob.getMax() / 2) / 10f;
					toneKnob[i].label.setText(String.format("%+.1f dB", value));
					writePrefs();
				}
			});
			toneKnob[i].knob.setKeyProgressIncrement(1);
			toneKnob[i].knob.setMax(120);
			toneKnob[i].knob.setProgress(toneKnob[i].knob.getMax() / 2);
		}
	}
	
	private void bindHeadphone() {
		headphoneEnable = (CheckBox) findViewById(R.id.HeadphoneEnable);
		headphoneEnable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				writePrefs();
			}
		});
	}
	
	private void readPrefs(Intent intent) {
		disablePreferencesWriting = true;
		
		mode = (DSPManager.Mode) intent.getSerializableExtra("mode");
		SharedPreferences preferences = getSharedPreferences(DSPManager.PREFERENCES_SETTINGS_NAME + "." + mode.name(), 0);

		compressionEnable.setChecked(preferences.getBoolean("dsp.compression.enable", false));
		compressionKnob.knob.setProgress(Math.round(preferences.getFloat("dsp.compression.ratio", 0) * 10 - 10));
		reverbEnable.setChecked(preferences.getBoolean("dsp.reverb.enable", false));
		reverbDeep.setChecked(preferences.getBoolean("dsp.reverb.deep", true));
		reverbWide.setChecked(preferences.getBoolean("dsp.reverb.wide", true));
		reverbKnob.knob.setProgress(Math.round(preferences.getFloat("dsp.reverb.level", 0) * 10 + reverbKnob.knob.getMax() / 2));

		toneEnable.setChecked(preferences.getBoolean("dsp.tone.enable", false));
		for (int i = 0; i < toneKnob.length; i++) {
			toneKnob[i].knob.setProgress(Math.round(preferences.getFloat("dsp.tone.eq" + (i + 1), 0) * 10 + toneKnob[i].knob.getMax() / 2));
		}

		headphoneEnable.setChecked(preferences.getBoolean("dsp.headphone.enable", false));
		
		disablePreferencesWriting = false;
	}

	private void writePrefs() {
		if (disablePreferencesWriting) {
			return;
		}
		
		mode = (DSPManager.Mode) getIntent().getSerializableExtra("mode");
		SharedPreferences preferences = getSharedPreferences(DSPManager.PREFERENCES_SETTINGS_NAME + "." + mode.name(), 0);
		SharedPreferences.Editor preferencesEditor = preferences.edit();

		preferencesEditor.putBoolean("dsp.compression.enable", compressionEnable.isChecked());
		preferencesEditor.putFloat("dsp.compression.ratio", 1f + compressionKnob.knob.getProgress() / 10f);

		preferencesEditor.putBoolean("dsp.reverb.enable", reverbEnable.isChecked());
		preferencesEditor.putBoolean("dsp.reverb.deep", reverbDeep.isChecked());
		preferencesEditor.putBoolean("dsp.reverb.wide", reverbWide.isChecked());
		preferencesEditor.putFloat("dsp.reverb.level", (reverbKnob.knob.getProgress() - reverbKnob.knob.getMax() / 2) / 10f);

		preferencesEditor.putBoolean("dsp.tone.enable", toneEnable.isChecked());
		for (int i = 0; i < toneKnob.length; i++) {
			preferencesEditor.putFloat("dsp.tone.eq" + (i+1), (toneKnob[i].knob.getProgress() - toneKnob[i].knob.getMax() / 2) / 10f);
		}

		preferencesEditor.putBoolean("dsp.headphone.enable", headphoneEnable.isChecked());

		preferencesEditor.commit();
		
		sendBroadcast(new Intent("com.bel.android.dspmanager.UPDATE"));
	}
}
