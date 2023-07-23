package net.osdn.gokigen.pkremote.preference.visionkids

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import net.osdn.gokigen.pkremote.R
import net.osdn.gokigen.pkremote.camera.vendor.visionkids.wrapper.connection.VisionKidsCameraPowerOff
import net.osdn.gokigen.pkremote.logcat.LogCatViewer
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor
import net.osdn.gokigen.pkremote.scene.IChangeScene

class VisionKidsPreferenceFragment: PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener
{
    private var context: AppCompatActivity? = null
    private var preferences: SharedPreferences? = null
    private var logCatViewer: LogCatViewer? = null
    private var powerOffController : VisionKidsCameraPowerOff? = null

    /**
     *
     *
     */
    private fun prepare(context: AppCompatActivity, changeScene: IChangeScene)
    {
        try
        {
            logCatViewer = LogCatViewer(changeScene)
            logCatViewer?.prepare()
            this.powerOffController = VisionKidsCameraPowerOff(context, changeScene)
            this.powerOffController?.prepare()
            this.context = context
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     *
     *
     */
    override fun onAttach(activity: Context)
    {
        super.onAttach(activity)
        Log.v(TAG, "onAttach()")
        try
        {
            // Preference をつかまえる
            preferences = PreferenceManager.getDefaultSharedPreferences(activity)

            // Preference を初期設定する
            initializePreferences()
            preferences?.registerOnSharedPreferenceChangeListener(this)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * Preferenceの初期化...
     *
     */
    private fun initializePreferences() {
        try {
            val items = preferences!!.all
            val editor = preferences!!.edit()
            if (!items.containsKey(IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA)) {
                editor.putBoolean(IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA, true)
            }
            if (!items.containsKey(IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW)) {
                editor.putBoolean(
                    IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW,
                    true
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.CONNECTION_METHOD)) {
                editor.putString(
                    IPreferencePropertyAccessor.CONNECTION_METHOD,
                    IPreferencePropertyAccessor.CONNECTION_METHOD_DEFAULT_VALUE
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.GET_SMALL_PICTURE_AS_VGA)) {
                editor.putBoolean(IPreferencePropertyAccessor.GET_SMALL_PICTURE_AS_VGA, false)
            }
            if (!items.containsKey(IPreferencePropertyAccessor.USE_SMARTPHONE_TRANSFER_MODE)) {
                editor.putBoolean(IPreferencePropertyAccessor.USE_SMARTPHONE_TRANSFER_MODE, false)
            }
            if (!items.containsKey(IPreferencePropertyAccessor.RICOH_GET_PICS_LIST_TIMEOUT)) {
                editor.putString(
                    IPreferencePropertyAccessor.RICOH_GET_PICS_LIST_TIMEOUT,
                    IPreferencePropertyAccessor.RICOH_GET_PICS_LIST_TIMEOUT_DEFAULT_VALUE
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.USE_OSC_THETA_V21)) {
                editor.putBoolean(IPreferencePropertyAccessor.USE_OSC_THETA_V21, false)
            }
            if (!items.containsKey(IPreferencePropertyAccessor.PIXPRO_HOST_IP)) {
                editor.putString(
                    IPreferencePropertyAccessor.PIXPRO_HOST_IP,
                    IPreferencePropertyAccessor.PIXPRO_HOST_IP_DEFAULT_VALUE
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.PIXPRO_COMMAND_PORT)) {
                editor.putString(
                    IPreferencePropertyAccessor.PIXPRO_COMMAND_PORT,
                    IPreferencePropertyAccessor.PIXPRO_COMMAND_PORT_DEFAULT_VALUE
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.PIXPRO_GET_PICS_LIST_TIMEOUT)) {
                editor.putString(
                    IPreferencePropertyAccessor.PIXPRO_GET_PICS_LIST_TIMEOUT,
                    IPreferencePropertyAccessor.PIXPRO_GET_PICS_LIST_TIMEOUT_DEFAULT_VALUE
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.THUMBNAIL_IMAGE_CACHE_SIZE)) {
                editor.putString(
                    IPreferencePropertyAccessor.THUMBNAIL_IMAGE_CACHE_SIZE,
                    IPreferencePropertyAccessor.THUMBNAIL_IMAGE_CACHE_SIZE_DEFAULT_VALUE
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.CANON_HOST_IP)) {
                editor.putString(
                    IPreferencePropertyAccessor.CANON_HOST_IP,
                    IPreferencePropertyAccessor.CANON_HOST_IP_DEFAULT_VALUE
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.CANON_CONNECTION_SEQUENCE)) {
                editor.putString(
                    IPreferencePropertyAccessor.CANON_CONNECTION_SEQUENCE,
                    IPreferencePropertyAccessor.CANON_CONNECTION_SEQUENCE_DEFAULT_VALUE
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.CANON_SMALL_PICTURE_TYPE)) {
                editor.putString(
                    IPreferencePropertyAccessor.CANON_SMALL_PICTURE_TYPE,
                    IPreferencePropertyAccessor.CANON_SMALL_PICTURE_TYPE_DEFAULT_VALUE
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.VISIONKIDS_HOST_IP)) {
                editor.putString(
                    IPreferencePropertyAccessor.VISIONKIDS_HOST_IP,
                    IPreferencePropertyAccessor.VISIONKIDS_HOST_IP_DEFAULT_VALUE
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.VISIONKIDS_FTP_USER)) {
                editor.putString(
                    IPreferencePropertyAccessor.VISIONKIDS_FTP_USER,
                    IPreferencePropertyAccessor.VISIONKIDS_FTP_USER_DEFAULT_VALUE
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.VISIONKIDS_FTP_PASS)) {
                editor.putString(
                    IPreferencePropertyAccessor.VISIONKIDS_FTP_PASS,
                    IPreferencePropertyAccessor.VISIONKIDS_FTP_PASS_DEFAULT_VALUE
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.VISIONKIDS_LIST_TIMEOUT)) {
                editor.putString(
                    IPreferencePropertyAccessor.VISIONKIDS_LIST_TIMEOUT,
                    IPreferencePropertyAccessor.VISIONKIDS_LIST_TIMEOUT_DEFAULT_VALUE
                )
            }
            editor.apply()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     *
     *
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?)
    {
        Log.v(TAG, "onSharedPreferenceChanged() : $key")
        val value: Boolean
        if (key != null)
        {
            when (key)
            {
                IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA -> {
                    value = preferences?.getBoolean(key, true)?: true
                    Log.v(TAG, " $key , $value")
                }

                IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW -> {
                    value = preferences?.getBoolean(key, true)?: true
                    Log.v(TAG, " $key , $value")
                }

                IPreferencePropertyAccessor.USE_OSC_THETA_V21 -> {
                    value = preferences?.getBoolean(key, false)?: false
                    Log.v(TAG, " $key , $value")
                }

                else -> {
                    val strValue = preferences?.getString(key, "")?: ""
                    setListPreference(key, key, strValue)
                }
            }
        }
    }

    /**
     *
     *
     */
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?)
    {
        Log.v(TAG, "onCreatePreferences()")
        try
        {
            //super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences_visionkids)
            val connectionMethod =
                findPreference<ListPreference>(IPreferencePropertyAccessor.CONNECTION_METHOD)
            if (connectionMethod != null)
            {
                connectionMethod.onPreferenceChangeListener =
                    Preference.OnPreferenceChangeListener { preference, newValue ->
                        preference.summary = "$newValue "
                        true
                    }
                connectionMethod.summary = connectionMethod.value + " "
            }
            setOnPreferenceClickListener(
                IPreferencePropertyAccessor.EXIT_APPLICATION,
                powerOffController
            )
            setOnPreferenceClickListener(IPreferencePropertyAccessor.DEBUG_INFO, logCatViewer)
            setOnPreferenceClickListener(IPreferencePropertyAccessor.WIFI_SETTINGS, this)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun setOnPreferenceClickListener(key: String, listener: Preference.OnPreferenceClickListener?)
    {
        val preference = findPreference<Preference>(key)
        if (preference != null)
        {
            preference.onPreferenceClickListener = listener
        }
    }

    /**
     *
     *
     */
    override fun onResume() {
        super.onResume()
        Log.v(TAG, "onResume() Start")
        try {
            synchronizedProperty()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.v(TAG, "onResume() End")
    }

    /**
     *
     *
     */
    override fun onPause()
    {
        super.onPause()
        Log.v(TAG, "onPause() Start")
        try
        {
            // Preference変更のリスナを解除
            preferences!!.unregisterOnSharedPreferenceChangeListener(this)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        Log.v(TAG, "onPause() End")
    }

    /**
     * ListPreference の表示データを設定
     *
     * @param pref_key     Preference(表示)のキー
     * @param key          Preference(データ)のキー
     * @param defaultValue Preferenceのデフォルト値
     */
    private fun setListPreference(pref_key: String, key: String, defaultValue: String?)
    {
        try
        {
            val pref: ListPreference? = findPreference(pref_key)
            val value = preferences?.getString(key, defaultValue)
            if (pref != null)
            {
                pref.value = value
                pref.summary = value
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * BooleanPreference の表示データを設定
     *
     * @param pref_key     Preference(表示)のキー
     * @param key          Preference(データ)のキー
     * @param defaultValue Preferenceのデフォルト値
     */
    private fun setBooleanPreference(pref_key: String, key: String, defaultValue: Boolean)
    {
        try
        {
            val pref = findPreference<CheckBoxPreference>(pref_key)
            if (pref != null)
            {
                val value = preferences?.getBoolean(key, defaultValue)?: defaultValue
                pref.isChecked = value
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     *
     *
     */
    private fun synchronizedProperty()
    {
        val activity = activity
        val defaultValue = true
        activity?.runOnUiThread {
            try
            {
                // Preferenceの画面に反映させる
                setBooleanPreference(
                    IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA,
                    IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA,
                    defaultValue
                )
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean
    {
        try
        {
            val preferenceKey = preference.key
            Log.v(TAG, " onPreferenceClick : $preferenceKey")
            if (preferenceKey.contains(IPreferencePropertyAccessor.WIFI_SETTINGS))
            {
                // Wifi 設定画面を表示する
                if (context != null)
                {
                    context?.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                }
            }
            return (true)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (false)
    }

    companion object
    {
        private val TAG = VisionKidsPreferenceFragment::class.java.simpleName

        /**
         *
         *
         */
        @JvmStatic fun newInstance(context: AppCompatActivity, changeScene: IChangeScene): VisionKidsPreferenceFragment
        {
            val instance = VisionKidsPreferenceFragment()
            instance.prepare(context, changeScene)

            // パラメータはBundleにまとめておく
            val arguments = Bundle()
            instance.arguments = arguments
            return (instance)
        }
    }
}
