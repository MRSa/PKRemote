package net.osdn.gokigen.pkremote.preference;

/**
 *
 *
 *
 */
public interface IPreferencePropertyAccessor
{
    String EXIT_APPLICATION = "exit_application";

    String AUTO_CONNECT_TO_CAMERA = "auto_connect_to_camera";
    String BLE_POWER_ON = "ble_power_on";

    String TAKE_MODE =  "take_mode";
    String TAKE_MODE_DEFAULT_VALUE =  "P";

    String SOUND_VOLUME_LEVEL = "sound_volume_level";
    String SOUND_VOLUME_LEVEL_DEFAULT_VALUE = "OFF";

    String USE_PLAYBACK_MENU = "use_playback_menu";

    String RAW = "raw";

    String LIVE_VIEW_QUALITY = "live_view_quality";
    String LIVE_VIEW_QUALITY_DEFAULT_VALUE = "VGA";

    String SMALL_PICTURE_SIZE = "download_small_picture_size";
    String SMALL_PICTURE_SIZE_DEFAULT_VALUE = "1600";

    String PEN_SMALL_PICTURE_SIZE = "pen_download_small_picture_size";
    String PEN_SMALL_PICTURE_SIZE_DEFAULT_VALUE = "1600";


    String CAMERAKIT_VERSION = "camerakit_version";

    String SHOW_GRID_STATUS = "show_grid";

    String SHARE_AFTER_SAVE = "share_after_save";

    String CONNECTION_METHOD = "connection_method";
    String CONNECTION_METHOD_DEFAULT_VALUE = "RICOH";

    String GR2_DISPLAY_CAMERA_VIEW = "gr2_display_camera_view";


    String USE_GR2_SPECIAL_COMMAND = "use_gr2_special_command";

    String PENTAX_CAPTURE_AFTER_AF = "pentax_capture_after_auto_focus";

    String DIGITAL_ZOOM_LEVEL = "digital_zoom_level";
    String DIGITAL_ZOOM_LEVEL_DEFAULT_VALUE = "1.0";

    String POWER_ZOOM_LEVEL = "power_zoom_level";
    String POWER_ZOOM_LEVEL_DEFAULT_VALUE = "1.0";

    String MAGNIFYING_LIVE_VIEW_SCALE = "magnifying_live_view_scale";
    String MAGNIFYING_LIVE_VIEW_SCALE_DEFAULT_VALUE = "10.0";

    String CAPTURE_BOTH_CAMERA_AND_LIVE_VIEW = "capture_both_camera_and_live_view";

    String OLYCAMERA_BLUETOOTH_SETTINGS = "olympus_air_bt";

    String GR2_DISPLAY_MODE = "gr2_display_mode";
    String GR2_DISPLAY_MODE_DEFAULT_VALUE = "0";

    String GR2_LCD_SLEEP = "gr2_lcd_sleep";
    String GR2_LIVE_VIEW = "gr2_display_camera_view";
    String USE_PENTAX_AUTOFOCUS = "use_pentax_autofocus_mode";

    String FUJIX_DISPLAY_CAMERA_VIEW = "fujix_display_camera_view";

    String FUJIX_FOCUS_XY = "fujix_focus_xy";
    String FUJIX_FOCUS_XY_DEFAULT_VALUE = "7,7";

    String FUJIX_LIVEVIEW_WAIT = "fujix_liveview_wait";
    String FUJIX_LIVEVIEW_WAIT_DEFAULT_VALUE = "80";

    String FUJIX_COMMAND_POLLING_WAIT = "fujix_command_polling_wait";
    String FUJIX_COMMAND_POLLING_WAIT_DEFAULT_VALUE = "500";

    String FUJIX_CONNECTION_FOR_READ = "fujix_connection_for_read";

    String RICOH_GET_PICS_LIST_TIMEOUT = "ricoh_get_pics_list_timeout";
    String RICOH_GET_PICS_LIST_TIMEOUT_DEFAULT_VALUE = "10";

    String RICOH_GET_PICS_LIST_MAX_COUNT = "ricoh_get_pics_list_max_count";
    String RICOH_GET_PICS_LIST_MAX_COUNT_DEFAULT_VALUE = "3000";

    String GET_SMALL_PICTURE_AS_VGA = "get_small_picture_as_vga";

    String USE_SMARTPHONE_TRANSFER_MODE = "use_smartphone_transfer_mode";

    String CANON_RAW_SUFFIX = "canon_raw_suffix";
    String CANON_RAW_SUFFIX_DEFAULT_VALUE = "CR2";

    String CANON_USE_SCREENNAIL_AS_SMALL = "canon_get_screennail_as_small_picture";

    String OLYMPUS_USE_SCREENNAIL_AS_SMALL = "olympus_get_screennail_as_small_picture";

    /*
    //String GR2_DISPLAY_MODE = "gr2_display_mode";
    //String GR2_DISPLAY_MODE_DEFAULT_VALUE = "0";


    int CHOICE_SPLASH_SCREEN = 10;

    int SELECT_SAMPLE_IMAGE_CODE = 110;
    int SELECT_SPLASH_IMAGE_CODE = 120;

    String getLiveViewSize();
    void restoreCameraSettings(Callback callback);
    void storeCameraSettings(Callback callback);

    interface Callback
    {
        void stored(boolean result);
        void restored(boolean result);
    }
*/

}
