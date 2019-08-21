package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.eventlistener;

import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraChangeListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReplyJsonParser implements ICameraStatusHolder
{
    private static final String TAG = ReplyJsonParser.class.getSimpleName();
    private String cameraStatus = null;
    private final Handler uiHandler;
    private ICameraChangeListener listener;

    private boolean currentLiveviewStatus;
    private String currentShootMode;
    private List<String> currentAvailableShootModes = Collections.unmodifiableList(new ArrayList<String>());
    private int currentZoomPosition;
    private String currentStorageId;
    private String currentFocusStatus;

    ReplyJsonParser(final @NonNull Handler uiHandler)
    {
        this.uiHandler = uiHandler;
    }

    void parse(@NonNull JSONObject replyJson)
    {
        // AvailableApis
        List<String> availableApis = findAvailableApiList(replyJson);
        if (!availableApis.isEmpty()) {
            fireApiListModifiedListener(availableApis);
        }

        // CameraStatus
        String cameraStatus = findCameraStatus(replyJson);
        Log.d(TAG, "getEvent cameraStatus: " + cameraStatus);
        if (cameraStatus != null && !cameraStatus.equals(this.cameraStatus)) {
            this.cameraStatus = cameraStatus;
            fireCameraStatusChangeListener(cameraStatus);
        }

        // LiveviewStatus
        Boolean liveviewStatus = findLiveviewStatus(replyJson);
        Log.d(TAG, "getEvent liveviewStatus: " + liveviewStatus);
        if (liveviewStatus != null && !liveviewStatus.equals(currentLiveviewStatus)) {
            currentLiveviewStatus = liveviewStatus;
            fireLiveviewStatusChangeListener(liveviewStatus);
        }

        // ShootMode
        String shootMode = findShootMode(replyJson);
        Log.d(TAG, "getEvent shootMode: " + shootMode);
        if (shootMode != null && !shootMode.equals(currentShootMode)) {
            currentShootMode = shootMode;

            // Available Shoot Modes
            List<String> shootModes = findAvailableShootModes(replyJson);
            currentAvailableShootModes = Collections.unmodifiableList(shootModes);
            fireShootModeChangeListener(shootMode);
        }

        // zoomPosition
        int zoomPosition = findZoomInformation(replyJson);
        Log.d(TAG, "getEvent zoomPosition: " + zoomPosition);
        if (zoomPosition != -1) {
            currentZoomPosition = zoomPosition;
            fireZoomInformationChangeListener(0, 0, zoomPosition, 0);
        }

        // storageId
        String storageId = findStorageId(replyJson);
        Log.d(TAG, "getEvent storageId:" + storageId);
        if (storageId != null && !storageId.equals(currentStorageId)) {
            currentStorageId = storageId;
            fireStorageIdChangeListener(storageId);
        }

        // focusStatus (v1.1)
        String focusStatus = findFocusStatus(replyJson);
        Log.d(TAG, "getEvent focusStatus:" + focusStatus);
        if (focusStatus != null && !focusStatus.equals(currentFocusStatus)) {
            currentFocusStatus = focusStatus;
            fireFocusStatusChangeListener(focusStatus);
        }
    }

    void setEventChangeListener(ICameraChangeListener listener)
    {
        this.listener = listener;
    }

    void clearEventChangeListener()
    {
        listener = null;
    }

    void fireResponseErrorListener()
    {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (listener != null) {
                        listener.onResponseError();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void fireApiListModifiedListener(final List<String> availableApis)
    {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (listener != null) {
                        listener.onApiListModified(availableApis);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void fireCameraStatusChangeListener(final String status) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (listener != null) {
                        listener.onCameraStatusChanged(status);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void fireLiveviewStatusChangeListener(final boolean status) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onLiveviewStatusChanged(status);
                }
            }
        });
    }

    private void fireShootModeChangeListener(final String shootMode) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onShootModeChanged(shootMode);
                }
            }
        });
    }

    private void fireZoomInformationChangeListener(final int zoomIndexCurrentBox, final int zoomNumberBox, final int zoomPosition, final int zoomPositionCurrentBox) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onZoomPositionChanged(zoomPosition);
                }
            }
        });
    }

    private void fireStorageIdChangeListener(final String storageId) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onStorageIdChanged(storageId);
                }
            }
        });
    }

    private void fireFocusStatusChangeListener(final String focusStatus) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (listener != null) {
                    listener.onFocusStatusChanged(focusStatus);
                }
            }
        });
    }

    private static List<String> findAvailableApiList(JSONObject replyJson) {
        List<String> availableApis = new ArrayList<>();
        int indexOfAvailableApiList = 0;
        try {

            JSONArray resultsObj = replyJson.getJSONArray("result");
            if (!resultsObj.isNull(indexOfAvailableApiList)) {
                JSONObject availableApiListObj = resultsObj.getJSONObject(indexOfAvailableApiList);
                String type = availableApiListObj.getString("type");
                if ("availableApiList".equals(type)) {
                    JSONArray apiArray = availableApiListObj.getJSONArray("names");
                    for (int i = 0; i < apiArray.length(); i++) {
                        availableApis.add(apiArray.getString(i));
                    }
                } else {
                    Log.w(TAG, "Event reply: Illegal Index (0: AvailableApiList) " + type);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (availableApis);
    }

    private static String findCameraStatus(JSONObject replyJson)
    {
        String cameraStatus = null;
        int indexOfCameraStatus = 1;
        try {
            JSONArray resultsObj = replyJson.getJSONArray("result");
            if (!resultsObj.isNull(indexOfCameraStatus)) {
                JSONObject cameraStatusObj = resultsObj.getJSONObject(indexOfCameraStatus);
                String type = cameraStatusObj.getString("type");
                if ("cameraStatus".equals(type)) {
                    cameraStatus = cameraStatusObj.getString("cameraStatus");
                } else {
                    Log.w(TAG, "Event reply: Illegal Index (1: CameraStatus) " + type);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (cameraStatus);
    }

    private static Boolean findLiveviewStatus(JSONObject replyJson)
    {
        Boolean liveviewStatus = null;
        try {
            int indexOfLiveviewStatus = 3;
            JSONArray resultsObj = replyJson.getJSONArray("result");
            if (!resultsObj.isNull(indexOfLiveviewStatus)) {
                JSONObject liveviewStatusObj = resultsObj.getJSONObject(indexOfLiveviewStatus);
                String type = liveviewStatusObj.getString("type");
                if ("liveviewStatus".equals(type)) {
                    liveviewStatus = liveviewStatusObj.getBoolean("liveviewStatus");
                } else {
                    Log.w(TAG, "Event reply: Illegal Index (3: LiveviewStatus) " + type);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (liveviewStatus);
    }


    private static String findShootMode(JSONObject replyJson)
    {
        String shootMode = null;
        try {
            int indexOfShootMode = 21;
            JSONArray resultsObj = replyJson.getJSONArray("result");
            if (!resultsObj.isNull(indexOfShootMode)) {
                JSONObject shootModeObj = resultsObj.getJSONObject(indexOfShootMode);
                String type = shootModeObj.getString("type");
                if ("shootMode".equals(type)) {
                    shootMode = shootModeObj.getString("currentShootMode");
                } else {
                    Log.w(TAG, "Event reply: Illegal Index (21: ShootMode) " + type);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (shootMode);
    }

    private static List<String> findAvailableShootModes(JSONObject replyJson)
    {
        List<String> shootModes = new ArrayList<>();
        try {
            int indexOfShootMode = 21;
            JSONArray resultsObj = replyJson.getJSONArray("result");
            if (!resultsObj.isNull(indexOfShootMode)) {
                JSONObject shootModesObj = resultsObj.getJSONObject(indexOfShootMode);
                String type = shootModesObj.getString("type");
                if ("shootMode".equals(type)) {
                    JSONArray shootModesArray = shootModesObj.getJSONArray("shootModeCandidates");
                    if (shootModesArray != null) {
                        for (int i = 0; i < shootModesArray.length(); i++) {
                            shootModes.add(shootModesArray.getString(i));
                        }
                    }
                } else {
                    Log.w(TAG, "Event reply: Illegal Index (21: ShootMode) " + type);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (shootModes);
    }

    private static int findZoomInformation(JSONObject replyJson)
    {
        int zoomPosition = -1;
        try {
            int indexOfZoomInformation = 2;
            JSONArray resultsObj = replyJson.getJSONArray("result");
            if (!resultsObj.isNull(indexOfZoomInformation)) {
                JSONObject zoomInformationObj = resultsObj.getJSONObject(indexOfZoomInformation);
                String type = zoomInformationObj.getString("type");
                if ("zoomInformation".equals(type)) {
                    zoomPosition = zoomInformationObj.getInt("zoomPosition");
                } else {
                    Log.w(TAG, "Event reply: Illegal Index (2: zoomInformation) " + type);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (zoomPosition);
    }

    private static String findStorageId(JSONObject replyJson)
    {
        String storageId = null;
        try {
            int indexOfStorageInfomation = 10;
            JSONArray resultsObj = replyJson.getJSONArray("result");
            if (!resultsObj.isNull(indexOfStorageInfomation)) {
                JSONArray storageInformationArray = resultsObj.getJSONArray(indexOfStorageInfomation);
                if (!storageInformationArray.isNull(0)) {
                    JSONObject storageInformationObj = storageInformationArray.getJSONObject(0);
                    String type = storageInformationObj.getString("type");
                    if ("storageInformation".equals(type)) {
                        storageId = storageInformationObj.getString("storageID");
                    } else {
                        Log.w(TAG, "Event reply: Illegal Index (11: storageInformation) " + type);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (storageId);
    }

    private static String findFocusStatus(JSONObject replyJson)
    {
        String focusStatus = null;
        try {
            int indexOfFocusStatus= 35;
            JSONArray resultsObj = replyJson.getJSONArray("result");
            if (!resultsObj.isNull(indexOfFocusStatus)) {
                JSONObject focustatusObj = resultsObj.getJSONObject(indexOfFocusStatus);
                String type = focustatusObj.getString("type");
                if ("focusStatus".equals(type)) {
                    focusStatus = focustatusObj.getString("focusStatus");
                } else {
                    Log.w(TAG, "Event reply: Illegal Index (21: ShootMode) " + type);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (focusStatus);
    }

    @Override
    public String getCameraStatus()
    {
        return (cameraStatus);
    }

    @Override
    public boolean getLiveviewStatus()
    {
        return (currentLiveviewStatus);
    }

    @Override
    public String getShootMode()
    {
        return (currentShootMode);
    }

    @Override
    public List<String> getAvailableShootModes()
    {
        return (currentAvailableShootModes);
    }

    @Override
    public int getZoomPosition()
    {
        return (currentZoomPosition);
    }

    @Override
    public String getStorageId()
    {
        return (currentStorageId);
    }

}
