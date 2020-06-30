package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.eventlistener;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.ICardSlotSelectionReceiver;
import net.osdn.gokigen.pkremote.ICardSlotSelector;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraChangeListener;

import java.util.List;

public class CameraStatusHolder implements ICameraStatusHolder, ICardSlotSelectionReceiver
{
    private static final String TAG = CameraStatusHolder.class.getSimpleName();
    private final Context context;
    private final ICardSlotSelector cardSlotSelector;
    private ICameraChangeListener listener = null;
    private boolean isInitialized = false;
    private boolean isDualSlot = false;

    CameraStatusHolder(@NonNull Context context, @NonNull ICardSlotSelector cardSlotSelector)
    {
        this.context = context;
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
        return (null);
    }

    @Override
    public void slotSelected(String slotId)
    {
        Log.v(TAG, " slotSelected : " + slotId);
    }
}
