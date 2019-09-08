package net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper;

import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient;
import net.osdn.gokigen.pkremote.camera.utils.XmlElement;

import java.util.ArrayList;
import java.util.List;

public class SonyCameraDeviceProvider implements ISonyCamera
{
    private static final String TAG = SonyCameraDeviceProvider.class.getSimpleName();
    private final List<ISonyApiService> apiServices;
    private final String ddUrl;
    private final String udn;
    private final String friendlyName;
    private final String modelName;
    private final String iconUrl;

    /**
     *   コンストラクタ： staticメソッド searchPanasonicCameraDevice() で生成する
     *
     */
    private SonyCameraDeviceProvider(String ddUrl, String friendlyName, String modelName, String udn, String iconUrl)
    {
        this.ddUrl = ddUrl;
        this.friendlyName = friendlyName;
        this.modelName = modelName;
        this.udn = udn;
        this.iconUrl = iconUrl;
        Log.v(TAG, "Sony Device : " + this.friendlyName + "(" + this.modelName + ") " + this.ddUrl + "  " + this.udn + " " + this.iconUrl);

        apiServices = new ArrayList<>();
    }

    /**
     *
     *
     */
    @Override
    public boolean hasApiService(@NonNull String serviceName)
    {
        try
        {
            for (ISonyApiService apiService : apiServices)
            {
                if (serviceName.equals(apiService.getName()))
                {
                    return (true);
                }
            }
            Log.v(TAG, "no API Service : " + serviceName + "[" + apiServices.size() + "]");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (false);
    }

    /**
     *
     *
     */
    @Override
    public List<ISonyApiService> getApiServices()
    {
        return (apiServices);
    }

    /**
     *
     *
     */
    @Override
    public String getFriendlyName()
    {
        return (friendlyName);
    }

    /**
     *
     *
     */
    @Override
    public String getModelName()
    {
        return (modelName);
    }

    /**
     *
     *
     */
    @Override
    public String getDdUrl()
    {
        return (ddUrl);
    }


    /**
     *
     *
     */
    private void addApiService(String name, String actionUrl)
    {
        Log.v(TAG, "API : " + name + "  : " + actionUrl);
        SonyApiService service = new SonyApiService(name, actionUrl);
        apiServices.add(service);
    }

    /**
     *
     *
     */
    public static ISonyCamera searchSonyCameraDevice(@NonNull String ddUrl)
    {
        SonyCameraDeviceProvider device = null;
        String ddXml;
        try
        {
            ddXml = SimpleHttpClient.httpGet(ddUrl, -1);
            Log.d(TAG, "fetch () httpGet done. : " + ddXml.length());
            if (ddXml.length() < 2)
            {
                // 内容がないときは...終了する
                Log.v(TAG, "NO BODY");
                return (null);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return (null);
        }
        try
        {
            //Log.v(TAG, "ddXml : " + ddXml);
            XmlElement rootElement = XmlElement.parse(ddXml);

            // "root"
            if ("root".equals(rootElement.getTagName()))
            {
                // "device"
                XmlElement deviceElement = rootElement.findChild("device");
                String friendlyName = deviceElement.findChild("friendlyName").getValue();
                String modelName = deviceElement.findChild("modelName").getValue();
                String udn = deviceElement.findChild("UDN").getValue();

                // "iconList"
                String iconUrl = "";
                XmlElement iconListElement = deviceElement.findChild("iconList");
                List<XmlElement> iconElements = iconListElement.findChildren("icon");
                for (XmlElement iconElement : iconElements)
                {
                    // Choose png icon to show Android UI.
                    if ("image/png".equals(iconElement.findChild("mimetype").getValue()))
                    {
                        String uri = iconElement.findChild("url").getValue();
                        String hostUrl = toSchemeAndHost(ddUrl);
                        iconUrl = hostUrl + uri;
                    }
                }
                device = new SonyCameraDeviceProvider(ddUrl, friendlyName, modelName, udn, iconUrl);

                // "av:X_ScalarWebAPI_DeviceInfo"
                XmlElement wApiElement = deviceElement.findChild("X_ScalarWebAPI_DeviceInfo");
                XmlElement wApiServiceListElement = wApiElement.findChild("X_ScalarWebAPI_ServiceList");
                List<XmlElement> wApiServiceElements = wApiServiceListElement.findChildren("X_ScalarWebAPI_Service");
                for (XmlElement wApiServiceElement : wApiServiceElements)
                {
                    String serviceName = wApiServiceElement.findChild("X_ScalarWebAPI_ServiceType").getValue();
                    String actionUrl = wApiServiceElement.findChild("X_ScalarWebAPI_ActionList_URL").getValue();
                    device.addApiService(serviceName, actionUrl);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Log.d(TAG, "fetch () parsing XML done.");
        if (device == null)
        {
            Log.v(TAG, "device is null.");
        }
        return (device);
    }

    private static String toSchemeAndHost(String url)
    {
        int i = url.indexOf("://"); // http:// or https://
        if (i == -1) {
            return ("");
        }

        int j = url.indexOf("/", i + 3);
        if (j == -1) {
            return ("");
        }

        return (url.substring(0, j));
    }

    private static String toHost(String url)
    {
        int i = url.indexOf("://"); // http:// or https://
        if (i == -1) {
            return ("");
        }

        int j = url.indexOf(":", i + 3);
        if (j == -1) {
            return ("");
        }
        return (url.substring(i + 3, j));
    }
}
