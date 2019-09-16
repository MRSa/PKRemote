package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.status;

import android.util.Log;
import android.util.SparseIntArray;

import androidx.annotation.NonNull;
import androidx.collection.SparseArrayCompat;

import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ICameraStatusUpdateNotify;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class PtpIpStatusHolder implements IPtpIpCameraProperties
{
    private final String TAG = toString();
    private SparseIntArray statusHolder;
    private SparseArrayCompat<String> statusNameArray;

    PtpIpStatusHolder()
    {
        statusHolder = new SparseIntArray();
        statusHolder.clear();

        statusNameArray = new SparseArrayCompat<>();
        prepareStatusNameArray();
    }

    private void prepareStatusNameArray()
    {
        statusNameArray.clear();
        statusNameArray.append(BATTERY_LEVEL, BATTERY_LEVEL_STR);
        statusNameArray.append(WHITE_BALANCE, WHITE_BALANCE_STR);
        statusNameArray.append(APERTURE, APERTURE_STR);
        statusNameArray.append(FOCUS_MODE, FOCUS_MODE_STR);
        statusNameArray.append(SHOOTING_MODE, SHOOTING_MODE_STR);
        statusNameArray.append(FLASH, FLASH_STR);
        statusNameArray.append(EXPOSURE_COMPENSATION, EXPOSURE_COMPENSATION_STR);
        statusNameArray.append(SELF_TIMER, SELF_TIMER_STR);
        statusNameArray.append(FILM_SIMULATION, FILM_SIMULATION_STR);
        statusNameArray.append(IMAGE_FORMAT, IMAGE_FORMAT_STR);
        statusNameArray.append(RECMODE_ENABLE, RECMODE_ENABLE_STR);
        statusNameArray.append(F_SS_CONTROL, F_SS_CONTROL_STR);
        statusNameArray.append(ISO, ISO_STR);
        statusNameArray.append(MOVIE_ISO, MOVIE_ISO_STR);
        statusNameArray.append(FOCUS_POINT, FOCUS_POINT_STR);
        statusNameArray.append(DEVICE_ERROR, DEVICE_ERROR_STR);
        statusNameArray.append(IMAGE_FILE_COUNT, IMAGE_FILE_COUNT_STR);
        statusNameArray.append(SDCARD_REMAIN_SIZE, SDCARD_REMAIN_SIZE_STR);
        statusNameArray.append(FOCUS_LOCK, FOCUS_LOCK_STR);
        statusNameArray.append(MOVIE_REMAINING_TIME, MOVIE_REMAINING_TIME_STR);
        statusNameArray.append(SHUTTER_SPEED, SHUTTER_SPEED_STR);
        statusNameArray.append(IMAGE_ASPECT,IMAGE_ASPECT_STR);
        statusNameArray.append(BATTERY_LEVEL_2, BATTERY_LEVEL_2_STR);

        statusNameArray.append(UNKNOWN_DF00, UNKNOWN_DF00_STR);
        statusNameArray.append(PICTURE_JPEG_COUNT, PICTURE_JPEG_COUNT_STR);
        statusNameArray.append(UNKNOWN_D400, UNKNOWN_D400_STR);
        statusNameArray.append(UNKNOWN_D401, UNKNOWN_D401_STR);
        statusNameArray.append(UNKNOWN_D52F, UNKNOWN_D52F_STR);

    }


    void updateValue(ICameraStatusUpdateNotify notifier, int id, byte data0, byte data1, byte data2, byte data3)
    {
        try
        {
            int value = ((((int) data3) & 0xff) << 24) + ((((int) data2) & 0xff) << 16) + ((((int) data1) & 0xff) << 8) + (((int) data0) & 0xff);
            int currentValue = statusHolder.get(id, -1);
            Log.v(TAG, "STATUS  ID: " + id + "  value : " + value + " (" + currentValue + ")");
            statusHolder.put(id, value);
            if (currentValue != value)
            {
                //Log.v(TAG, "STATUS  ID: " + id + " value : " + currentValue + " -> " + value);
                if (notifier != null)
                {
                    updateDetected(notifier, id, currentValue, value);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void updateDetected(@NonNull ICameraStatusUpdateNotify notifier, int id, int previous, int current)
    {
        try
        {
            String idName = statusNameArray.get(id, "Unknown");
            Log.v(TAG, String.format(Locale.US,"<< UPDATE STATUS >> id: 0x%04x[%s] 0x%08x(%d) -> 0x%08x(%d)", id, idName, previous, previous, current, current));
            //Log.v(TAG, "updateDetected(ID: " + id + " [" + idName + "] " + previous + " -> " + current + " )");

            if (id == FOCUS_LOCK)
            {
                if (current == 1)
                {
                    // focus Lock
                    notifier.updateFocusedStatus(true, true);
                }
                else
                {
                    // focus unlock
                    notifier.updateFocusedStatus(false, false);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *   認識したカメラのステータス名称のリストを応答する
     *
     */
    private List<String> getAvailableStatusNameList()
    {
        ArrayList<String> selection = new ArrayList<>();
        try
        {
            for (int index = 0; index < statusHolder.size(); index++)
            {
                int key = statusHolder.keyAt(index);
                selection.add(statusNameArray.get(key, String.format(Locale.US, "0x%04x", key)));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (selection);

    }

    List<String> getAvailableItemList(String listKey)
    {
        if (listKey == null)
        {
            // アイテム名の一覧を応答する
            return (getAvailableStatusNameList());
        }

        /////  選択可能なステータスの一覧を取得する : でも以下はアイテム名の一覧... /////
        ArrayList<String> selection = new ArrayList<>();
        try
        {
            for (int index = 0; index < statusHolder.size(); index++)
            {
                int key = statusHolder.keyAt(index);
                selection.add(statusNameArray.get(key));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (selection);
    }

    String getItemStatus(String key)
    {
        try
        {
            int strIndex = key.indexOf("x");
            Log.v(TAG, "getItemStatus() : " + key + " [" + strIndex + "]");
            if (strIndex >= 1)
            {
                key = key.substring(strIndex + 1);
                try
                {
                    int id = Integer.parseInt(key, 16);
                    int value = statusHolder.get(id);
                    Log.v(TAG, "getItemStatus() value : " + value);
                    return (value + "");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            for (int index = 0; index < statusNameArray.size(); index++)
            {
                int id = statusNameArray.keyAt(index);
                String strKey = statusNameArray.valueAt(index);
                if (key.contentEquals(strKey))
                {
                    int value = statusHolder.get(id);
                    return (value + "");
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ("? [" + key + "]");
    }
}
