package net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.status;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ICameraStatusUpdateNotify;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatus;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusWatcher;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandCallback;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.IPtpIpCommandPublisher;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.command.messages.specific.StatusRequestMessage;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;

import java.util.ArrayList;
import java.util.List;

public class PtpIpStatusChecker implements IPtpIpCommandCallback, ICameraStatusWatcher, ICameraStatus
{
    private final String TAG = toString();
    private static final int STATUS_MESSAGE_HEADER_SIZE = 14;
    private int sleepMs;
    private final IPtpIpCommandPublisher issuer;
    private ICameraStatusUpdateNotify notifier = null;
    private PtpIpStatusHolder statusHolder;
    private boolean whileFetching = false;
    private boolean logcat = false;


    public PtpIpStatusChecker(@NonNull Activity activity, @NonNull IPtpIpCommandPublisher issuer)
    {
        this.issuer = issuer;
        this.statusHolder = new PtpIpStatusHolder();
        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
            String pollingWait = preferences.getString(IPreferencePropertyAccessor.FUJIX_COMMAND_POLLING_WAIT, IPreferencePropertyAccessor.FUJIX_COMMAND_POLLING_WAIT_DEFAULT_VALUE);
            this.sleepMs = Integer.parseInt(pollingWait);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            this.sleepMs = 400;
        }
        Log.v(TAG, "POLLING WAIT : " + sleepMs);
    }

    @Override
    public void onReceiveProgress(int currentBytes, int totalBytes, byte[] body)
    {
        Log.v(TAG, " " + currentBytes + "/" + totalBytes);
    }

    @Override
    public boolean isReceiveMulti()
    {
        return (false);
    }

    @Override
    public void receivedMessage(int id, byte[] data)
    {
        try
        {
            logcat("receivedMessage : " + id + ", length: " + data.length);
            if (data.length < STATUS_MESSAGE_HEADER_SIZE)
            {
                Log.v(TAG, "received status length is short. (" + data.length + " bytes.)");
                return;
            }

            int nofStatus = (data[13] * 256) + data[12];
            int statusCount = 0;
            int index = STATUS_MESSAGE_HEADER_SIZE;
            while ((statusCount < nofStatus)&&(index < data.length))
            {
                int dataId = ((((int)data[index + 1]) & 0xff) * 256) + (((int) data[index]) & 0xff);
                statusHolder.updateValue(notifier, dataId, data[index + 2], data[index + 3], data[index +4], data[index + 5]);
                index = index + 6;
                statusCount++;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getStatusList(String key)
    {
        try
        {
            if (statusHolder == null)
            {
                return (new ArrayList<>());
            }
            return (statusHolder.getAvailableItemList(key));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (new ArrayList<>());
    }

    @Override
    public String getStatus(String key)
    {
        try
        {
            if (statusHolder == null)
            {
                return ("");
            }
            return (statusHolder.getItemStatus(key));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ("");
    }

    @Override
    public void setStatus(String key, String value)
    {
        try
        {
            if (logcat)
            {
                Log.v(TAG, "setStatus(" + key + ", " + value + ")");
            }

            // ここで設定を行う。
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void startStatusWatch(@NonNull ICameraStatusUpdateNotify notifier)
    {
        if (whileFetching)
        {
            Log.v(TAG, "startStatusWatch() already starting.");
            return;
        }
        try
        {
            final IPtpIpCommandCallback callback = this;
            this.notifier = notifier;
            whileFetching = true;
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    logcat("Start status watch. : " + sleepMs + "ms");
                    while (whileFetching)
                    {
                        try
                        {
                            issuer.enqueueCommand(new StatusRequestMessage(callback));
                            Thread.sleep(sleepMs);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    logcat("STATUS WATCH STOPPED.");
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void stopStatusWatch()
    {
        Log.v(TAG, "stoptStatusWatch()");
        whileFetching = false;
        this.notifier = null;
    }

    private void logcat(String message)
    {
        if (logcat)
        {
            Log.v(TAG, message);
        }
    }
}
