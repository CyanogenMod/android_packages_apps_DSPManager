package com.bel.android.dspmanager;

import android.widget.SeekBar.OnSeekBarChangeListener;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class SliderPreference extends Preference {
	private SeekBar slider;
	private TextView label;
	float level;

	public SliderPreference(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
		setWidgetLayoutResource(R.layout.slider_preference);
	}
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		
		label = (TextView) view.findViewById(R.id.Label);

		slider = (SeekBar) view.findViewById(R.id.Slider);
		slider.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				level = (slider.getProgress() - slider.getMax() / 2) / 10f;
				label.setText(String.format("%+.1f dB", level));
				persistFloat(level);
				notifyChanged();
			}
		});		
		slider.setMax(120);
		slider.setProgress(Math.round(level * 10f) + slider.getMax() / 2);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		level = restorePersistedValue ? getPersistedFloat(level) : 0;
		notifyChanged();
	}
}
