package net.osdn.gokigen.pkremote.preference.ricohgr2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.provider.Settings;
import android.util.Log;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.vendor.ricoh.operation.CameraPowerOffRicohGr2;
import net.osdn.gokigen.pkremote.logcat.LogCatViewer;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;
import net.osdn.gokigen.pkremote.scene.IChangeScene;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import static net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor.DEBUG_INFO;
import static net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor.EXIT_APPLICATION;
import static net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor.WIFI_SETTINGS;

public class RicohGr2PreferenceFragment  extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener
{
    private final String TAG = toString();
    private AppCompatActivity context = null;
    private SharedPreferences preferences = null;
    private CameraPowerOffRicohGr2 powerOffController = null;
    private LogCatViewer logCatViewer = null;

    /**
     *
     *
     */
    public static RicohGr2PreferenceFragment newInstance(@NonNull AppCompatActivity context, @NonNull IChangeScene changeScene)
    {
        RicohGr2PreferenceFragment instance = new RicohGr2PreferenceFragment();
        instance.prepare(context, changeScene);

        // パラメータはBundleにまとめておく
        Bundle arguments = new Bundle();
        //arguments.putString("title", title);
        //arguments.putString("message", message);
        instance.setArguments(arguments);

        return (instance);
    }

    /**
     *
     *
     */
    private void prepare(@NonNull AppCompatActivity context, @NonNull IChangeScene changeScene)
    {
        try
        {
            powerOffController = new CameraPowerOffRicohGr2(context, changeScene);
            powerOffController.prepare();

            logCatViewer = new LogCatViewer(changeScene);
            logCatViewer.prepare();

            this.context = context;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     *
     */
    @Override
    public void onAttach(Context activity)
    {
        super.onAttach(activity);
        Log.v(TAG, "onAttach()");
        try
        {
            // Preference をつかまえる
            preferences = PreferenceManager.getDefaultSharedPreferences(activity);

            // Preference を初期設定する
            initializePreferences();

            preferences.registerOnSharedPreferenceChangeListener(this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Preferenceの初期化...
     *
     */
    private void initializePreferences()
    {
        try
        {
            Map<String, ?> items = preferences.getAll();
            SharedPreferences.Editor editor = preferences.edit();

            if (!items.containsKey(IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA))
            {
                editor.putBoolean(IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA, true);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW))
            {
                editor.putBoolean(IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW, true);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.USE_PLAYBACK_MENU))
            {
                editor.putBoolean(IPreferencePropertyAccessor.USE_PLAYBACK_MENU, true);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.CONNECTION_METHOD))
            {
                editor.putString(IPreferencePropertyAccessor.CONNECTION_METHOD, IPreferencePropertyAccessor.CONNECTION_METHOD_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.GR2_DISPLAY_CAMERA_VIEW))
            {
                editor.putBoolean(IPreferencePropertyAccessor.GR2_DISPLAY_CAMERA_VIEW, true);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.GR2_LCD_SLEEP))
            {
                editor.putBoolean(IPreferencePropertyAccessor.GR2_LCD_SLEEP, false);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.SHARE_AFTER_SAVE))
            {
                editor.putBoolean(IPreferencePropertyAccessor.SHARE_AFTER_SAVE, false);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.USE_GR2_SPECIAL_COMMAND))
            {
                editor.putBoolean(IPreferencePropertyAccessor.USE_GR2_SPECIAL_COMMAND, true);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.PENTAX_CAPTURE_AFTER_AF))
            {
                editor.putBoolean(IPreferencePropertyAccessor.PENTAX_CAPTURE_AFTER_AF, false);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.SMALL_PICTURE_SIZE))
            {
                editor.putString(IPreferencePropertyAccessor.SMALL_PICTURE_SIZE, IPreferencePropertyAccessor.SMALL_PICTURE_SIZE_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.RICOH_GET_PICS_LIST_TIMEOUT))
            {
                editor.putString(IPreferencePropertyAccessor.RICOH_GET_PICS_LIST_TIMEOUT, IPreferencePropertyAccessor.RICOH_GET_PICS_LIST_TIMEOUT_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.RICOH_GET_PICS_LIST_MAX_COUNT))
            {
                editor.putString(IPreferencePropertyAccessor.RICOH_GET_PICS_LIST_MAX_COUNT, IPreferencePropertyAccessor.RICOH_GET_PICS_LIST_MAX_COUNT_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.PIXPRO_HOST_IP))
            {
                editor.putString(IPreferencePropertyAccessor.PIXPRO_HOST_IP, IPreferencePropertyAccessor.PIXPRO_HOST_IP_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.PIXPRO_COMMAND_PORT))
            {
                editor.putString(IPreferencePropertyAccessor.PIXPRO_COMMAND_PORT, IPreferencePropertyAccessor.PIXPRO_COMMAND_PORT_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.PIXPRO_GET_PICS_LIST_TIMEOUT))
            {
                editor.putString(IPreferencePropertyAccessor.PIXPRO_GET_PICS_LIST_TIMEOUT, IPreferencePropertyAccessor.PIXPRO_GET_PICS_LIST_TIMEOUT_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.THUMBNAIL_IMAGE_CACHE_SIZE))
            {
                editor.putString(IPreferencePropertyAccessor.THUMBNAIL_IMAGE_CACHE_SIZE, IPreferencePropertyAccessor.THUMBNAIL_IMAGE_CACHE_SIZE_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.CANON_HOST_IP)) {
                editor.putString(IPreferencePropertyAccessor.CANON_HOST_IP, IPreferencePropertyAccessor.CANON_HOST_IP_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.CANON_CONNECTION_SEQUENCE)) {
                editor.putString(IPreferencePropertyAccessor.CANON_CONNECTION_SEQUENCE, IPreferencePropertyAccessor.CANON_CONNECTION_SEQUENCE_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.CANON_SMALL_PICTURE_TYPE)) {
                editor.putString(IPreferencePropertyAccessor.CANON_SMALL_PICTURE_TYPE, IPreferencePropertyAccessor.CANON_SMALL_PICTURE_TYPE_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.VISIONKIDS_HOST_IP)) {
                editor.putString(IPreferencePropertyAccessor.VISIONKIDS_HOST_IP, IPreferencePropertyAccessor.VISIONKIDS_HOST_IP_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.VISIONKIDS_FTP_USER)) {
                editor.putString(IPreferencePropertyAccessor.VISIONKIDS_FTP_USER, IPreferencePropertyAccessor.VISIONKIDS_FTP_USER_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.VISIONKIDS_FTP_PASS)) {
                editor.putString(IPreferencePropertyAccessor.VISIONKIDS_FTP_PASS, IPreferencePropertyAccessor.VISIONKIDS_FTP_PASS_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.VISIONKIDS_LIST_TIMEOUT)) {
                editor.putString(IPreferencePropertyAccessor.VISIONKIDS_LIST_TIMEOUT, IPreferencePropertyAccessor.VISIONKIDS_LIST_TIMEOUT_DEFAULT_VALUE);
            }
            editor.apply();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     *
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        Log.v(TAG, "onSharedPreferenceChanged() : " + key);
        boolean value;
        if (key != null)
        {
            switch (key)
            {
                case IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA:
                    value = preferences.getBoolean(key, true);
                    Log.v(TAG, " " + key + " , " + value);
                    break;

                case IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW:
                    value = preferences.getBoolean(key, true);
                    Log.v(TAG, " " + key + " , " + value);
                    break;

                case IPreferencePropertyAccessor.USE_PLAYBACK_MENU:
                    value = preferences.getBoolean(key, true);
                    Log.v(TAG, " " + key + " , " + value);
                    break;

                case IPreferencePropertyAccessor.GR2_DISPLAY_CAMERA_VIEW:
                    value = preferences.getBoolean(key, true);
                    Log.v(TAG, " " + key + " , " + value);
                    break;

                case IPreferencePropertyAccessor.GR2_LCD_SLEEP:
                    value = preferences.getBoolean(key, false);
                    Log.v(TAG, " " + key + " , " + value);
                    break;

                case IPreferencePropertyAccessor.SHARE_AFTER_SAVE:
                    value = preferences.getBoolean(key, false);
                    Log.v(TAG, " " + key + " , " + value);
                    break;

                case IPreferencePropertyAccessor.USE_GR2_SPECIAL_COMMAND:
                    value = preferences.getBoolean(key, true);
                    Log.v(TAG, " " + key + " , " + value);
                    break;

                case IPreferencePropertyAccessor.PENTAX_CAPTURE_AFTER_AF:
                    value = preferences.getBoolean(key, false);
                    Log.v(TAG, " " + key + " , " + value);
                    break;

                default:
                    String strValue = preferences.getString(key, "");
                    setListPreference(key, key, strValue);
                    break;
            }
        }
    }

    /**
     *
     *
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        Log.v(TAG, "onCreatePreferences()");
        try
        {
            //super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences_ricoh_gr2);

            ListPreference connectionMethod = (ListPreference) findPreference(IPreferencePropertyAccessor.CONNECTION_METHOD);
            connectionMethod.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary(newValue + " ");
                    return (true);
                }
            });
            connectionMethod.setSummary(connectionMethod.getValue() + " ");
/*
            ListPreference smallPictureSize = (ListPreference) findPreference(IPreferencePropertyAccessor.SMALL_PICTURE_SIZE);
            smallPictureSize.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    preference.setSummary(newValue + " ");
                    return (true);
                }
            });
            smallPictureSize.setSummary(smallPictureSize.getValue() + " ");
*/
            findPreference(EXIT_APPLICATION).setOnPreferenceClickListener(powerOffController);
            findPreference(DEBUG_INFO).setOnPreferenceClickListener(logCatViewer);
            findPreference(WIFI_SETTINGS).setOnPreferenceClickListener(this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     *
     */
    @Override
    public void onResume()
    {
        super.onResume();
        Log.v(TAG, "onResume() Start");
        try
        {
            synchronizedProperty();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.v(TAG, "onResume() End");
    }

    /**
     *
     *
     */
    @Override
    public void onPause()
    {
        super.onPause();
        Log.v(TAG, "onPause() Start");
        try
        {
            // Preference変更のリスナを解除
            preferences.unregisterOnSharedPreferenceChangeListener(this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Log.v(TAG, "onPause() End");
    }

    /**
     * ListPreference の表示データを設定
     *
     * @param pref_key     Preference(表示)のキー
     * @param key          Preference(データ)のキー
     * @param defaultValue Preferenceのデフォルト値
     */
    private void setListPreference(String pref_key, String key, String defaultValue)
    {
        try
        {
            ListPreference pref;
            pref = (ListPreference) findPreference(pref_key);
            String value = preferences.getString(key, defaultValue);
            if (pref != null)
            {
                pref.setValue(value);
                pref.setSummary(value);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * BooleanPreference の表示データを設定
     *
     * @param pref_key     Preference(表示)のキー
     * @param key          Preference(データ)のキー
     * @param defaultValue Preferenceのデフォルト値
     */
    private void setBooleanPreference(String pref_key, String key, boolean defaultValue)
    {
        try
        {
            CheckBoxPreference pref = (CheckBoxPreference) findPreference(pref_key);
            if (pref != null) {
                boolean value = preferences.getBoolean(key, defaultValue);
                pref.setChecked(value);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     *
     */
    private void synchronizedProperty()
    {
        final FragmentActivity activity = getActivity();
        final boolean defaultValue = true;
        if (activity != null)
        {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try
                    {
                        // Preferenceの画面に反映させる
                        setBooleanPreference(IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA, IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA, defaultValue);
                        setBooleanPreference(IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW, IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW, defaultValue);
                        setBooleanPreference(IPreferencePropertyAccessor.USE_PLAYBACK_MENU, IPreferencePropertyAccessor.USE_PLAYBACK_MENU, defaultValue);
                        setBooleanPreference(IPreferencePropertyAccessor.GR2_DISPLAY_CAMERA_VIEW, IPreferencePropertyAccessor.GR2_DISPLAY_CAMERA_VIEW, defaultValue);
                        setBooleanPreference(IPreferencePropertyAccessor.GR2_LCD_SLEEP, IPreferencePropertyAccessor.GR2_LCD_SLEEP, defaultValue);
                        setBooleanPreference(IPreferencePropertyAccessor.SHARE_AFTER_SAVE, IPreferencePropertyAccessor.SHARE_AFTER_SAVE, defaultValue);
                        setBooleanPreference(IPreferencePropertyAccessor.USE_GR2_SPECIAL_COMMAND, IPreferencePropertyAccessor.USE_GR2_SPECIAL_COMMAND, defaultValue);
                        setBooleanPreference(IPreferencePropertyAccessor.PENTAX_CAPTURE_AFTER_AF, IPreferencePropertyAccessor.PENTAX_CAPTURE_AFTER_AF, false);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference)
    {
        try
        {
            String preferenceKey = preference.getKey();
            if (preferenceKey.contains(WIFI_SETTINGS))
            {
                // Wifi 設定画面を表示する
                Log.v(TAG, " onPreferenceClick : " + preferenceKey);
                if (context != null)
                {
                    context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            }
            return (true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (false);
    }
}
