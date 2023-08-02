package net.osdn.gokigen.pkremote.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class PreferencePropertyInitializer(context: Context)
{
    private var preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    /**
     * Preferenceの初期化...
     *
     */
     fun initializePreferences()
     {
        try
        {
            val items = preferences.all
            val editor = preferences.edit()
            if (!items.containsKey(IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA)) {
                editor?.putBoolean(IPreferencePropertyAccessor.AUTO_CONNECT_TO_CAMERA, true)
            }
            if (!items.containsKey(IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW)) {
                editor?.putBoolean(
                    IPreferencePropertyAccessor.CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW,
                    true
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.CONNECTION_METHOD)) {
                editor?.putString(
                    IPreferencePropertyAccessor.CONNECTION_METHOD,
                    IPreferencePropertyAccessor.CONNECTION_METHOD_DEFAULT_VALUE
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.GET_SMALL_PICTURE_AS_VGA)) {
                editor?.putBoolean(IPreferencePropertyAccessor.GET_SMALL_PICTURE_AS_VGA, false)
            }
            if (!items.containsKey(IPreferencePropertyAccessor.USE_SMARTPHONE_TRANSFER_MODE)) {
                editor?.putBoolean(IPreferencePropertyAccessor.USE_SMARTPHONE_TRANSFER_MODE, false)
            }
            if (!items.containsKey(IPreferencePropertyAccessor.RICOH_GET_PICS_LIST_TIMEOUT)) {
                editor?.putString(
                    IPreferencePropertyAccessor.RICOH_GET_PICS_LIST_TIMEOUT,
                    IPreferencePropertyAccessor.RICOH_GET_PICS_LIST_TIMEOUT_DEFAULT_VALUE
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.USE_OSC_THETA_V21)) {
                editor?.putBoolean(IPreferencePropertyAccessor.USE_OSC_THETA_V21, false)
            }
            if (!items.containsKey(IPreferencePropertyAccessor.PIXPRO_HOST_IP)) {
                editor?.putString(
                    IPreferencePropertyAccessor.PIXPRO_HOST_IP,
                    IPreferencePropertyAccessor.PIXPRO_HOST_IP_DEFAULT_VALUE
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.PIXPRO_COMMAND_PORT)) {
                editor?.putString(
                    IPreferencePropertyAccessor.PIXPRO_COMMAND_PORT,
                    IPreferencePropertyAccessor.PIXPRO_COMMAND_PORT_DEFAULT_VALUE
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.PIXPRO_GET_PICS_LIST_TIMEOUT)) {
                editor?.putString(
                    IPreferencePropertyAccessor.PIXPRO_GET_PICS_LIST_TIMEOUT,
                    IPreferencePropertyAccessor.PIXPRO_GET_PICS_LIST_TIMEOUT_DEFAULT_VALUE
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.THUMBNAIL_IMAGE_CACHE_SIZE)) {
                editor?.putString(
                    IPreferencePropertyAccessor.THUMBNAIL_IMAGE_CACHE_SIZE,
                    IPreferencePropertyAccessor.THUMBNAIL_IMAGE_CACHE_SIZE_DEFAULT_VALUE
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.CANON_HOST_IP)) {
                editor?.putString(
                    IPreferencePropertyAccessor.CANON_HOST_IP,
                    IPreferencePropertyAccessor.CANON_HOST_IP_DEFAULT_VALUE
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.CANON_AUTO_DETECT_HOST_IP)) {
                editor?.putBoolean(IPreferencePropertyAccessor.CANON_AUTO_DETECT_HOST_IP, true)
            }
            if (!items.containsKey(IPreferencePropertyAccessor.CANON_CONNECTION_SEQUENCE)) {
                editor?.putString(
                    IPreferencePropertyAccessor.CANON_CONNECTION_SEQUENCE,
                    IPreferencePropertyAccessor.CANON_CONNECTION_SEQUENCE_DEFAULT_VALUE
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.CANON_SMALL_PICTURE_TYPE)) {
                editor?.putString(
                    IPreferencePropertyAccessor.CANON_SMALL_PICTURE_TYPE,
                    IPreferencePropertyAccessor.CANON_SMALL_PICTURE_TYPE_DEFAULT_VALUE
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.VISIONKIDS_HOST_IP)) {
                editor?.putString(
                    IPreferencePropertyAccessor.VISIONKIDS_HOST_IP,
                    IPreferencePropertyAccessor.VISIONKIDS_HOST_IP_DEFAULT_VALUE
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.VISIONKIDS_FTP_USER)) {
                editor?.putString(
                    IPreferencePropertyAccessor.VISIONKIDS_FTP_USER,
                    IPreferencePropertyAccessor.VISIONKIDS_FTP_USER_DEFAULT_VALUE
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.VISIONKIDS_FTP_PASS)) {
                editor?.putString(
                    IPreferencePropertyAccessor.VISIONKIDS_FTP_PASS,
                    IPreferencePropertyAccessor.VISIONKIDS_FTP_PASS_DEFAULT_VALUE
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.VISIONKIDS_LIST_TIMEOUT)) {
                editor?.putString(
                    IPreferencePropertyAccessor.VISIONKIDS_LIST_TIMEOUT,
                    IPreferencePropertyAccessor.VISIONKIDS_LIST_TIMEOUT_DEFAULT_VALUE
                )
            }
            if (!items.containsKey(IPreferencePropertyAccessor.VISIONKIDS_AUTO_SET_HOST_IP)) {
                editor?.putBoolean(IPreferencePropertyAccessor.VISIONKIDS_AUTO_SET_HOST_IP, true)
            }
            editor?.apply()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }
}
