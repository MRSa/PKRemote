package net.osdn.gokigen.pkremote.scene;

/**
 *
 */
public interface IChangeScene
{
    void changeSceneToCameraPropertyList();
    void changeSceneToConfiguration();
    void changeCameraConnection();
    void changeSceneToDebugInformation();
    void changeSceneToApiList();
    //void changeScenceToImageList();
    void exitApplication();
}
