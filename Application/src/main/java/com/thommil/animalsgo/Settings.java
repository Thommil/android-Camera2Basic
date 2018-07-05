package com.thommil.animalsgo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * tooling class used to get/set preferences
 */
public class Settings {

    private static final String TAG = "A_GO/Settings";

    // Static settings
    // Number of frames between 2 updates
    public static final int CAPTURE_UPDATE_FREQUENCY = 10;

    // Capture image ratio (4/3)
    public static final float CAPTURE_RATIO = 0.75f;

    // Mvt detection sensibility (more = less sensible)
    public static final float MOVEMENT_THRESHOLD = 1f;

    // Mvt detection sensibility threshold for orientation change trigger
    public static final float MOVEMENT_ORIENTATION_CHANGE_THRESHOLD = 2f;

    // Maximum zoom value
    public static final float ZOOM_MAX = 10.0f;

    // Zoom velocity
    public static final float ZOOM_VELOCITY = 50f;

    // Landscape mode threshold on orientation
    public static final float[] LANSCAPE_MODE_VERTICAL_TRESHOLDS = new float[]{9f, 6f};

    // Settings keys & values
    public static final String CAMERA_PREVIEW_QUALITY_AUTO = "prefs_camera_preview_quality_auto";
    public static final String CAMERA_PREVIEW_QUALITY = "prefs_camera_preview_quality";
    // TODO in Prefs ?
    public static final String PLUGINS_CAMERA_DEFAULT = "camera_basic";
    public static final String PLUGINS_PREVIEW_DEFAULT = "prefs_plugins_default";
    public static final String PLUGINS_UI_DEFAULT = "ui_basic";

    private final SharedPreferences mSharedPreferences;

    private static Settings sInstance;

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

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            //Auto/manual quality preview
            if(key.equals(CAMERA_PREVIEW_QUALITY_AUTO)){
                findPreference(CAMERA_PREVIEW_QUALITY).setEnabled(!getInstance().getBoolean(CAMERA_PREVIEW_QUALITY_AUTO));
            }

        }

        @Override
        public void onResume() {
            super.onResume();
            findPreference(CAMERA_PREVIEW_QUALITY).setEnabled(!getInstance().getBoolean(CAMERA_PREVIEW_QUALITY_AUTO));
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }
    }

}
