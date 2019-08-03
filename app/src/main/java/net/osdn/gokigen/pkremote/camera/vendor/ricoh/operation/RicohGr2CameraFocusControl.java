package net.osdn.gokigen.pkremote.camera.vendor.ricoh.operation;

import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;

import net.osdn.gokigen.pkremote.camera.interfaces.control.IFocusingControl;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IIndicatorControl;
import net.osdn.gokigen.pkremote.camera.vendor.ricoh.operation.takepicture.RicohGr2AutoFocusControl;

import androidx.annotation.NonNull;


/**
 *
 *
 */
public class RicohGr2CameraFocusControl implements IFocusingControl
{
    private final String TAG = toString();
    private final RicohGr2AutoFocusControl afControl;
    private final IAutoFocusFrameDisplay frameDisplay;

    /**
     *
     *
     */
    public RicohGr2CameraFocusControl(boolean useGrCommand, @NonNull final IAutoFocusFrameDisplay frameDisplayer, @NonNull final IIndicatorControl indicator)
    {
        this.frameDisplay = frameDisplayer;
        this.afControl = new RicohGr2AutoFocusControl(useGrCommand, frameDisplayer, indicator);
    }

    /**
     *
     *
     */
    @Override
    public boolean driveAutoFocus(MotionEvent motionEvent)
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

    /**
     *
     *
     */
    @Override
    public void unlockAutoFocus()
    {
        afControl.unlockAutoFocus();
    }

    @Override
    public void halfPressShutter(boolean isPressed)
    {

    }
}
