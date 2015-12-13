/*
 * Copyright 2015 Fabio Bombace
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.colordetector;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener
{
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		// setting default values into the summaryes
		ListPreference listPreference = (ListPreference) findPreference(CamMainActivity.KEY_PREF_COLOR); // color
		listPreference.setSummary(listPreference.getEntry());
		listPreference = (ListPreference) findPreference(CamMainActivity.KEY_PREF_QUALITY); // quality
		listPreference.setSummary(listPreference.getEntry());
		listPreference = (ListPreference) findPreference(CamMainActivity.KEY_PREF_METHOD); // shooting method
		listPreference.setSummary(listPreference.getEntry());

		// enable automatic type shooting only if the shooting method is set to automatic
		listPreference = (ListPreference) findPreference(CamMainActivity.KEY_PREF_AUTOMATIC_SHOOT); // automatic shooting method
		if(listPreference.getEntry().toString().compareTo(getString(R.string.manual)) == 0)
		{
			listPreference.setEnabled(false);
		}
		else
		{
			listPreference.setSummary(listPreference.getEntry());
		}
		listPreference = (ListPreference) findPreference(CamMainActivity.KEY_PREF_WHAT_SAVE); // what image (shoot and/or mask) save
		listPreference.setSummary(listPreference.getEntry());
	}

	// Register the listener of the menu changes
	@SuppressWarnings("deprecation")
	@Override
	protected void onResume()
	{
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	// un-register the listener of the menu changes
	@SuppressWarnings("deprecation")
	@Override
	protected void onPause()
	{
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	// listener for the menu changes
	@SuppressWarnings("deprecation")
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if (key.equals(CamMainActivity.KEY_PREF_COLOR))
		{
			ListPreference listColor = (ListPreference) findPreference(key);
			listColor.setSummary(listColor.getEntry());
		}
		else if(key.equals(CamMainActivity.KEY_PREF_QUALITY))
		{
			ListPreference listQuality = (ListPreference) findPreference(key);
			listQuality.setSummary(listQuality.getEntry());
		}
		else if(key.equals(CamMainActivity.KEY_PREF_METHOD))
		{
			ListPreference listMethod = (ListPreference) findPreference(key);
			listMethod.setSummary(listMethod.getEntry());

			// Disable automatic shooting mode (countdown, immediate) if the shoot is set to manual
			if(listMethod.getEntry().toString().compareTo(getString(R.string.manual)) == 0)
			{
				ListPreference listAutomaticShoot = (ListPreference) findPreference(CamMainActivity.KEY_PREF_AUTOMATIC_SHOOT);
				listAutomaticShoot.setEnabled(false);
			}
			else
			{
				ListPreference listAutomaticShoot = (ListPreference) findPreference(CamMainActivity.KEY_PREF_AUTOMATIC_SHOOT);
				listAutomaticShoot.setEnabled(true);
				listAutomaticShoot.setSummary(listAutomaticShoot.getEntry());
			}
		}
		else if(key.equals(CamMainActivity.KEY_PREF_AUTOMATIC_SHOOT))
		{
			ListPreference listAutomaticShoot = (ListPreference) findPreference(key);
			listAutomaticShoot.setSummary(listAutomaticShoot.getEntry());
		}
		else if(key.equals(CamMainActivity.KEY_PREF_WHAT_SAVE))
		{
			ListPreference listWhatSave = (ListPreference) findPreference(key);
			listWhatSave.setSummary(listWhatSave.getEntry());
		}
	}
}