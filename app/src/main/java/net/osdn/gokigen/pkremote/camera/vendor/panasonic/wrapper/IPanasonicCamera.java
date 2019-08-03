package net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper;

import java.util.List;

public interface IPanasonicCamera
{
    boolean hasApiService(String serviceName);
    List<IPanasonicApiService> getApiServices();

    String getFriendlyName();
    String getModelName();
    String getddUrl();
    String getCmdUrl();
    String getObjUrl();
    String getPictureUrl();

    String getClientDeviceUuId();
}
