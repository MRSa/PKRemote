package net.osdn.gokigen.pkremote.camera.vendor.ptpip.operation;


import net.osdn.gokigen.pkremote.camera.interfaces.control.IZoomLensControl;

public class PtpIpZoomControl implements IZoomLensControl
{

    @Override
    public boolean canZoom()
    {
        return (false);
    }

    @Override
    public void updateStatus()
    {

    }

    @Override
    public float getMaximumFocalLength()
    {
        return (0);
    }

    @Override
    public float getMinimumFocalLength()
    {
        return (0);
    }

    @Override
    public float getCurrentFocalLength()
    {
        return (0);
    }

    @Override
    public void driveZoomLens(float targetLength)
    {

    }

    @Override
    public void driveZoomLens(boolean isZoomIn)
    {

    }

    @Override
    public void moveInitialZoomPosition()
    {

    }

    @Override
    public boolean isDrivingZoomLens()
    {
        return (false);
    }
}
