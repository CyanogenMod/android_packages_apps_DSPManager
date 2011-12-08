package com.bel.android.dspmanager.activity;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.bel.android.dspmanager.R;
import com.bel.android.dspmanager.service.HeadsetService;

/**
 * Setting utility for CyanogenMod's DSP capabilities.
 * This page is displays the top-level configurations menu.
 *
 * @author alankila@gmail.com
 */
public final class DSPManager extends FragmentActivity {
	public static final String SHARED_PREFERENCES_BASENAME = "com.bel.android.dspmanager";
	public static final String ACTION_UPDATE_PREFERENCES = "com.bel.android.dspmanager.UPDATE";

	protected MyAdapter pagerAdapter;
	protected ActionBar actionBar;
	protected ViewPager viewPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.top);

		pagerAdapter = new MyAdapter(getFragmentManager());
		actionBar = getActionBar();
		viewPager = (ViewPager) findViewById(R.id.viewPager);

		Intent serviceIntent = new Intent(this, HeadsetService.class);
		startService(serviceIntent);

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(true);

		for (String entry : pagerAdapter.getEntries()) {
			ActionBar.Tab tab = actionBar.newTab();
			tab.setTabListener(new TabListener() {
				@Override
				public void onTabReselected(Tab tab, FragmentTransaction ft) {
				}

				@Override
				public void onTabSelected(Tab tab, FragmentTransaction ft) {
					viewPager.setCurrentItem(tab.getPosition());
				}

				@Override
				public void onTabUnselected(Tab tab, FragmentTransaction ft) {
				}
			});
			try {
				int stringId = R.string.class.getField(entry + "_title")
						.getInt(null);
				tab.setText(getString(stringId));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			actionBar.addTab(tab);
		}

		viewPager.setAdapter(pagerAdapter);
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int idx) {
				actionBar.selectTab(actionBar.getTabAt(idx));
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

    @Override
	public void onResume() {
		super.onResume();
		ServiceConnection connection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder binder) {
				HeadsetService service = ((HeadsetService.LocalBinder) binder).getService();
				String routing = service.getAudioOutputRouting();
				String[] entries = pagerAdapter.getEntries();
				for (int i = 0; i < entries.length; i ++) {
					if (routing.equals(entries[i])) {
						viewPager.setCurrentItem(i);
						actionBar.selectTab(actionBar.getTabAt(i));
						break;
					}
				}
				unbindService(this);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
			}
		};
		Intent serviceIntent = new Intent(this, HeadsetService.class);
		bindService(serviceIntent, connection, 0);
    }
}

class MyAdapter extends FragmentPagerAdapter {
	private final String[] entries = { "headset", "speaker", "bluetooth" };

	public MyAdapter(FragmentManager fm) {
		super(fm);
	}

	public String[] getEntries() {
		return entries;
	}

	@Override
	public int getCount() {
		return entries.length;
	}

	@Override
	public Fragment getItem(int position) {
		final DSPScreen dspFragment = new DSPScreen();
		Bundle b = new Bundle();
		b.putString("config", entries[position]);
		dspFragment.setArguments(b);
		return dspFragment;
	}
}