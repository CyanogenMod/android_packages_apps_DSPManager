package com.bel.android.dspmanager.preference;

import java.util.Arrays;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;

import com.bel.android.dspmanager.R;

public class EqualizerPreference extends Preference {
	private EqualizerSurface surface;
	float[] levels = new float[5];
	
	public EqualizerPreference(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		setLayoutResource(R.layout.equalizer);
	}
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);

		surface = (EqualizerSurface) view.findViewById(R.id.FrequencyResponse);
		surface.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				float x = event.getX();
				float y = event.getY();
				
				int wx = v.getWidth();
				int wy = v.getHeight();
				
				int band = (int) (x * 5 / wx);
				if (band < 0) {
					band = 0;
				}
				if (band > levels.length - 1) {
					band = levels.length - 1;
				}

				float level = (y / wy) * (EqualizerSurface.MIN_DB - EqualizerSurface.MAX_DB) - EqualizerSurface.MIN_DB;
				if (level < EqualizerSurface.MIN_DB) {
					level = EqualizerSurface.MIN_DB;
				}
				if (level > EqualizerSurface.MAX_DB) {
					level = EqualizerSurface.MAX_DB;
				}
				levels[band] = level;
				
				String levelString = "";
				for (float f : levels) {
					levelString += f + ";";
				}
				EqualizerPreference.this.persistString(levelString);
				
				surface.setBand(band, level);
				return true;
			}
		});
		/* we consume longclicks */
		surface.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				return true;
			}
		});

		for (int i = 0; i < levels.length; i ++) {
			surface.setBand(i, levels[i]);
		}
	}
	
	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		Arrays.fill(levels, 0);
		String levelString = restorePersistedValue ? getPersistedString(null) : (String) defaultValue;
		if (levelString != null) {
			String[] levelsStr = levelString.split(";");
			for (int i = 0; i < levels.length; i ++) {
				levels[i] = Float.valueOf(levelsStr[i]);
			}
		}
		
		notifyChanged();
	}
}
