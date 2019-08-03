package net.osdn.gokigen.pkremote.camera.vendor.sony.operation;

import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.control.IFocusingControl;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IIndicatorControl;
import net.osdn.gokigen.pkremote.camera.vendor.sony.operation.takepicture.SonyAutoFocusControl;
import net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.ISonyCameraApi;

public class SonyCameraFocusControl  implements IFocusingControl
{
    private final String TAG = toString();
    private final SonyAutoFocusControl afControl;
    private final IAutoFocusFrameDisplay frameDisplay;

    public SonyCameraFocusControl(@NonNull final IAutoFocusFrameDisplay frameDisplayer, @NonNull final IIndicatorControl indicator)
    {
        this.frameDisplay = frameDisplayer;
        afControl = new SonyAutoFocusControl(frameDisplayer, indicator);
    }

    public void setCameraApi(@NonNull ISonyCameraApi sonyCameraApi)
    {
        afControl.setCameraApi(sonyCameraApi);
    }

    @Override
    public boolean driveAutoFocus(final MotionEvent motionEvent)
    {
        Log.v(TAG, "driveAutoFocus()");
        if (motionEvent.getAction() != MotionEvent.ACTION_DOWN)
        {
            return (false);
        }
        try
        {
            PointF point = frameDisplay.getPointWithEvent(motionEvent);
            if (frameDisplay.isContainsPoint(point))
            {
                afControl.lockAutoFocus(point);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (false);
    }

    @Override
    public void unlockAutoFocus()
    {
        Log.v(TAG, "unlockAutoFocus()");
        try
        {
            afControl.unlockAutoFocus();
            frameDisplay.hideFocusFrame();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void halfPressShutter(boolean isPressed)
    {
        Log.v(TAG, "halfPressShutter() " + isPressed);
        try
        {
            afControl.halfPressShutter(isPressed);
            if (!isPressed)
            {
                // フォーカスを外す
                frameDisplay.hideFocusFrame();
                afControl.unlockAutoFocus();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
