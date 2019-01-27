package net.osdn.gokigen.pkremote.camera.interfaces.liveview;

import net.osdn.gokigen.pkremote.camera.interfaces.control.IFocusingModeNotify;

/**
 *
 *
 */
public interface IDisplayInjector
{
    void injectDisplay(IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator, IFocusingModeNotify focusingModeNotify);
}
