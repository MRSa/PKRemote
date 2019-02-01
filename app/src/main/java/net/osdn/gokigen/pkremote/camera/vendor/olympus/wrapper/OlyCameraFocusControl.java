package net.osdn.gokigen.pkremote.camera.vendor.olympus.wrapper;

import android.graphics.PointF;
import android.view.MotionEvent;

import net.osdn.gokigen.pkremote.camera.interfaces.control.IFocusingControl;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IIndicatorControl;
import net.osdn.gokigen.pkremote.camera.vendor.olympus.operation.takepicture.OlympusAutoFocusControl;

/**
 *
 *
 */
public class OlyCameraFocusControl implements IFocusingControl
{
    private final OlympusAutoFocusControl afControl;
    private final IAutoFocusFrameDisplay frameDisplay;

    OlyCameraFocusControl(OlyCameraWrapper wrapper, IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator)
    {
        this.frameDisplay = frameDisplayer;
        afControl = new OlympusAutoFocusControl(wrapper.getOLYCamera(), frameDisplayer, indicator);
    }

    @Override
    public boolean driveAutoFocus(final MotionEvent motionEvent)
    {
        if (motionEvent.getAction() != MotionEvent.ACTION_DOWN)
        {
            return (false);
        }

        if (frameDisplay != null)
        {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    PointF point = frameDisplay.getPointWithEvent(motionEvent);
                    if (frameDisplay.isContainsPoint(point))
                    {
                        afControl.lockAutoFocus(point);
                    }
                }
            });
            try
            {
                thread.start();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return (false);
    }

    @Override
    public void unlockAutoFocus()
    {
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

}
