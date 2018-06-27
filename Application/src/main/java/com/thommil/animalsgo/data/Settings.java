package com.thommil.animalsgo.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.thommil.animalsgo.R;

import java.util.Set;

/**
 * tooling class used to get/set preferences
 */
public class Settings {

    private static final String TAG = "A_GO/Settings";

    // Static settings
    // Number of frames between 2 updates
    public static final int CAPTURE_UPDATE_FREQUENCY = 10;

    // Mvt detection sensibility (more = less sensible)
    public static final float MOVEMENT_THRESHOLD = 1f;

    // Settings keys & values

    //Preview Settings values
    public static final String CAMERA_PREVIEW_QUALITY = "prefs_camera_preview_quality";
    public static final String PREVIEW_QUALITY_LOW = "low";
    public static final String PREVIEW_QUALITY_MEDIUM = "medium";
    public static final String PREVIEW_QUALITY_HIGH = "high";

    //Plugins default values

    private static Settings sInstance;
    public static final String PLUGINS_DEFAULT = "prefs_plugins_default";

    private final SharedPreferences mSharedPreferences;

    private Settings(final Context context){
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static Settings newInstance(final Context context){
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
        sInstance = new Settings(context);
        return sInstance;
    }

    public static Settings getInstance(){
        return sInstance;
    }

    public String getString(final String key){
        return mSharedPreferences.getString(key, null);
    }

    public void setString(final String key, final String value){
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public int getInt(final String key){
        return mSharedPreferences.getInt(key, 0);
    }

    public void setInt(final String key, final int value){
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public boolean getBoolean(final String key){
        return mSharedPreferences.getBoolean(key, false);
    }

    public void setBoolean(final String key, final boolean value){
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public float getFloat(final String key){
        return mSharedPreferences.getFloat(key, 0);
    }

    public void setFloat(final String key, final float value){
        final SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }
    }

}
