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
import java.util.List;
import java.util.Map;

public class SonyPlaybackControl implements IPlaybackControl
{
    private final String TAG = toString();
    private final Activity activity;
    private final IInformationReceiver informationReceiver;
    private ISonyCameraApi cameraApi = null;
    private HashMap<String, ISonyImageContentInfo> contentList;
    private int timeoutMs = 55000;
    private boolean contentListIsCreating = false;

    public SonyPlaybackControl(@NonNull Activity activity, @NonNull IInformationReceiver informationReceiver)
    {
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
    public void downloadContentList(IDownloadContentListCallback callback)
    {
        Log.v(TAG, "downloadContentList()");

    }

    @Override
    public void getContentInfo(String path, String name, IContentInfoCallback callback)
    {
        Log.v(TAG, "getContentInfo()");
    }

    @Override
    public void updateCameraFileInfo(ICameraFileInfo info) {
        Log.v(TAG, "updateCameraFileInfo()");
    }

    @Override
    public void downloadContentScreennail(String path, IDownloadThumbnailImageCallback callback)
    {
        //Log.v(TAG, "downloadContentScreennail()" + path);
        try
        {
            ISonyImageContentInfo content = contentList.get(path.substring(path.indexOf('/') + 1));
            if (content == null)
            {
                Log.v(TAG, " CONTENT IS NULL... : " + path);
                return;
            }
            try
            {
                String url = content.getSmallUrl();   // Screennail は VGAサイズ
                if (url.length() < 1)
                {
                    url = content.getThumbnailUrl();  // VGAサイズが取れなかった場合はサムネイルサイズ
                }
                if (url.length() > 1)
                {
                    Bitmap bmp = SimpleHttpClient.httpGetBitmap(url, null, timeoutMs);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("Orientation", 0);
                    callback.onCompleted(bmp, map);
                }
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                callback.onErrorOccurred(new NullPointerException());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void downloadContentThumbnail(String path, IDownloadThumbnailImageCallback callback)
    {
        //Log.v(TAG, "downloadContentThumbnail() : " + path);
        try
        {
            ISonyImageContentInfo content = contentList.get(path.substring(path.indexOf('/') + 1));
            if (content == null)
            {
                Log.v(TAG, " CONTENT IS NULL... : " + path);
                return;
            }
            try
            {
                String url = content.getThumbnailUrl();
                if (url.length() > 1)
                {
                    Bitmap bmp = SimpleHttpClient.httpGetBitmap(url, null, timeoutMs);
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("Orientation", 0);
                    callback.onCompleted(bmp, map);
                }
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                callback.onErrorOccurred(new NullPointerException());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void downloadContent(String path, boolean isSmallSize, final IDownloadContentCallback callback)
    {
        //Log.v(TAG, "downloadContent() : " + path);
        try
        {
            ISonyImageContentInfo content = contentList.get(path.substring(path.indexOf('/') + 1));
            if (content == null)
            {
                Log.v(TAG, " CONTENT IS NULL... : " + path);
                return;
            }
            try
            {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
                boolean isVgaSize = preferences.getBoolean(IPreferencePropertyAccessor.GET_SMALL_PICTURE_AS_VGA, false);
                String url = (isSmallSize) ? ((isVgaSize) ? content.getSmallUrl() : content.getLargeUrl()) : content.getOriginalUrl();
                if (url.length() < 1)
                {
                    url = content.getOriginalUrl();
                    if (url.length() < 1)
                    {
                        //  全然だめなら、サムネイルサイズ...
                        url = content.getThumbnailUrl();
                    }
                }
                Log.v(TAG, "downloadContent()  PATH : " + path + "  [SMALL:" + isSmallSize + "][VGA:" + isVgaSize + "]" + " GET URL : " + url);

                SimpleHttpClient.httpGetBytes(url, null, timeoutMs, new SimpleHttpClient.IReceivedMessageCallback()
                {
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
            }
            catch (Throwable e)
            {
                e.printStackTrace();
                callback.onErrorOccurred(new NullPointerException());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void getCameraContentList(ICameraContentListCallback callback)
    {
        Log.v(TAG, " getCameraContentList()");
        try
        {
            if (cameraApi == null)
            {
                Log.v(TAG, " CAMERA API is NULL.");
                return;
            }
            if (contentListIsCreating)
            {
                // すでにコンテントリストを作り始めているので、処理は継続しない。
                Log.v(TAG, " ALREADY CREATING CONTENT LIST.");
                return;
            }
            contentListIsCreating = true;

            // 画像転送に「スマートフォン転送機能」を使う場合...
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
            boolean useSmartphoneTransfer = preferences.getBoolean(IPreferencePropertyAccessor.USE_SMARTPHONE_TRANSFER_MODE, false);
            if (useSmartphoneTransfer)
            {
                // DLNAを使用したコンテンツ特定モード(「スマートフォン転送機能」)を使う
                try
                {
                    getContentDirectorySoapAction();
                }
                catch (Exception ee)
                {
                    ee.printStackTrace();
                }
                contentListIsCreating = false;

                // 解析終了を報告する
                informationReceiver.updateMessage(activity.getString(R.string.get_image_list) + " " +  contentList.size() + "/" + contentList.size() + " ", false, false, 0);
                if (callback != null)
                {
                    // コレクションを詰めなおして応答する
                    callback.onCompleted(new ArrayList<ICameraContent>(contentList.values()));
                }
                return;
            }

            Log.v(TAG, "  >>>>>>>>>> START RECEIVE SEQUENCE...");

            /////  繰り返しイベント発行する...
            checkCameraFunctionResult(5, 150);

            // メディア(SDカード等)が入っているかどうか、先に呼べ、ということらしい。
            JSONObject storageInformationObj = cameraApi.getStorageInformation();

            // 画像転送モードに切り替える
            informationReceiver.updateMessage(activity.getString(R.string.get_image_list), false, false, 0);
            boolean ret = changeContentsTransferMode();  // コンテンツトランスファモードに切り替える
            if (!ret)
            {
                informationReceiver.updateMessage(activity.getString(R.string.change_transfer_mode_failure), true, true, Color.RED);
                contentListIsCreating = false;
                return;
            }

            /////  繰り返しイベント発行する...
            checkCameraFunctionResult(20, 100);

            // ここでも呼んでみる
            JSONObject storageInformation2Obj = cameraApi.getStorageInformation();

            /////  ここの処理が弱い... ちゃんと解析が必要。
            JSONObject schemeListObj = cameraApi.getSchemeList();
            //JSONArray schemeArray = schemeListObj.getJSONArray("result");
            JSONObject sourceObj = cameraApi.getSourceList("storage");
            //JSONArray sourceArray = sourceObj.getJSONArray("result");
            JSONObject countObject = cameraApi.getContentCountFlatAll("storage:memoryCard1");
            JSONArray resultArray = countObject.getJSONArray("result");
            int objectCount = resultArray.getJSONObject(0).getInt("count");
            Log.v(TAG, "  OBJECT COUNT  : " + objectCount);
            if (objectCount < 1)
            {
                // コンテンツ一覧の取得失敗...
                informationReceiver.updateMessage(activity.getString(R.string.content_is_nothing), true, false, 0);
                contentListIsCreating = false;
                return;
            }
            contentList.clear();

            int index = 0;
            // データを解析してリストを作る
            while ((index >= 0) && (index < objectCount))
            {
                informationReceiver.updateMessage(activity.getString(R.string.get_image_list) + " " + index + "/" + objectCount + " ", false, false, 0);

                int remainCount = objectCount - index;
                JSONObject paramsObj = new JSONObject();
                paramsObj.put("uri", "storage:memoryCard1");
                paramsObj.put("stIdx", index);
                paramsObj.put("cnt", (remainCount > 100 ? 100 : remainCount));      // 一括取得数...最大100
                //paramsObj.put("cnt", (remainCount > 50 ? 50 : remainCount)); // 一括取得数
                paramsObj.put("view", "flat");
                paramsObj.put("sort", "descending");
                try
                {
                    JSONObject responseObject = cameraApi.getContentList(new JSONArray().put(paramsObj));
                    JSONArray resultsArray = responseObject.getJSONArray("result").getJSONArray(0);
                    int nofContents = resultsArray.length();
                    for (int pos = 0; pos < nofContents; pos++)
                    {
                        //  ひろったデータを全部入れていく
                        JSONObject contentObject = resultsArray.getJSONObject(pos);
                        int listCount = 1;
                        try
                        {
                            JSONObject contents = contentObject.getJSONObject("content");
                            JSONArray originalArray = contents.getJSONArray("original");
                            listCount = originalArray.length();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                        for (int content = 0; content < listCount; content++)
                        {
                            SonyImageContentInfoJson contentInfo = new SonyImageContentInfoJson(contentObject, content);
                            String contentName = contentInfo.getContentName();
                            //Date createdTime = contentInfo.getCapturedDate();
                            //String folderNo = contentInfo.getContentPath();
                            if (contentName.length() > 0)
                            {
                                contentList.put(contentName, contentInfo);
                            }
                        }
                        //Log.v(TAG, " [" + pos + "] " + "  " + contentName + " " + " " + createdTime + " " + folderNo);
                    }
                    index = index + nofContents;
                    //Log.v(TAG, "  COUNT : " + index);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    break;
                }
            }
            contentListIsCreating = false;
            informationReceiver.updateMessage(activity.getString(R.string.get_image_list) + " " + index + "/" + objectCount + " ", false, false, 0);
            if (callback != null)
            {
                // コレクションを詰めなおして応答する
                callback.onCompleted(new ArrayList<ICameraContent>(contentList.values()));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        contentListIsCreating = false;
    }

    private void sleep(int delayMs)
    {
        try
        {
            Thread.sleep(delayMs);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void checkCameraFunctionResult(int retryCount, int waitMs)
    {
        try
        {
            for (int count = 0; count < retryCount; count++)
            {
                try
                {
                    JSONObject eventListObj = cameraApi.getEvent("1.0", false);
                    JSONArray resultArray = eventListObj.getJSONArray("result");
                    {
                        JSONObject cameraFunctionResult = resultArray.getJSONObject(15);
                        if (cameraFunctionResult != null)
                        {
                            String result = cameraFunctionResult.getString("cameraFunctionResult");
                            if (result.contains("Success"))
                            {
                                //  モードが変わったことを認識した！
                                Log.v(TAG, " ----- cameraFunctionResult is Success.");
                                return;
                            }
                        }
                    }
                    sleep(waitMs);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public void showPictureStarted()
    {

    }

    @Override
    public void showPictureFinished()
    {

    }

    private boolean changeContentsTransferMode()
    {
        try
        {
            if (cameraApi == null)
            {
                return (false);
            }

            boolean isAvailable = false;
            int maxRetryCount = 10;    // 最大リトライ回数
            while ((!isAvailable) && (maxRetryCount > 0))
            {
                isAvailable = setCameraFunction(false);
                maxRetryCount--;
            }
            if (maxRetryCount <= 0)
            {
                // Retry over
                informationReceiver.updateMessage(activity.getString(R.string.change_transfer_mode_retry_over), true, true, Color.RED);

                // QX10のコマンドを有効化する。
                QX10actEnableMethods actEnableMethods = new QX10actEnableMethods(cameraApi);
                boolean ret = actEnableMethods.actEnableMethods();
                if (!ret)
                {
                    // actEnableMethods がうまく動かなかった場合... ここで処理を止める

                    // カメラのモードチェンジ
                   setCameraFunction(false);

                    getContentDirectorySoapAction();   //  ← やっても動かないはず
                    return (false);
                }

                // カメラのモードチェンジ
                setCameraFunction(false);

                //  DLNAで画像取得に入る...。
                informationReceiver.updateMessage(activity.getString(R.string.image_checking), false, false, Color.BLACK);
                getContentDirectorySoapAction();
                return (false);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (true);
    }

    private boolean setCameraFunction(boolean isRecording)
    {
        try
        {
            JSONObject reply = cameraApi.setCameraFunction((isRecording) ? "Remote Shooting" : "Contents Transfer");
            try
            {
                int value = reply.getJSONArray("result").getInt(0);
                Log.v(TAG, "CHANGE RUN MODE : " + value);
                return (true);
            }
            catch (Exception ee)
            {
                ee.printStackTrace();
                informationReceiver.updateMessage(activity.getString(R.string.change_transfer_mode_retry), false, false, 0);
                Thread.sleep(500); //  500ms 待つ
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (false);
    }

    /**
     *   スマートフォン転送（DLNAを使用したコンテンツ一覧取得）時の一覧取得処理
     *
     */
    private void getContentDirectorySoapAction()
    {
        try
        {
            String accessUrl = cameraApi.getDdUrl();
            accessUrl = accessUrl.substring(0, accessUrl.lastIndexOf("/"));

            //String reply = getSortCapabilities(accessUrl);
            String reply = browseRootDirectory(accessUrl);
            ContentDirectoryInfo directoryInfo = parseObjectId(parseResult(reply, true));

            // PhotoRoot Directory
            int returnedCount = 0;
            int totalCount = directoryInfo.getCount();
            List<ContentDirectoryInfo> dateFolderInfoList = new ArrayList<>();
            while (returnedCount < totalCount)
            {
                reply = browsePhotoSubRootDirectory(accessUrl, directoryInfo.getObjectId(), returnedCount);
                List<ContentDirectoryInfo> objectInfoList = parseObjectIds(parseResult(reply, true));
                returnedCount = objectInfoList.size();
                dateFolderInfoList.addAll(objectInfoList);
            }

            /////////////////  Date Directories  /////////////////
            int totalObjectCount = 0;
            List<ContentDirectoryInfo> folderInfoList = new ArrayList<>();
            for (ContentDirectoryInfo  rootObjectInfo : dateFolderInfoList)
            {
                int returnedFolderCount = 0;
                reply = browsePhotoSubRootDirectory(accessUrl, rootObjectInfo.getObjectId(), returnedFolderCount);
                List<ContentDirectoryInfo> folderList = parseObjectIds(parseResult(reply, true));
                folderInfoList.addAll(folderList);
                for (ContentDirectoryInfo  folderInfo : folderList)
                {
                    totalObjectCount = totalObjectCount + folderInfo.getCount();
                }
            }

            ///////////////// GET CONTENTS /////////////////
            contentList.clear();
            int objectCount = 0;
            informationReceiver.updateMessage(activity.getString(R.string.get_image_list) + " " + contentList.size() + "/" + totalObjectCount + " ", false, false, 0);
            Log.v(TAG, " TOTAL OBJECT COUNT : " + contentList.size() + "/" + totalObjectCount);
            for (ContentDirectoryInfo  dateFolderInfo : folderInfoList)
            {
                objectCount = objectCount + getObjects(accessUrl, dateFolderInfo, totalObjectCount);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private int getObjects(String accessUrl, ContentDirectoryInfo  dateFolderInfo, int totalObjectCount)
    {
        int currentCount = 0;
        while (currentCount < dateFolderInfo.getCount())
        {
            String reply = browsePhotoSubRootDirectory(accessUrl, dateFolderInfo.getObjectId(), currentCount);
            currentCount = currentCount + parseContentObject(parseResult(reply, false));

            informationReceiver.updateMessage(activity.getString(R.string.get_image_list) + " " + contentList.size() + "/" + totalObjectCount + " ", false, false, 0);
            Log.v(TAG, " TOTAL OBJECT COUNT : " + contentList.size() + "/" + totalObjectCount);
        }
        return (currentCount);
    }

    private String getSortCapabilities(String accessUrl)
    {
        String url =   accessUrl + "/upnp/control/ContentDirectory";
        String postData = "<?xml version=\"1.0\"?>" +
                "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
                "<s:Body>" +
                "<u:GetSortCapabilities xmlns:u=\"urn:schemas-upnp-org:service:ContentDirectory:1\">" +
                "</u:GetSortCapabilities>" +
                "</s:Body>" +
                "</s:Envelope>\r\n\r\n";
        Map<String, String> header = new HashMap<>();
        header.clear();
        header.put("SOAPACTION", "\"urn:schemas-upnp-org:service:ContentDirectory:1" + "#GetSortCapabilities\"");
        return (SimpleHttpClient.httpPostWithHeader(url, postData, header, "text/xml; charset=\"utf-8\"", timeoutMs));
    }

    private String browseRootDirectory(String accessUrl)
    {
        String url =   accessUrl + "/upnp/control/ContentDirectory";
        String postData = "<?xml version=\"1.0\"?>" +
                "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">" +
                "<s:Body>" +
                "<u:Browse xmlns:u=\"urn:schemas-upnp-org:service:ContentDirectory:1\">" +
                "<ObjectID>0</ObjectID>" +
                "<BrowseFlag>BrowseDirectChildren</BrowseFlag>" +
                "<Filter>*</Filter>" +
                "<StartingIndex>0</StartingIndex>" +
                "<RequestedCount>8000</RequestedCount>" +
                "<SortCriteria></SortCriteria>" +
                "</u:Browse>" +
                "</s:Body>" +
                "</s:Envelope>";

        Map<String, String> header = new HashMap<>();
        header.clear();
        header.put("SOAPACTION", "\"urn:schemas-upnp-org:service:ContentDirectory:1" + "#Browse\"");
        return (SimpleHttpClient.httpPostWithHeader(url, postData, header, "text/xml; charset=\"utf-8\"", timeoutMs));
    }

    private List<ContentDirectoryInfo> parseObjectIds(String targetString)
    {
        try
        {
            List<ContentDirectoryInfo> objectIds = new ArrayList<>();
            objectIds.clear();

            int parsedIndex = 0;
            int maxSize = targetString.length();
            while (parsedIndex < maxSize)
            {
                String checkString = targetString.substring(parsedIndex);
                int startIndex = checkString.toLowerCase().indexOf("<container ");
                if (startIndex < 0)
                {
                    // containerタグが見つからない
                    break;
                }
                int endIndex = checkString.indexOf(">", startIndex);
                if (startIndex > endIndex)
                {
                    // タグの末尾が見つからない
                    //Log.v(TAG, " NOT FOUND END CLAUSE TAG");
                    break;
                }
                ContentDirectoryInfo objectInfo = parseObjectId(checkString.substring(startIndex, endIndex + 1));
                if (objectInfo.getObjectId().length() > 0)
                {
                    objectIds.add(objectInfo);
                }
                parsedIndex = parsedIndex + endIndex;
            }
            return (objectIds);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (new ArrayList<>());
    }

    private ContentDirectoryInfo parseObjectId(String targetString)
    {
        String objectId = "";
        String childCount = "0";
        int count = 0;
        try
        {
            int startIndex = targetString.toLowerCase().indexOf("<container ");
            if (startIndex < 0)
            {
                // パース失敗
                return (new ContentDirectoryInfo("", 0));
            }
            int endIndex = targetString.indexOf(">", startIndex);
            String containerString = targetString.substring(startIndex + 11, endIndex - 1);
            String[] attrList = containerString.split(" ");
            for (String attribute : attrList)
            {
                if (attribute.indexOf("id=") == 0)
                {
                    objectId = attribute.substring(3).replaceAll("\"","");
                }
                else if (attribute.toLowerCase().indexOf("childcount=") == 0)
                {
                    childCount = attribute.substring(12).replaceAll("\"","");
                }
            }
            count = Integer.parseInt(childCount);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Log.v(TAG, "  OBJECT ID : " + objectId + "  COUNT : " + childCount);
        return (new ContentDirectoryInfo(objectId, count));
    }

    private String browsePhotoSubRootDirectory(String accessUrl, String objectId, int startIndex)
    {
        String url =   accessUrl + "/upnp/control/ContentDirectory";
        String postData = "<?xml version=\"1.0\"?><s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\"><s:Body>" +
                "<u:Browse xmlns:u=\"urn:schemas-upnp-org:service:ContentDirectory:1\">" +
                "<ObjectID>" + objectId + "</ObjectID>" +
                "<BrowseFlag>BrowseDirectChildren</BrowseFlag>" +
                "<Filter>*</Filter>" +
                "<StartingIndex>" + startIndex + "</StartingIndex>" +
                "<RequestedCount>80000</RequestedCount>" +
                "<SortCriteria></SortCriteria>" +
                "</u:Browse></s:Body></s:Envelope>";

        Map<String, String> header = new HashMap<>();
        header.clear();
        header.put("SOAPACTION", "\"urn:schemas-upnp-org:service:ContentDirectory:1" + "#Browse\"");
        return (SimpleHttpClient.httpPostWithHeader(url, postData, header, "text/xml; charset=\"utf-8\"", timeoutMs));
    }

    private int parseContentObject(String receivedData)
    {
        int entryCount = 0;
        int startIndex = 0;
        int endLength = receivedData.length();
        try
        {
            //  <item> ～ </item> を切り出して保管する
            while (startIndex < endLength)
            {
                int index = receivedData.indexOf("<item", startIndex);
                if (index < 0)
                {
                    // もうコンテントがない
                    break;
                }
                int endIndex = receivedData.indexOf("</item>", index);
                if (endIndex < 0)
                {
                    endIndex = endLength;
                }

                String itemString = receivedData.substring(index, endIndex + 7);
                SonyImageContentInfoXml contentInfo = new SonyImageContentInfoXml(itemString);
                String contentName = contentInfo.getContentName();
                if (contentName.length() > 0)
                {
                    contentList.put(contentName, contentInfo);
                    entryCount++;
                }
                startIndex = endIndex;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (entryCount);
    }

    private String parseResult(String reply, boolean isResultSubstring)
    {
        String decordReply = reply;
        try
        {
            int startIndex = reply.indexOf("<Result>");
            int endIndex = reply.indexOf("</Result>");
            if ((isResultSubstring)&&(startIndex < endIndex) && (startIndex > 0))
            {
                decordReply = reply.substring((startIndex + 8), endIndex); // = URLDecoder.decode(reply.substring((startIndex + 8), endIndex), "UTF-8");
            }
            decordReply = decordReply.replaceAll("&lt;", "<");
            decordReply = decordReply.replaceAll("&gt;", ">");
            decordReply = decordReply.replaceAll("&quot;", "\"");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (decordReply);
    }
}
