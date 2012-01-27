
package com.bel.android.dspmanager.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bel.android.dspmanager.R;
import com.bel.android.dspmanager.activity.Utils;
import com.bel.android.dspmanager.preference.HeadsetAmplifierPreference.HeadsetAmplifierSeekBar;

public class HeadsetAmplifierPreference extends DialogPreference {

    private static final String TAG = "HEADSET...";

    private static final int[] SEEKBAR_ID = new int[] {
        R.id.headphone_amplifier_level_seekbar
    };

    private static final int[] VALUE_DISPLAY_ID = new int[] {
        R.id.headphone_amplifier_level_value
    };

    private static final String[] FILE_PATH = new String[] {
        "/sys/class/misc/wm8994_sound/headphone_amplifier_level"
    };

    private HeadsetAmplifierSeekBar mSeekBars[] = new HeadsetAmplifierSeekBar[1];

    private static final int MAX_VALUE = 62;

    private static final int OFFSET_VALUE = 57;

    private static int sInstances = 0;

    public HeadsetAmplifierPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.preference_dialog_headphone_amplifier_level);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        sInstances++;

        Log.d(TAG, "onBindDialogView");
        for (int i = 0; i < SEEKBAR_ID.length; i++) {
            SeekBar seekBar = (SeekBar) view.findViewById(SEEKBAR_ID[i]);
            TextView valueDisplay = (TextView) view.findViewById(VALUE_DISPLAY_ID[i]);
            mSeekBars[i] = new HeadsetAmplifierSeekBar(seekBar, valueDisplay, FILE_PATH[i]);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        sInstances--;

        if (positiveResult) {
            for (HeadsetAmplifierSeekBar csb : mSeekBars) {
                csb.save();
            }
        } else if (sInstances == 0) {
            for (HeadsetAmplifierSeekBar csb : mSeekBars) {
                csb.reset();
            }
        }
    }

    public static void restore(Context context) {
        if (!isSupported()) {
            return;
        }

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Log.d(TAG, "restore");
        for (String filePath : FILE_PATH) {
            String sDefaultValue = Utils.readOneLine(filePath);
            int iValue = sharedPrefs.getInt(filePath, Integer.valueOf(sDefaultValue));
            Utils.writeValue(filePath, String.valueOf((long) iValue));
        }
    }

    public static boolean isSupported() {
        boolean supported = true;
        for (String filePath : FILE_PATH) {
            if (!Utils.fileExists(filePath)) {
                supported = false;
            }
        }

        return supported;
    }

    class HeadsetAmplifierSeekBar implements SeekBar.OnSeekBarChangeListener {

        private String mFilePath;

        private int mOriginal;

        private SeekBar mSeekBar;

        private TextView mValueDisplay;

        public HeadsetAmplifierSeekBar(SeekBar seekBar, TextView valueDisplay, String filePath) {
            Log.d(TAG, "HeadsetAmplifierSeekBar");
            int iValue;

            mSeekBar = seekBar;
            mValueDisplay = valueDisplay;
            mFilePath = filePath;

            SharedPreferences sharedPreferences = getSharedPreferences();

            // Read original value
            if (Utils.fileExists(mFilePath)) {
                String sDefaultValue = Utils.readOneLine(mFilePath);
                iValue = Integer.valueOf(sDefaultValue);
            } else {
                iValue = MAX_VALUE;
            }
            mOriginal = iValue;

            mSeekBar.setMax(MAX_VALUE);
            reset();
            mSeekBar.setOnSeekBarChangeListener(this);
        }

        public void reset() {
            Log.d(TAG, "reset");
            int iValue;

            iValue = mOriginal - OFFSET_VALUE;
            mSeekBar.setProgress(mOriginal);
            updateValue(iValue);
        }

        public void save() {
            Log.d(TAG, "save");
            int iValue;

            iValue = mSeekBar.getProgress();
            Editor editor = getEditor();
            editor.putInt(mFilePath, iValue);
            editor.commit();
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int iValue;

            iValue = progress - OFFSET_VALUE;
            Utils.writeValue(mFilePath, String.valueOf((long) progress));
            updateValue(iValue);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // Do nothing
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // Do nothing
        }

        private void updateValue(int progress) {
            mValueDisplay.setText(String.format("%d", (int) progress) + " dB");
        }

    }
}
