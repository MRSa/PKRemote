package net.osdn.gokigen.pkremote.camera.interfaces.control;

import android.view.MotionEvent;

/**
 *
 *
 */
public interface IFocusingControl
{
    boolean driveAutoFocus(MotionEvent motionEvent);
    void unlockAutoFocus();
}
