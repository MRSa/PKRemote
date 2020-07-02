package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.eventlistener;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.ICardSlotSelectionReceiver;
import net.osdn.gokigen.pkremote.ICardSlotSelector;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraChangeListener;
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient;
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.IPanasonicCamera;

import java.util.List;

public class CameraStatusHolder implements ICameraStatusHolder, ICardSlotSelectionReceiver
{
    private static final String TAG = CameraStatusHolder.class.getSimpleName();
    private final Context context;
    private final IPanasonicCamera remote;
    private static final int TIMEOUT_MS = 3000;
    private final ICardSlotSelector cardSlotSelector;
    private ICameraChangeListener listener = null;
    private String current_sd = "sd1";
    private boolean isInitialized = false;
    private boolean isDualSlot = false;

    CameraStatusHolder(@NonNull Context context, @NonNull IPanasonicCamera apiClient, @NonNull ICardSlotSelector cardSlotSelector)
    {
        this.context = context;
        this.remote = apiClient;
        this.cardSlotSelector = cardSlotSelector;

    }

    void parse(String reply)
    {
        try
        {
            // Log.v(TAG, " getState : " + reply);

            boolean isEnableDualSlot = false;
            if (reply.contains("<sd_memory>set</sd_memory>") && (reply.contains("<sd2_memory>set</sd2_memory>")))
            {
                // カードが2枚刺さっている場合...
                isEnableDualSlot = true;
            }
            if ((!isInitialized)||(isDualSlot != isEnableDualSlot))
            {
                // 初回だけの実行...
                if (isEnableDualSlot)
                {
                    // カードが2枚刺さっている場合...
                    cardSlotSelector.setupSlotSelector(true, this);
                }
                else
                {
                    // カードが１つしか刺さっていない場合...
                    cardSlotSelector.setupSlotSelector(false, null);
                }
                isInitialized = true;
                isDualSlot = isEnableDualSlot;
            }
            checkCurrentSlot(reply);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void checkCurrentSlot(String reply)
    {
        try
        {
            String header = "<current_sd>";
            int indexStart = reply.indexOf(header);
            int indexEnd = reply.indexOf("</current_sd>");
            if ((indexStart > 0)&&(indexEnd > 0)&&(indexStart < indexEnd))
            {
                String currentSlot = reply.substring(indexStart + header.length(), indexEnd);
                if (!current_sd.equals(currentSlot))
                {
                    current_sd = currentSlot;
                    cardSlotSelector.changedCardSlot(current_sd);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    void setEventChangeListener(@NonNull ICameraChangeListener listener)
    {
        this.listener = listener;
    }

    void clearEventChangeListener()
    {
        this.listener = null;
    }

    @Override
    public String getCameraStatus()
    {
        return (null);
    }

    @Override
    public boolean getLiveviewStatus()
    {
        return (false);
    }

    @Override
    public String getShootMode()
    {
        return (null);
    }

    @Override
    public List<String> getAvailableShootModes()
    {
        return (null);
    }

    @Override
    public int getZoomPosition()
    {
        return (0);
    }

    @Override
    public String getStorageId()
    {
        return (current_sd);
    }

    @Override
    public void slotSelected(@NonNull String slotId)
    {
        Log.v(TAG, " slotSelected : " + slotId);
        if (!current_sd.equals(slotId))
        {
            // スロットを変更したい！
            requestToChangeSlot(slotId);
        }
    }


    private void requestToChangeSlot(final String slotId)
    {
        try
        {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run()
                {
                    try
                    {
                        boolean loop = true;
                        while (loop)
                        {
                            String reply = SimpleHttpClient.httpGet(remote.getCmdUrl() + "cam.cgi?mode=setsetting&type=current_sd&value=" + slotId, TIMEOUT_MS);
                            if (reply.indexOf("<result>ok</result>") > 0)
                            {
                                loop = false;
                                cardSlotSelector.selectSlot(slotId);
                            }
                            else
                            {
                                Thread.sleep(1000);  // 1秒待つ
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
