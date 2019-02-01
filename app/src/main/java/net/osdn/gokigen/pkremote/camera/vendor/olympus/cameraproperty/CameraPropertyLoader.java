package net.osdn.gokigen.pkremote.camera.vendor.olympus.cameraproperty;


import android.util.Log;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.vendor.olympus.wrapper.property.IOlyCameraPropertyProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

public class CameraPropertyLoader implements Runnable
{
    private final String TAG = toString();
    private final IOlyCameraPropertyProvider propertyInterface;
    private final IPropertyLoaderCallback callback;
    private ArrayList<CameraPropertyArrayItem> propertyItems = null;

    CameraPropertyLoader(IOlyCameraPropertyProvider propertyInterface, IPropertyLoaderCallback callback)
    {
        this.propertyInterface = propertyInterface;
        this.callback = callback;
    }

    @Override
    public void run()
    {
        Log.v(TAG, "CameraPropertyLoader::run() START");

        propertyItems = new ArrayList<>();

        // カメラプロパティを設定する
        Set<String> names = propertyInterface.getCameraPropertyNames();
        for (String name : names)
        {
            String title = propertyInterface.getCameraPropertyTitle(name);
            String value = propertyInterface.getCameraPropertyValue(name);
            String rawValue = propertyInterface.getCameraPropertyValueTitle(value);
            int iconId = (propertyInterface.canSetCameraProperty(name)) ? R.drawable.ic_web_asset_black_24dp : R.drawable.ic_block_black_24dp;

            propertyItems.add(new CameraPropertyArrayItem(name, title, rawValue, value, iconId));
        }

        // プロパティ名でソートしてしまおう。。。
        Collections.sort(propertyItems, new Comparator<CameraPropertyArrayItem>() {
            public int compare(CameraPropertyArrayItem o1, CameraPropertyArrayItem o2) {
                return o1.getPropertyName().compareTo(o2.getPropertyName());
            }
        });



        // 終了通知
        callback.finished();

        Log.v(TAG, "CameraPropertyLoader::run() END");
    }

    public void resetProperty()
    {
        Log.v(TAG, "CameraPropertyLoader::resetProperty() START");

        for (CameraPropertyArrayItem item : propertyItems)
        {
            item.resetValue();
        }
        callback.resetProperty();
        Log.v(TAG, "CameraPropertyLoader::resetProperty() END");

    }

    /**
     *   プロパティ一覧を応答
     *
     */
    public ArrayList<CameraPropertyArrayItem> getItemList()
    {
        return (propertyItems);
    }

    public interface IPropertyLoaderCallback
    {
        void finished();
        void resetProperty();
    }
}
