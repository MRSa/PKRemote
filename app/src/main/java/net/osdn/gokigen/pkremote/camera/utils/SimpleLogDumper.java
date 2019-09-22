package net.osdn.gokigen.pkremote.camera.utils;

import android.util.Log;

public class SimpleLogDumper
{
    private static final String TAG = SimpleLogDumper.class.getSimpleName();

    /**
     *   デバッグ用：ログにバイト列を出力する
     *
     */
    public static void dump_bytes(String header, byte[] data)
    {
        if (data == null)
        {
            Log.v(TAG, "DATA IS NULL");
            return;
        }
        if (data.length > 8192)
        {
            Log.v(TAG, " --- DUMP DATA IS TOO LONG... " + data.length + " bytes.");
            return;
        }

        int index = 0;
        StringBuffer message;
        message = new StringBuffer();
        for (byte item : data)
        {
            index++;
            message.append(String.format("%02x ", item));
            if (index >= 16)
            {
                Log.v(TAG, header + " " + message);
                index = 0;
                message = new StringBuffer();
            }
        }
        if (index != 0)
        {
            Log.v(TAG, header + " " + message);
        }
        System.gc();
    }

}
