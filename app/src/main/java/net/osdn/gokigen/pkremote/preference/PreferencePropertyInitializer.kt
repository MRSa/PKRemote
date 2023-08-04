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
            if (!items.containsKey(IPreferencePropertyAccessor.NIKON_AUTO_DETECT_HOST_IP)) {
                editor?.putBoolean(IPreferencePropertyAccessor.NIKON_AUTO_DETECT_HOST_IP, true)
            }
            if (!items.containsKey(IPreferencePropertyAccessor.CANON_RAW_SUFFIX))
            {
                editor.putString(IPreferencePropertyAccessor.CANON_RAW_SUFFIX, IPreferencePropertyAccessor.CANON_RAW_SUFFIX_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.CANON_RECEIVE_WAIT))
            {
                editor.putString(IPreferencePropertyAccessor.CANON_RECEIVE_WAIT, IPreferencePropertyAccessor.CANON_RECEIVE_WAIT_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.CANON_USE_SCREENNAIL_AS_SMALL))
            {
                editor.putBoolean(IPreferencePropertyAccessor.CANON_USE_SCREENNAIL_AS_SMALL, false);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.FUJIX_DISPLAY_CAMERA_VIEW)) {
                editor.putBoolean(IPreferencePropertyAccessor.FUJIX_DISPLAY_CAMERA_VIEW, false);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.FUJIX_FOCUS_XY)) {
                editor.putString(IPreferencePropertyAccessor.FUJIX_FOCUS_XY, IPreferencePropertyAccessor.FUJIX_FOCUS_XY_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.FUJIX_LIVEVIEW_WAIT)) {
                editor.putString(IPreferencePropertyAccessor.FUJIX_LIVEVIEW_WAIT, IPreferencePropertyAccessor.FUJIX_LIVEVIEW_WAIT_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.FUJIX_COMMAND_POLLING_WAIT)) {
                editor.putString(IPreferencePropertyAccessor.FUJIX_COMMAND_POLLING_WAIT, IPreferencePropertyAccessor.FUJIX_COMMAND_POLLING_WAIT_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.FUJIX_CONNECTION_FOR_READ)) {
                editor.putBoolean(IPreferencePropertyAccessor.FUJIX_CONNECTION_FOR_READ, false);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.NIKON_CAMERA_IP_ADDRESS)) {
                editor.putString(IPreferencePropertyAccessor.NIKON_CAMERA_IP_ADDRESS, IPreferencePropertyAccessor.NIKON_CAMERA_IP_ADDRESS_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.NIKON_RECEIVE_WAIT)) {
                editor.putString(IPreferencePropertyAccessor.NIKON_RECEIVE_WAIT, IPreferencePropertyAccessor.NIKON_RECEIVE_WAIT_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.NIKON_USE_SCREENNAIL_AS_SMALL)) {
                editor.putBoolean(IPreferencePropertyAccessor.NIKON_USE_SCREENNAIL_AS_SMALL, false);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.BLE_WIFI_ON)) {
                editor.putBoolean(IPreferencePropertyAccessor.BLE_WIFI_ON, false);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.LIVE_VIEW_QUALITY))
            {
                editor.putString(IPreferencePropertyAccessor.LIVE_VIEW_QUALITY, IPreferencePropertyAccessor.LIVE_VIEW_QUALITY_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.SOUND_VOLUME_LEVEL))
            {
                editor.putString(IPreferencePropertyAccessor.SOUND_VOLUME_LEVEL, IPreferencePropertyAccessor.SOUND_VOLUME_LEVEL_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.RAW))
            {
                editor.putBoolean(IPreferencePropertyAccessor.RAW, true);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.SHARE_AFTER_SAVE)) {
                editor.putBoolean(IPreferencePropertyAccessor.SHARE_AFTER_SAVE, false);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.USE_PLAYBACK_MENU)) {
                editor.putBoolean(IPreferencePropertyAccessor.USE_PLAYBACK_MENU, true);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.GR2_DISPLAY_CAMERA_VIEW)) {
                editor.putBoolean(IPreferencePropertyAccessor.GR2_DISPLAY_CAMERA_VIEW, true);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.GR2_LCD_SLEEP)) {
                editor.putBoolean(IPreferencePropertyAccessor.GR2_LCD_SLEEP, false);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.USE_GR2_SPECIAL_COMMAND)) {
                editor.putBoolean(IPreferencePropertyAccessor.USE_GR2_SPECIAL_COMMAND, true);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.PENTAX_CAPTURE_AFTER_AF)) {
                editor.putBoolean(IPreferencePropertyAccessor.PENTAX_CAPTURE_AFTER_AF, false);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.SMALL_PICTURE_SIZE)) {
                editor.putString(IPreferencePropertyAccessor.SMALL_PICTURE_SIZE, IPreferencePropertyAccessor.SMALL_PICTURE_SIZE_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.PEN_SMALL_PICTURE_SIZE)) {
                editor.putString(IPreferencePropertyAccessor.PEN_SMALL_PICTURE_SIZE, IPreferencePropertyAccessor.PEN_SMALL_PICTURE_SIZE_DEFAULT_VALUE);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.OLYMPUS_USE_SCREENNAIL_AS_SMALL)) {
                editor.putBoolean(IPreferencePropertyAccessor.OLYMPUS_USE_SCREENNAIL_AS_SMALL, false);
            }
            if (!items.containsKey(IPreferencePropertyAccessor.USE_PLAYBACK_MENU)) {
                editor.putBoolean(IPreferencePropertyAccessor.USE_PLAYBACK_MENU, true)
            }
            editor?.apply()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }
}
