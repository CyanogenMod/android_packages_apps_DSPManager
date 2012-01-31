/*
 * Copyright (C) 2011 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bel.android.dspmanager.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.DialogPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.bel.android.dspmanager.R;
import com.bel.android.dspmanager.preference.HeadsetAmplifierPreference;

public class WM8994 extends PreferenceFragment {
    protected static final String TAG = WM8994.class.getSimpleName();

    public static final String WM8994_CONFIG = "wm8994";
    public static final String WM8994_ENABLE_FILE = "/sys/class/misc/wm8994_sound_control/enable";

    public static final String aOptionControl[][] = {
        {"/sys/class/misc/wm8994_sound_control/enable","pref_wm8994_control_enable"},
        {"/sys/class/misc/wm8994_sound/speaker_tuning","pref_wm8994_speaker_tuning"},
        {"/sys/class/misc/wm8994_sound/mono_downmix","pref_wm8994_mono_downmix"},
        {"/sys/class/misc/wm8994_sound/stereo_expansion","pref_wm8994_stereo_expansion"},
        {"/sys/class/misc/wm8994_sound/dac_direct","pref_wm8994_dac_direct"},
        {"/sys/class/misc/wm8994_sound/dac_osr128","pref_wm8994_dac_osr128"},
        {"/sys/class/misc/wm8994_sound/adc_osr128","pref_wm8994_adc_osr128"},
        {"/sys/class/misc/wm8994_sound/fll_tuning","pref_wm8994_fll_tuning"}
    };

    public static final String microphone_recording_preset[][] = {
        {"/sys/class/misc/wm8994_sound/recording_preset","dsp.wm8994.microphone.recording"}
    };

    public static final String BASS_BOOST_ENABLE_PREF = "dsp.wm8994.bassboost.enable";
    public static final String BASS_BOOST_PRESET_PREF = "dsp.wm8994.bassboost.preset";
    public static final String BASS_BOOST_GAIN_RANGE_PREF = "dsp.wm8994.bassboost.gainrange";

    public static final String BASS_BOOST_PREF_GAIN = "dsp.wm8994.bassboost.gain";
    public static final String BASS_BOOST_PREF_RANGE = "dsp.wm8994.bassboost.range";
    public static final String[] BASS_BOOST_PREFS = new String[] {
        BASS_BOOST_PREF_GAIN
        ,BASS_BOOST_PREF_RANGE
    };
    public static final int MAX_VALUE_GAIN = 12;
    public static final int MAX_VALUE_RANGE = 5;

    public static final String BASS_BOOST_ENABLE_FILE = "/sys/class/misc/wm8994_sound/headphone_eq";
    public static final String[] BASS_BOOST_FILES = new String[] {
        "/sys/class/misc/wm8994_sound/digital_gain"
        ,"/sys/class/misc/wm8994_sound/headphone_eq_b1_gain"
        ,"/sys/class/misc/wm8994_sound/headphone_eq_b2_gain"
    };

    private static final Integer iTotalOptions = aOptionControl.length;
    private static final String PREF_ENABLED = "1";

    private CheckBoxPreference cbpStatus[] = new CheckBoxPreference[iTotalOptions];

    private final OnSharedPreferenceChangeListener listener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            if (microphone_recording_preset[0][1].equals(key)) {
                String newValue = sharedPreferences.getString(key, null);
                Editor e = sharedPreferences.edit();
                e.putString(key, newValue);
                Log.d(TAG,"Setting " + key + " == " + newValue);
                e.commit();
                Utils.writeValue(microphone_recording_preset[0][0], newValue);
            } else if (BASS_BOOST_ENABLE_PREF.equals(key) || BASS_BOOST_PRESET_PREF.equals(key)) {
                if (BASS_BOOST_ENABLE_PREF.equals(key)) {
                    Boolean newValue = sharedPreferences.getBoolean(key, true);
                    Editor e = sharedPreferences.edit();
                    e.putBoolean(key, newValue);
                    Log.d(TAG,"Setting " + key + " == " + newValue);
                    e.commit();
                    if (newValue) {
                        Utils.writeValue(BASS_BOOST_ENABLE_FILE, "1");
                    } else {
                        Utils.writeValue(BASS_BOOST_ENABLE_FILE, "0");
                    }
                } else if (BASS_BOOST_PRESET_PREF.equals(key)) {
                    String newValue = sharedPreferences.getString(key, null);
                    Editor e = sharedPreferences.edit();
                    e.putString(key, newValue);
                    Log.d(TAG,"Setting " + key + " == " + newValue);
                    e.commit();
                }
                writeBassBoost();
            }
            getActivity().sendBroadcast(new Intent(DSPManager.ACTION_UPDATE_PREFERENCES));
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String config = getArguments().getString("config");

        try {
            int xmlId = R.xml.class.getField(config + "_preferences").getInt(null);
            addPreferencesFromResource(xmlId);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        PreferenceScreen prefSet = getPreferenceScreen();

        Integer iPosition;
        for(iPosition=0;iPosition<iTotalOptions;iPosition++) {
            if (isSupported(aOptionControl[iPosition][0])) {
                cbpStatus[iPosition] = (CheckBoxPreference) prefSet.findPreference(aOptionControl[iPosition][1]);
                cbpStatus[iPosition].setChecked(PREF_ENABLED.equals(Utils.readOneLine(aOptionControl[iPosition][0])));
            } else {
                cbpStatus[iPosition] = (CheckBoxPreference) prefSet.findPreference(aOptionControl[iPosition][1]);
                cbpStatus[iPosition].setSummary(R.string.pref_unavailable);
                cbpStatus[iPosition].setEnabled(false);
            }
        }

        if (!isSupported(HeadsetAmplifierPreference.FILE_PATH)) {
            Preference mHeadSet = prefSet.findPreference("headphone_amp");
            PreferenceCategory mHeadSetCategory = (PreferenceCategory) prefSet.findPreference("wm8994_headphone_amp_category");
            mHeadSetCategory.removePreference(mHeadSet);
            prefSet.removePreference(mHeadSetCategory);
        }
        if (!isSupported(microphone_recording_preset[0][0])) {
            Preference mMicrophone = prefSet.findPreference(microphone_recording_preset[0][1]);
            PreferenceCategory mMicrophoneCategory = (PreferenceCategory) prefSet.findPreference("wm8994_microphone_recording_category");
            mMicrophoneCategory.removePreference(mMicrophone);
            prefSet.removePreference(mMicrophoneCategory);
        }
        if (!isSupported(BASS_BOOST_ENABLE_FILE)) {
            Preference mBassBoostEnable = prefSet.findPreference(BASS_BOOST_ENABLE_PREF);
            Preference mBassBoostPreset = prefSet.findPreference(BASS_BOOST_PRESET_PREF);
            Preference mBassBoostGainRange = prefSet.findPreference(BASS_BOOST_GAIN_RANGE_PREF);
            PreferenceCategory mBassBoostCategory = (PreferenceCategory) prefSet.findPreference("wm8994_signal_processing_category");
            mBassBoostCategory.removePreference(mBassBoostEnable);
            mBassBoostCategory.removePreference(mBassBoostPreset);
            mBassBoostCategory.removePreference(mBassBoostGainRange);
        }

        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);

        Integer iPosition;
        String boxValue;
        Boolean boxValueBool;
        for(iPosition=0;iPosition<iTotalOptions;iPosition++) {
            if (preference == cbpStatus[iPosition]) {
                boxValueBool = cbpStatus[iPosition].isChecked();
                boxValue = boxValueBool ? "1" : "0";
                Utils.writeValue(aOptionControl[iPosition][0], boxValue);
                Editor e = getPreferenceManager().getSharedPreferences().edit();
                e.putBoolean(aOptionControl[iPosition][1], boxValueBool);
                Log.d(TAG,"Setting " + aOptionControl[iPosition][1] + " == " + boxValueBool);
                e.commit();
            }
        }
        return true;
    }

    public static boolean isSupported(String FILE) {
        return Utils.fileExists(FILE);
    }

    public static void restore(Context context) {

        Integer iPosition;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        for(iPosition=0;iPosition<iTotalOptions;iPosition++) {
            if (isSupported(aOptionControl[iPosition][0])) {
                Log.d(TAG,"Does " + aOptionControl[iPosition][1] + " exist == " + sharedPrefs.contains(aOptionControl[iPosition][1]));
                Utils.writeValue(aOptionControl[iPosition][0], sharedPrefs.getBoolean(aOptionControl[iPosition][1], PREF_ENABLED.equals(Utils.readOneLine(aOptionControl[iPosition][0]))));
            }
        }

        if (isSupported(microphone_recording_preset[0][0])) {
            Log.d(TAG,"Does " + microphone_recording_preset[0][1] + " exist == " + sharedPrefs.contains(microphone_recording_preset[0][1]));
            Utils.writeValue(microphone_recording_preset[0][0], sharedPrefs.getString(microphone_recording_preset[0][1], Utils.readOneLine(microphone_recording_preset[0][0])));
        }

        HeadsetAmplifierPreference.restore(context);

    }

    public void writeBassBoost() {
        SharedPreferences sharedPrefs = getPreferenceManager().getSharedPreferences();
        int mGain = sharedPrefs.getInt(BASS_BOOST_PREF_GAIN, MAX_VALUE_GAIN);
        int mRange = sharedPrefs.getInt(BASS_BOOST_PREF_RANGE, MAX_VALUE_RANGE);
        String mPreset = sharedPrefs.getString(BASS_BOOST_PRESET_PREF, "0");

        double mDigitalGain = (double) mRange;
        int mGain1 = 1;
        int mGain2 = 1;
        if (mPreset.equals("0")) {
            mGain2 = 0;
        } else {
            mGain1 = -1;
        }

        mDigitalGain = (mDigitalGain / 5) * (mGain * 1000);
        Utils.writeValue(BASS_BOOST_FILES[0], String.valueOf((long) mDigitalGain * -1));
        Utils.writeValue(BASS_BOOST_FILES[1], String.valueOf((long) mGain * mGain1));
        Utils.writeValue(BASS_BOOST_FILES[2], String.valueOf((long) mGain * mGain2));
        Log.d(TAG, "writeBassBoost");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
    }

}
