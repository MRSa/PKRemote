package net.osdn.gokigen.pkremote.preference.theta;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpSendCommandDialog;
import net.osdn.gokigen.pkremote.camera.vendor.theta.operation.ThetaCameraPowerOff;
import net.osdn.gokigen.pkremote.logcat.LogCatViewer;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;
import net.osdn.gokigen.pkremote.scene.IChangeScene;

import java.util.Map;

import static net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor.DEBUG_INFO;
import static net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor.EXIT_APPLICATION;
import static net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor.HTTP_COMMAND_SEND_DIALOG;
import static net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor.WIFI_SETTINGS;

/**
 *
 *
 */
public class ThetaPreferenceFragment  extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener
{
    private final String TAG = toString();
    private AppCompatActivity context = null;
    private SharedPreferences preferences = null;
    private ThetaCameraPowerOff powerOffController = null;
    private LogCatViewer logCatViewer = null;

    /**
     *
     *
     */
    public static ThetaPreferenceFragment newInstance(@NonNull AppCompatActivity context, @NonNull IChangeScene changeScene)
    {
        ThetaPreferenceFragment instance = new ThetaPreferenceFragment();
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
            powerOffController = new ThetaCameraPowerOff(context, changeScene);
            powerOffController.prepare();

            logCatViewer = new LogCatViewer(changeScene);
            logCatViewer.prepare();

            //cameraApiListViewer = new PanasonicCameraApiListViewer(changeScene);
            //cameraApiListViewer.prepare();

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
    public void onAttach(@NonNull Context activity)
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

            if (!items.containsKey(IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA)) {
                editor.putBoolean(IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA, true);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW)) {
                editor.putBoolean(IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW, true);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.CONNECTION_METHOD)) {
                editor.putString(IPreferencePropertyAccessor.CONNECTION_METHOD, IPreferencePropertyAccessor.CONNECTION_METHOD_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.GET_SMALL_PICTURE_AS_VGA))
            {
                editor.putBoolean(IPreferencePropertyAccessor.GET_SMALL_PICTURE_AS_VGA, false);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.USE_SMARTPHONE_TRANSFER_MODE))
            {
                editor.putBoolean(IPreferencePropertyAccessor.USE_SMARTPHONE_TRANSFER_MODE, false);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.RICOH_GET_PICS_LIST_TIMEOUT)) {
                editor.putString(IPreferencePropertyAccessor.RICOH_GET_PICS_LIST_TIMEOUT, IPreferencePropertyAccessor.RICOH_GET_PICS_LIST_TIMEOUT_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.USE_OSC_THETA_V21)) {
                editor.putBoolean(IPreferencePropertyAccessor.USE_OSC_THETA_V21, false);
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
            if (!items.containsKey(IPreferencePropertyAccessor.VISIONKIDS_AUTO_SET_HOST_IP))
            {
                editor.putBoolean(IPreferencePropertyAccessor.VISIONKIDS_AUTO_SET_HOST_IP, true);
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

                case IPreferencePropertyAccessor.USE_OSC_THETA_V21:
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
            addPreferencesFromResource(R.xml.preferences_theta);

            ListPreference connectionMethod = findPreference(IPreferencePropertyAccessor.CONNECTION_METHOD);
            if (connectionMethod != null)
            {
                connectionMethod.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        preference.setSummary(newValue + " ");
                        return (true);
                    }
                });
                connectionMethod.setSummary(connectionMethod.getValue() + " ");
            }

            setOnPreferenceClickListener(EXIT_APPLICATION, powerOffController);
            setOnPreferenceClickListener(DEBUG_INFO, logCatViewer);
            setOnPreferenceClickListener(WIFI_SETTINGS, this);
            setOnPreferenceClickListener(HTTP_COMMAND_SEND_DIALOG, this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void setOnPreferenceClickListener(String key, Preference.OnPreferenceClickListener listener)
    {
        Preference preference = findPreference(key);
        if (preference != null)
        {
            preference.setOnPreferenceClickListener(listener);
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
            pref = findPreference(pref_key);
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
            CheckBoxPreference pref = findPreference(pref_key);
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
                        setBooleanPreference(IPreferencePropertyAccessor.USE_OSC_THETA_V21, IPreferencePropertyAccessor.USE_OSC_THETA_V21, false);
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
            Log.v(TAG, " onPreferenceClick : " + preferenceKey);
            if (preferenceKey.contains(WIFI_SETTINGS))
            {
                // Wifi 設定画面を表示する
                if (context != null)
                {
                    context.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            }
            else if (preferenceKey.contains(HTTP_COMMAND_SEND_DIALOG))
            {
                // HTTP送信ダイアログを表示する
                SimpleHttpSendCommandDialog.newInstance("http://192.168.1.1/", null, null).show(context.getSupportFragmentManager(), "sendCommandDialog");
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
