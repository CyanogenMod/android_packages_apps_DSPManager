package com.bel.android.dspmanager.preference;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.bel.android.dspmanager.R;

public class EqualizerPreference extends DialogPreference {
	protected EqualizerSurface listEqualizer, dialogEqualizer;
	private final float[] levels = new float[5];
	
	public EqualizerPreference(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		setLayoutResource(R.layout.equalizer);
		setDialogLayoutResource(R.layout.equalizer_popup);
	}
	
	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);

		dialogEqualizer = (EqualizerSurface) view.findViewById(R.id.FrequencyResponse);
		dialogEqualizer.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				float x = event.getX();
				float y = event.getY();
				
				/* Which band is closest to the position user pressed? */
				int band = dialogEqualizer.findClosest(x);

				int wy = v.getHeight();
				float level = (y / wy) * (EqualizerSurface.MIN_DB - EqualizerSurface.MAX_DB) - EqualizerSurface.MIN_DB;
				if (level < EqualizerSurface.MIN_DB) {
					level = EqualizerSurface.MIN_DB;
				}
				if (level > EqualizerSurface.MAX_DB) {
					level = EqualizerSurface.MAX_DB;
				}
				
				dialogEqualizer.setBand(band, level);
				refreshPreferenceFromEqualizer(dialogEqualizer);
				return true;
			}
		});

		for (int i = 0; i < 5; i ++) {
			dialogEqualizer.setBand(i, levels[i]);
		}
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			refreshPreferenceFromEqualizer(dialogEqualizer);
			for (int i = 0; i < 5; i ++) {
				float value = dialogEqualizer.getBand(i);
				listEqualizer.setBand(i, value);
				levels[i] = value;
			}
			notifyChanged();
		} else {
			refreshPreferenceFromEqualizer(listEqualizer);
		}
	}
	
	protected void refreshPreferenceFromEqualizer(EqualizerSurface equalizer) {
		String levelString = "";
		for (int i = 0; i < 5; i ++) {
			float value = equalizer.getBand(i);
			levelString += value + ";";
		}
		EqualizerPreference.this.persistString(levelString);
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		listEqualizer = (EqualizerSurface) view.findViewById(R.id.FrequencyResponse);
		for (int i = 0; i < 5 ; i ++) {
			listEqualizer.setBand(i, levels[i]);
		}
	}
	
	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		String levelString = restorePersistedValue ? getPersistedString(null) : (String) defaultValue;
		if (levelString != null) {
			String[] levelsStr = levelString.split(";");
			for (int i = 0; i < 5; i ++) {
				levels[i] = Float.valueOf(levelsStr[i]);
			}
		}
	}
}
