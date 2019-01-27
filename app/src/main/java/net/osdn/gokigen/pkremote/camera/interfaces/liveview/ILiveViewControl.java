package net.osdn.gokigen.pkremote.camera.interfaces.liveview;

/**
 *
 *
 */
public interface ILiveViewControl
{
    void changeLiveViewSize(String size);

    void startLiveView(boolean isCameraScreen);
    void stopLiveView();
    void updateDigitalZoom();
    void updateMagnifyingLiveViewScale(boolean isChangeScale);
    float getMagnifyingLiveViewScale();
    float getDigitalZoomScale();
}
