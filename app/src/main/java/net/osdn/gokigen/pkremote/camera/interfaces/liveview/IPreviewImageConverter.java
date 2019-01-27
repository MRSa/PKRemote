package net.osdn.gokigen.pkremote.camera.interfaces.liveview;

import android.graphics.Bitmap;

/**
 *   ビットマップ変換
 */
public interface IPreviewImageConverter
{
    Bitmap getModifiedBitmap(Bitmap src);
}
