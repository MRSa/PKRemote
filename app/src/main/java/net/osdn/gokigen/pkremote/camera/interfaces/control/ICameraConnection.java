package net.osdn.gokigen.pkremote.camera.interfaces.control;

import android.content.Context;

/**
 *   カメラの接続/切断
 *
 */
public interface ICameraConnection
{
    enum CameraConnectionMethod
    {
        UNKNOWN,
        OPC,
        SONY,
        RICOH,
        FUJI_X,
        PANASONIC,
        CANON,
        NIKON,
        OLYMPUS,
        THETA,
        PIXPRO,
        VISIONKIDS,
    }

    enum CameraConnectionStatus
    {
        UNKNOWN,
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    /**  WIFI 接続系  **/
    void startWatchWifiStatus(Context context);
    void stopWatchWifiStatus(Context context);

    /** カメラ接続系 **/
    void disconnect(final boolean powerOff);
    void connect();

    /** カメラ接続失敗 **/
    void alertConnectingFailed(String message);

    /** 接続状態 **/
    CameraConnectionStatus getConnectionStatus();
    void forceUpdateConnectionStatus(CameraConnectionStatus status);

}
