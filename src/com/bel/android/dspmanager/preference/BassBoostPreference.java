
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
import com.bel.android.dspmanager.activity.WM8994;
import com.bel.android.dspmanager.preference.HeadsetAmplifierPreference.HeadsetAmplifierSeekBar;

public class BassBoostPreference extends DialogPreference {

    private static final String TAG = "BassBoostPreference";

    private static final int[] SEEKBAR_ID = new int[] {
        R.id.bass_boost_gain_seekbar
        ,R.id.bass_boost_range_seekbar
    };
    private static final int[] VALUE_DISPLAY_ID = new int[] {
        R.id.bass_boost_gain_value
        ,R.id.bass_boost_range_value
    };
    private static final int[] SEEKBAR_MAX_VALUE = new int[] {
        WM8994.MAX_VALUE_GAIN
        ,WM8994.MAX_VALUE_RANGE
    };
    private static final String[] SEEKBAR_UOM = new String[] {
        "dB"
        ,"%"
    };

    private BassBoostSeekBar mSeekBars[] = new BassBoostSeekBar[2];

    private static int sInstances = 0;

    //WM8994 mWM8994 = new WM8994();

    public BassBoostPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.preference_dialog_bass_boost);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        sInstances++;

        Log.d(TAG, "onBindDialogView");
        for (int i = 0; i < SEEKBAR_ID.length; i++) {
            SeekBar seekBar = (SeekBar) view.findViewById(SEEKBAR_ID[i]);
            TextView valueDisplay = (TextView) view.findViewById(VALUE_DISPLAY_ID[i]);
            mSeekBars[i] = new BassBoostSeekBar(seekBar, valueDisplay, WM8994.BASS_BOOST_PREFS[i], SEEKBAR_MAX_VALUE[i], SEEKBAR_UOM[i]);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        sInstances--;

        if (positiveResult) {
            for (BassBoostSeekBar csb : mSeekBars) {
                csb.save();
            }
        } else if (sInstances == 0) {
            for (BassBoostSeekBar csb : mSeekBars) {
                csb.reset();
            }
        }
    }

    public static boolean isSupported(String FILE) {
        return Utils.fileExists(FILE);
    }

    public static void restore(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Log.d(TAG, "restore");
        writeBassBoost(context);
    }

    public static void writeBassBoost(Context context) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        //SharedPreferences sharedPrefs = getPreferenceManager().getSharedPreferences();
        int mGain = sharedPrefs.getInt(WM8994.BASS_BOOST_PREF_GAIN, WM8994.MAX_VALUE_GAIN);
        int mRange = sharedPrefs.getInt(WM8994.BASS_BOOST_PREF_RANGE, WM8994.MAX_VALUE_RANGE);
        String mPreset = sharedPrefs.getString(WM8994.BASS_BOOST_PRESET_PREF, "0");

        double mDigitalGain = (double) mRange;
        int mGain1 = 1;
        int mGain2 = 1;
        if (mPreset.equals("0")) {
            mGain2 = 0;
        } else {
            mGain1 = -1;
        }

        mDigitalGain = (mDigitalGain / 5) * (mGain * 1000);
        Log.d(TAG, "mDigitalGain == " + mDigitalGain);
        Utils.writeValue(WM8994.BASS_BOOST_FILES[0], String.valueOf((long) mDigitalGain * -1));
        Utils.writeValue(WM8994.BASS_BOOST_FILES[1], String.valueOf((long) mGain * mGain1));
        Utils.writeValue(WM8994.BASS_BOOST_FILES[2], String.valueOf((long) mGain * mGain2));
        //Utils.writeValue("/sys/class/misc/wm8994_sound/digital_gain", String.valueOf((long) mGain));
        Log.d(TAG, "writeBassBoost");

    }

    class BassBoostSeekBar implements SeekBar.OnSeekBarChangeListener {

        private String mPref;

        private int mOriginal;

        private SeekBar mSeekBar;

        private TextView mValueDisplay;

        private int mSeekbarMax;

        private String mSeekbarUOM;

        public BassBoostSeekBar(SeekBar seekBar, TextView valueDisplay, String pref, int maxValue, String uom) {
            Log.d(TAG, "BassBoostSeekBar");
            int iValue;

            mSeekBar = seekBar;
            mValueDisplay = valueDisplay;
            mPref = pref;
            mSeekbarMax = maxValue;
            mSeekbarUOM = uom;

            SharedPreferences sharedPreferences = getSharedPreferences();

            iValue = sharedPreferences.getInt(mPref, mSeekbarMax);
            mOriginal = iValue;

            mSeekBar.setMax(mSeekbarMax);
            reset();
            mSeekBar.setOnSeekBarChangeListener(this);
        }

        public void reset() {
            Log.d(TAG, "reset");
            int iValue;

            iValue = mOriginal;
            mSeekBar.setProgress(mOriginal);
            updateValue(iValue);
        }

        public void save() {
            Log.d(TAG, "save");
            int iValue;

            iValue = mSeekBar.getProgress();
            Editor editor = getEditor();
            editor.putInt(mPref, iValue);
            editor.commit();
            BassBoostPreference.this.writeBassBoost(getContext());
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int iValue;

            iValue = progress;
            //Utils.writeValue(mFilePath, String.valueOf((long) progress));
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
            double mProgress = (double) progress;

            if (mPref.equalsIgnoreCase(WM8994.BASS_BOOST_PREF_RANGE)) {
                mProgress = (mProgress / 5) * 100;
            }

            mValueDisplay.setText(String.format("%d", (int) mProgress) + mSeekbarUOM);
        }

    }
}
