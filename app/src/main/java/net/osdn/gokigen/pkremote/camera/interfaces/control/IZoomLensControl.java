package net.osdn.gokigen.pkremote.camera.interfaces.control;

/**
 *  ズームレンズの状態
 *
 */

public interface IZoomLensControl
{
    boolean canZoom();
    void updateStatus();
    float getMaximumFocalLength();
    float getMinimumFocalLength();
    float getCurrentFocalLength();
    void driveZoomLens(float targetLength);
    void driveZoomLens(boolean isZoomIn);
    void moveInitialZoomPosition();
    boolean isDrivingZoomLens();

}
