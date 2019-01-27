package net.osdn.gokigen.pkremote.camera.interfaces.liveview;
/**
 *
 *
 */
interface ILiveImageStatusNotify
{
    void toggleFocusAssist();
    void toggleShowGridFrame();
    void takePicture();
    IMessageDrawer getMessageDrawer();
}
