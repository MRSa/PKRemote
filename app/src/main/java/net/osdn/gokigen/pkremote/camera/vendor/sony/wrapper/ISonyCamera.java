package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper;

import java.util.List;

public interface ISonyCamera
{
    boolean hasApiService(String serviceName);
    List<ISonyApiService> getApiServices();

    String getFriendlyName();
    String getModelName();
}
