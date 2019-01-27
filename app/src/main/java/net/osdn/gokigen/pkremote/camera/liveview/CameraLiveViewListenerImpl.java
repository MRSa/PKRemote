package net.osdn.gokigen.pkremote.camera.liveview;

import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ICameraLiveViewListener;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IImageDataReceiver;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ILiveViewListener;

import java.util.Map;

public class CameraLiveViewListenerImpl implements ILiveViewListener, ICameraLiveViewListener
{
    private IImageDataReceiver imageView = null;

    /**
     * コンストラクタ
     */
    public CameraLiveViewListenerImpl()
    {

    }

    /**
     * 更新するImageViewを拾う
     *
     */
    @Override
    public void setCameraLiveImageView(IImageDataReceiver target)
    {
        imageView = target;
    }

    /**
     * LiveViewの画像データを更新する
     *
     */
    @Override
    public void onUpdateLiveView(byte[] data, Map<String, Object> metadata)
    {
        if (imageView != null)
        {
            imageView.setImageData(data, metadata);
        }
    }
}
