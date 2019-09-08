package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.playback;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import net.osdn.gokigen.pkremote.IInformationReceiver;
import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraFileInfo;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IContentInfoCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;
import net.osdn.gokigen.pkremote.camera.playback.ProgressEvent;
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient;
import net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.ISonyCameraApi;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SonyPlaybackControl implements IPlaybackControl {
    private final String TAG = toString();
    private final Activity activity;
    private final IInformationReceiver informationReceiver;
    private ISonyCameraApi cameraApi = null;
    private HashMap<String, SonyImageContentInfo> contentList;
    private int timeoutMs = 55000;
    private boolean contentListIsCreating = false;

    public SonyPlaybackControl(@NonNull Activity activity, @NonNull IInformationReceiver informationReceiver) {
        Log.v(TAG, "SonyPlaybackControl()");
        this.activity = activity;
        this.informationReceiver = informationReceiver;
        contentList = new HashMap<>();

    }

    public void setCameraApi(@NonNull ISonyCameraApi sonyCameraApi) {
        cameraApi = sonyCameraApi;
    }

    @Override
    public String getRawFileSuffix() {
        return "ARW";
    }

    @Override
    public void downloadContentList(IDownloadContentListCallback callback) {
        Log.v(TAG, "downloadContentList()");

    }

    @Override
    public void getContentInfo(String path, String name, IContentInfoCallback callback) {
        Log.v(TAG, "getContentInfo()");
    }

    @Override
    public void updateCameraFileInfo(ICameraFileInfo info) {
        Log.v(TAG, "updateCameraFileInfo()");
    }

    @Override
    public void downloadContentScreennail(String path, IDownloadThumbnailImageCallback callback) {
        //Log.v(TAG, "downloadContentScreennail()" + path);
        try {
            SonyImageContentInfo content = contentList.get(path.substring(path.indexOf('/') + 1));
            if (content == null) {
                Log.v(TAG, " CONTENT IS NULL... : " + path);
                return;
            }
            try {
                String url = content.getSmallUrl();   // Screennail は VGAサイズ
                if (url.length() < 1) {
                    url = content.getThumbnailUrl();  // VGAサイズが取れなかった場合はサムネイルサイズ
                }
                if (url.length() > 1) {
                    Bitmap bmp = SimpleHttpClient.httpGetBitmap(url, timeoutMs);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("Orientation", 0);
                    callback.onCompleted(bmp, map);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                callback.onErrorOccurred(new NullPointerException());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void downloadContentThumbnail(String path, IDownloadThumbnailImageCallback callback) {
        //Log.v(TAG, "downloadContentThumbnail() : " + path);
        try {
            SonyImageContentInfo content = contentList.get(path.substring(path.indexOf('/') + 1));
            if (content == null) {
                Log.v(TAG, " CONTENT IS NULL... : " + path);
                return;
            }
            try {
                String url = content.getThumbnailUrl();
                if (url.length() > 1) {
                    Bitmap bmp = SimpleHttpClient.httpGetBitmap(url, timeoutMs);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("Orientation", 0);
                    callback.onCompleted(bmp, map);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                callback.onErrorOccurred(new NullPointerException());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void downloadContent(String path, boolean isSmallSize, final IDownloadContentCallback callback) {
        //Log.v(TAG, "downloadContent() : " + path);
        try {
            SonyImageContentInfo content = contentList.get(path.substring(path.indexOf('/') + 1));
            if (content == null) {
                Log.v(TAG, " CONTENT IS NULL... : " + path);
                return;
            }
            try {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
                boolean isVgaSize = preferences.getBoolean(IPreferencePropertyAccessor.GET_SMALL_PICTURE_AS_VGA, false);
                String url = (isSmallSize) ? ((isVgaSize) ? content.getSmallUrl() : content.getLargeUrl()) : content.getOriginalUrl();
                if (url.length() < 1) {
                    url = content.getOriginalUrl();
                    if (url.length() < 1) {
                        //  全然だめなら、サムネイルサイズ...
                        url = content.getThumbnailUrl();
                    }
                }
                Log.v(TAG, "downloadContent()  PATH : " + path + "  [SMALL:" + isSmallSize + "][VGA:" + isVgaSize + "]" + " GET URL : " + url);

                SimpleHttpClient.httpGetBytes(url, timeoutMs, new SimpleHttpClient.IReceivedMessageCallback() {
                    @Override
                    public void onCompleted() {
                        callback.onCompleted();
                    }

                    @Override
                    public void onErrorOccurred(Exception e) {
                        callback.onErrorOccurred(e);
                    }

                    @Override
                    public void onReceive(int readBytes, int length, int size, byte[] data) {
                        float percent = (length == 0) ? 0.0f : ((float) readBytes / (float) length);
                        //Log.v(TAG, " onReceive : " + readBytes + " " + length + " " + size);
                        ProgressEvent event = new ProgressEvent(percent, null);
                        callback.onProgress(data, size, event);
                    }
                });
            } catch (Throwable e) {
                e.printStackTrace();
                callback.onErrorOccurred(new NullPointerException());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getCameraContentList(ICameraContentListCallback callback) {
        Log.v(TAG, "getCameraContentList()");
        try {
            if (cameraApi == null) {
                Log.v(TAG, "CAMERA API is NULL.");
                return;
            }
            if (contentListIsCreating) {
                // すでにコンテントリストを作り始めているので、処理は継続しない。
                Log.v(TAG, "ALREADY CREATING CONTENT LIST.");
                return;
            }
            contentListIsCreating = true;
            informationReceiver.updateMessage(activity.getString(R.string.get_image_list), false, false, 0);
            changeContentsTransferMode();  // コンテンツトランスファモードに切り替える

            JSONObject storageInformationObj = cameraApi.getStorageInformation();
            JSONObject schemeListObj = cameraApi.getSchemeList();
            //JSONArray schemeArray = schemeListObj.getJSONArray("result");
            JSONObject sourceObj = cameraApi.getSourceList("storage");
            //JSONArray sourceArray = sourceObj.getJSONArray("result");
            JSONObject countObject = cameraApi.getContentCountFlatAll("storage:memoryCard1");
            JSONArray resultArray = countObject.getJSONArray("result");
            int objectCount = resultArray.getJSONObject(0).getInt("count");
            Log.v(TAG, "  OBJECT COUNT  : " + objectCount);
            if (objectCount < 1) {
                // コンテンツ一覧の取得失敗...
                informationReceiver.updateMessage(activity.getString(R.string.content_is_nothing), true, false, 0);
                contentListIsCreating = false;
                return;
            }
            contentList.clear();

            int index = 0;
            // データを解析してリストを作る
            while ((index >= 0) && (index < objectCount)) {
                informationReceiver.updateMessage(activity.getString(R.string.get_image_list) + " " + index + "/" + objectCount + " ", false, false, 0);

                int remainCount = objectCount - index;
                JSONObject paramsObj = new JSONObject();
                paramsObj.put("uri", "storage:memoryCard1");
                paramsObj.put("stIdx", index);
                paramsObj.put("cnt", (remainCount > 100 ? 100 : remainCount));      // 一括取得数...最大100
                //paramsObj.put("cnt", (remainCount > 50 ? 50 : remainCount)); // 一括取得数
                paramsObj.put("view", "flat");
                paramsObj.put("sort", "descending");
                try {
                    JSONObject responseObject = cameraApi.getContentList(new JSONArray().put(paramsObj));
                    JSONArray resultsArray = responseObject.getJSONArray("result").getJSONArray(0);
                    int nofContents = resultsArray.length();
                    for (int pos = 0; pos < nofContents; pos++) {
                        //  ひろったデータを全部入れていく
                        SonyImageContentInfo contentInfo = new SonyImageContentInfo(resultsArray.getJSONObject(pos));
                        String contentName = contentInfo.getContentName();
                        //Date createdTime = contentInfo.getCapturedDate();
                        //String folderNo = contentInfo.getContentPath();
                        if (contentName.length() > 0) {
                            contentList.put(contentName, contentInfo);
                        }
                        //Log.v(TAG, " [" + pos + "] " + "  " + contentName + " " + " " + createdTime + " " + folderNo);
                    }
                    index = index + nofContents;
                    //Log.v(TAG, "  COUNT : " + index);
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
            contentListIsCreating = false;
            informationReceiver.updateMessage(activity.getString(R.string.get_image_list) + " " + index + "/" + objectCount + " ", false, false, 0);
            if (callback != null) {
                // コレクションを詰めなおして応答する
                callback.onCompleted(new ArrayList<ICameraContent>(contentList.values()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        contentListIsCreating = false;
    }

    private void changeContentsTransferMode() {
        try {
            if (cameraApi == null) {
                return;
            }
            boolean isAvailable = false;
            int maxRetryCount = 10;    // 最大リトライ回数
            while ((!isAvailable) && (maxRetryCount > 0)) {
                isAvailable = setCameraFunction(false);
                maxRetryCount--;
            }
            if (maxRetryCount <= 0) {
                // Retry over
                informationReceiver.updateMessage(activity.getString(R.string.change_transfer_mode_retry_over), true, true, Color.RED);

                // 試しに呼んでみる。
                getContentDirectorySoapAction();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean setCameraFunction(boolean isRecording) {
        try {
            JSONObject reply = cameraApi.setCameraFunction((isRecording) ? "Remote Shooting" : "Contents Transfer");
            try {
                int value = reply.getInt("result");
                Log.v(TAG, "CHANGE RUN MODE : " + value);
                return (true);
            } catch (Exception ee) {
                ee.printStackTrace();
                informationReceiver.updateMessage(activity.getString(R.string.change_transfer_mode_retry), false, false, 0);
                Thread.sleep(500); //  500ms 待つ
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (false);
    }

    private void getContentDirectorySoapAction()
    {

        ////////////  ある程度の数に区切って送られてくる... 何度か繰り返す必要があるようだ  ////////////
        int sequenceNumber = 0;
        int totalCount = 100000;
        int returnedCount = 0;
        while (totalCount > returnedCount)
        {
            Log.v(TAG, "  ===== getContentList() " + sequenceNumber + " =====");
            sequenceNumber++;
            String accessUrl = cameraApi.getDdUrl();
            String url =  accessUrl.substring(0, accessUrl.lastIndexOf("/")) + "/upnp/control/ContentDirectory";

            String postData = "<?xml version=\"1.0\"?><s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
                    "<s:Body><u:Browse xmlns:u=\"urn:schemas-upnp-org:service:ContentDirectory:" + sequenceNumber + "\">" +
                    //"<ObjectID>0</ObjectID>" +
                    "<ObjectID>03_01_0002002552_000002_000000_000000</ObjectID>" +
                    "<BrowseFlag>BrowseDirectChildren</BrowseFlag><Filter>*</Filter>" +
                    "<StartingIndex>" + returnedCount + "</StartingIndex>" +
                    //"<RequestedCount>3500</RequestedCount>" +
                    "<RequestedCount>1</RequestedCount>" +
                    //"<SortCriteria>" + "-dc:flat" +  "</SortCriteria>" +
                    "<SortCriteria>" + "-dc:date" +  "</SortCriteria>" +
                    "</u:Browse></s:Body></s:Envelope>";
/*
            String postData = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"><s:Body>" +
                    "<u:Browse xmlns:u=\"urn:schemas-upnp-org:service:ContentDirectory:" + sequenceNumber + "\" xmlns:pana=\"urn:schemas-panasonic-com:pana\">" +
                    "<ObjectID>0</ObjectID><BrowseFlag>BrowseDirectChildren</BrowseFlag><Filter>*</Filter><StartingIndex>" + returnedCount + "</StartingIndex><RequestedCount>3500</RequestedCount><SortCriteria></SortCriteria>" +
                    "<pana:X_FromCP>LumixLink2.0</pana:X_FromCP></u:Browse></s:Body></s:Envelope>";
*/
            String reply = SimpleHttpClient.httpPostWithHeader(url, postData, "SOAPACTION", "urn:schemas-upnp-org:service:ContentDirectory:" + sequenceNumber + "#Browse", "text/xml; charset=\"utf-8\"", timeoutMs);
            if (reply.length() < 10)
            {
                Log.v(TAG, postData);
                Log.v(TAG, "ContentDirectory is FAILURE. [" + sequenceNumber + "]");
                //break;
            }
            Log.v(TAG, " < REPLY > " + reply);
/*
            getObjectLists = getObjectLists.append(reply);
            String matches = reply.substring(reply.indexOf("<TotalMatches>") + 14, reply.indexOf("</TotalMatches>"));
            try
            {
                totalCount = Integer.parseInt(matches);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                totalCount = 0;
            }

            String returned = reply.substring(reply.indexOf("<NumberReturned>") + 16, reply.indexOf("</NumberReturned>"));
            try
            {
                returnedCount = returnedCount + Integer.parseInt(returned);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            Log.v(TAG, "  REPLY DATA : (" + matches + "/" + totalCount + ") [" + returned + "/" + returnedCount + "] " + " " + reply.length() + "bytes");
            informationReceiver.updateMessage(activity.getString(R.string.get_image_list) + " " + returnedCount + "/" + totalCount + " ", false, false, 0);
*/
        }
    }
}
