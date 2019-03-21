package net.osdn.gokigen.pkremote.scene;

import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver;

/**
 *
 */
public interface IChangeScene
{
    void changeSceneToCameraPropertyList();
    void changeSceneToConfiguration();
    void changeCameraConnection();
    void changeSceneToDebugInformation();
    void changeSceneToCalendar();
    void changeSceneToApiList();
    void changeScenceDateSelected(String filterLabel);
    void changeScenceToImageList();
    void exitApplication();
    void reloadRemoteImageContents();
    void setAnotherStatusReceiver(ICameraStatusReceiver statusReceiver);
    void updateBottomNavigationMenu();
}
