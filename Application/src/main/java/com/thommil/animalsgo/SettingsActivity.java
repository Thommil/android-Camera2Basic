package com.thommil.animalsgo;

import android.app.Activity;
import android.os.Bundle;

import com.thommil.animalsgo.data.Settings;

public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new Settings.SettingsFragment())
                .commit();
    }
}
