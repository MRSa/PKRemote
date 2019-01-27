package net.osdn.gokigen.pkremote.camera.interfaces.liveview;

import java.util.Map;

public interface IImageDataReceiver
{
    void setImageData(byte[] data, Map<String, Object> metadata);
}
