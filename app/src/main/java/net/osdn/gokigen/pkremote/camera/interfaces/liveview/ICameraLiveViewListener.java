package net.osdn.gokigen.pkremote.camera.interfaces.liveview;

import java.util.Map;

public interface ICameraLiveViewListener
{
    void onUpdateLiveView(byte[] data, Map<String, Object> metadata);
}
