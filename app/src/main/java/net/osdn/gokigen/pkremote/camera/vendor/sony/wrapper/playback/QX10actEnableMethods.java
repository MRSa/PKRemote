package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.playback;

import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.ISonyCameraApi;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.MessageDigest;

/**
 *   QX10actEnableMethods : QX10を Contents Transferモードに切り替えるための処理...
 *      (処理はこれでよさそうだが...このキーはファームアップ後に変わっていそう...)
 *
 *    [参考サイト]
 *      - https://github.com/erik-smit/sony-camera-api/blob/master/actEnableMethods.sh
 *      - http://qaru.site/questions/4735618/qx1-manual-shoot-mode-m-via-api
 *
 *
 *
 */
class QX10actEnableMethods
{
    private final String TAG = toString();
    private final ISonyCameraApi cameraApi;
    QX10actEnableMethods(@NonNull ISonyCameraApi cameraApi)
    {
        this.cameraApi = cameraApi;
    }

    private String getDigest()
    {
        String digest = "";
        try
        {
            JSONObject reply = cameraApi.actEnableMethods("", "", "", "");
            JSONArray resultArray = reply.getJSONArray("result");
            digest = resultArray.getJSONObject(0).getString("dg");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (digest);
    }

    private String decideSignature()
    {
        try
        {
            String keyDigest = "90adc8515a40558968fe8318b5b023fdd48d3828a2dda8905f3b93a3cd8e58dc" + getDigest();  // たぶんこのキーは違う...
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(keyDigest.getBytes("UTF-8"));
            String signature = Base64.encodeToString(messageDigest.digest(), Base64.NO_WRAP);
            Log.v(TAG, "  signature : " + signature + "  keyDg (" + keyDigest + ") ");
            return (signature);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ("");
    }

    boolean actEnableMethods()
    {
        boolean response = false;
        try
        {
            // ここは現物合わせ...
            String methods =
                    "camera\\/setFlashMode:camera\\/getFlashMode:camera\\/getSupportedFlashMode:camera\\/getAvailableFlashMode:" +
                    "camera\\/setExposureCompensation:camera\\/getExposureCompensation:camera\\/getSupportedExposureCompensation:camera\\/getAvailableExposureCompensation:" +
                    "camera\\/setSteadyMode:camera\\/getSteadyMode:camera\\/getSupportedSteadyMode:camera\\/getAvailableSteadyMode:" +
                    "camera\\/setViewAngle:camera\\/getViewAngle:camera\\/getSupportedViewAngle:camera\\/getAvailableViewAngle:" +
                    "camera\\/setMovieQuality:camera\\/getMovieQuality:camera\\/getSupportedMovieQuality:camera\\/getAvailableMovieQuality:" +
                    "camera\\/setFocusMode:camera\\/getFocusMode:camera\\/getSupportedFocusMode:camera\\/getAvailableFocusMode:" +
                    "camera\\/setStillSize:camera\\/getStillSize:camera\\/getSupportedStillSize:camera\\/getAvailableStillSize:" +
                    "camera\\/setBeepMode:camera\\/getBeepMode:camera\\/getSupportedBeepMode:camera\\/getAvailableBeepMode:" +
                    "camera\\/setCameraFunction:camera\\/getCameraFunction:camera\\/getSupportedCameraFunction:camera\\/getAvailableCameraFunction:" +
                    "camera\\/setLiveviewSize:camera\\/getLiveviewSize:camera\\/getSupportedLiveviewSize:camera\\/getAvailableLiveviewSize:" +
                    "camera\\/setTouchAFPosition:camera\\/getTouchAFPosition:camera\\/cancelTouchAFPosition:" +
                    "camera\\/setFNumber:camera\\/getFNumber:camera\\/getSupportedFNumber:camera\\/getAvailableFNumber:" +
                    "camera\\/setShutterSpeed:camera\\/getShutterSpeed:camera\\/getSupportedShutterSpeed:camera\\/getAvailableShutterSpeed:" +
                    "camera\\/setIsoSpeedRate:camera\\/getIsoSpeedRate:camera\\/getSupportedIsoSpeedRate:camera\\/getAvailableIsoSpeedRate:" +
                    "camera\\/setExposureMode:camera\\/getExposureMode:camera\\/getSupportedExposureMode:camera\\/getAvailableExposureMode:" +
                    "camera\\/setWhiteBalance:camera\\/getWhiteBalance:camera\\/getSupportedWhiteBalance:camera\\/getAvailableWhiteBalance:" +
                    "camera\\/setProgramShift:camera\\/getSupportedProgramShift:" + "camera\\/getStorageInformation:" + "camera\\/startLiveviewWithSize:" +
                    "camera\\/startIntervalStillRec:camera\\/stopIntervalStillRec:" + "camera\\/actFormatStorage:" + "system\\/setCurrentTime:" +
                    "contentSync\\/actPairing:contentSync\\/notifySyncStatus:" + "system\\/setAccessPointInfo:system\\/getAccessPointInfo";
            String developerName = "Sony Corporation";
            String developerID = "7DED695E-75AC-4ea9-8A85-E5F8CA0AF2F3";
            String signature = decideSignature();

            JSONObject reply = cameraApi.actEnableMethods(developerName, developerID, signature, methods);
            JSONArray resultArray = reply.getJSONArray("result");
            String digest = resultArray.getJSONObject(0).getString("dg");
            String error = resultArray.getJSONObject(0).getString("error");
            response = ((digest.length() == 0)&&(error.length() == 0));
            Log.v(TAG, "actEnableMethods() : " + response + " " + digest + " [" + signature + "] " + error);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (response);
    }
}
