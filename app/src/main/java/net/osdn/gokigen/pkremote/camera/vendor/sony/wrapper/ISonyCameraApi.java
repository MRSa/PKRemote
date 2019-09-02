package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import androidx.annotation.NonNull;

public interface ISonyCameraApi
{
    JSONObject getAvailableApiList();
    JSONObject getApplicationInfo();

    JSONObject getShootMode();
    JSONObject setShootMode(@NonNull String shootMode);
    JSONObject getAvailableShootMode();
    JSONObject getSupportedShootMode();

    JSONObject setTouchAFPosition(double Xpos, double Ypos);
    JSONObject getTouchAFPosition();
    JSONObject cancelTouchAFPosition();

    JSONObject actHalfPressShutter();
    JSONObject cancelHalfPressShutter();

    JSONObject setFocusMode(String focusMode);
    JSONObject getFocusMode();
    JSONObject getSupportedFocusMode();
    JSONObject getAvailableFocusMode();

    JSONObject startLiveview();
    JSONObject stopLiveview();

    JSONObject startRecMode();
    JSONObject actTakePicture();
    JSONObject awaitTakePicture();

    JSONObject startMovieRec();
    JSONObject stopMovieRec();

    JSONObject actZoom(@NonNull String direction, @NonNull String movement);

    JSONObject getEvent(@NonNull String version, boolean longPollingFlag);

    JSONObject setCameraFunction(@NonNull String cameraFunction);

    JSONObject getCameraMethodTypes();

    JSONObject getAvcontentMethodTypes();

    JSONObject getSchemeList();
    JSONObject getSourceList(String scheme);

    JSONObject getContentCountFlatAll(String uri);

    JSONObject getContentList(JSONArray params);

    JSONObject setStreamingContent(String uri);

    JSONObject startStreaming();
    JSONObject stopStreaming();

    List<String> getSonyApiServiceList();
    JSONObject callGenericSonyApiMethod(@NonNull String service, @NonNull String method, @NonNull JSONArray params, @NonNull String version);
}
