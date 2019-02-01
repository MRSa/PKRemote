package net.osdn.gokigen.pkremote.camera.vendor.olympus.operation.takepicture;


import android.graphics.PointF;
import android.graphics.RectF;

import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IIndicatorControl;

import jp.co.olympus.camerakit.OLYCamera;
import jp.co.olympus.camerakit.OLYCameraAutoFocusResult;
import jp.co.olympus.camerakit.OLYCameraKitException;

/**
 *   オートフォーカス制御クラス
 *
 *     1. setAutoFocusFrameDisplay() で AFフレームの表示クラスを設定
 *     2. lockAutoFocus() で AF-Lockを実行
 *     3. unlockAutoFocus() で AF-Unlock を実行
 *
 */
public class OlympusAutoFocusControl implements OLYCamera.TakePictureCallback
{
    private final OLYCamera camera;
    private final IIndicatorControl indicator;

    private IAutoFocusFrameDisplay frameDisplayer;
    private RectF focusFrameRect = null;

    /**
     *   コンストラクタ
     *
     */
    public OlympusAutoFocusControl(OLYCamera camera, IAutoFocusFrameDisplay frameDisplayer, IIndicatorControl indicator)
    {
        this.camera = camera;
        this.frameDisplayer = frameDisplayer;
        this.indicator = indicator;
    }

    /**
     *   オートフォーカスを駆動させ、ロックする
     *
     * @param point  ターゲットAF点
     *
     */
    public boolean lockAutoFocus(PointF point)
    {
        if (camera.isTakingPicture() || camera.isRecordingVideo())
        {
            //  撮影中の場合にはフォーカスロックはやらない。
            return (false);
        }

        RectF preFocusFrameRect = getPreFocusFrameRect(point);
        showFocusFrame(preFocusFrameRect, IAutoFocusFrameDisplay.FocusFrameStatus.Running, 0.0);

        try
        {
            // Set auto-focus point.
            camera.setAutoFocusPoint(point);

            // Lock auto-focus.
            focusFrameRect = preFocusFrameRect;
            camera.lockAutoFocus(this);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            // Lock failed.
            try
            {
                camera.clearAutoFocusPoint();
                camera.unlockAutoFocus();
                showFocusFrame(preFocusFrameRect, IAutoFocusFrameDisplay.FocusFrameStatus.Failed, 1.0);
            }
            catch (Exception ee)
            {
                ee.printStackTrace();
            }
            return (false);
        }
        return (true);
    }

    /**
     *   AF-Lを解除する
     *
     */
    public void unlockAutoFocus()
    {
        if (camera.isTakingPicture() || camera.isRecordingVideo())
        {
            // 撮影中の場合には、フォーカスロック解除はやらない
            return;
        }

        // Unlock auto-focus.
        try
        {
            camera.unlockAutoFocus();
            camera.clearAutoFocusPoint();
            hideFocusFrame();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onProgress(OLYCamera olyCamera, OLYCamera.TakingProgress takingProgress, OLYCameraAutoFocusResult olyCameraAutoFocusResult)
    {
        if (takingProgress == OLYCamera.TakingProgress.EndFocusing)
        {
            if (olyCameraAutoFocusResult.getResult().equals("ok") && olyCameraAutoFocusResult.getRect() != null)
            {
                // Lock succeed.
                RectF postFocusFrameRect = olyCameraAutoFocusResult.getRect();
                showFocusFrame(postFocusFrameRect, IAutoFocusFrameDisplay.FocusFrameStatus.Focused, 0.0);

            }
            else if (olyCameraAutoFocusResult.getResult().equals("none"))
            {
                // Could not lock.
                try
                {
                    camera.clearAutoFocusPoint();
                    camera.unlockAutoFocus();
                }
                catch (OLYCameraKitException ee)
                {
                    ee.printStackTrace();
                }
                hideFocusFrame();
            }
            else
            {
                // Lock failed.
                try
                {
                    camera.clearAutoFocusPoint();
                    camera.unlockAutoFocus();
                }
                catch (OLYCameraKitException ee)
                {
                    ee.printStackTrace();
                }
                showFocusFrame(focusFrameRect, IAutoFocusFrameDisplay.FocusFrameStatus.Failed, 1.0);
            }
        }
    }

    @Override
    public void onCompleted()
    {
        // フォーカスロック成功、なにもしない
    }

    @Override
    public void onErrorOccurred(Exception e)
    {
        // フォーカスロック失敗 : 通知される
        e.printStackTrace();
        try
        {
            camera.clearAutoFocusPoint();
            camera.unlockAutoFocus();
            hideFocusFrame();
        }
        catch (Exception ee)
        {
            ee.printStackTrace();
        }
        showFocusFrame(focusFrameRect, IAutoFocusFrameDisplay.FocusFrameStatus.Errored, 1.0);
    }

    private void showFocusFrame(RectF rect, IAutoFocusFrameDisplay.FocusFrameStatus status, double duration)
    {
        if (frameDisplayer != null)
        {
            frameDisplayer.showFocusFrame(rect, status, duration);
        }
        indicator.onAfLockUpdate(IAutoFocusFrameDisplay.FocusFrameStatus.Focused == status);
    }

    private void hideFocusFrame()
    {
        if (frameDisplayer != null)
        {
            frameDisplayer.hideFocusFrame();
        }
        indicator.onAfLockUpdate(false);
    }

    private RectF getPreFocusFrameRect(PointF point)
    {
        float imageWidth = 1.0f;
        float imageHeight = 1.0f;
        if (frameDisplayer != null)
        {
            imageWidth = frameDisplayer.getContentSizeWidth();
            imageHeight = frameDisplayer.getContentSizeHeight();
        }
        // Display a provisional focus frame at the touched point.
        float focusWidth = 0.125f;  // 0.125 is rough estimate.
        float focusHeight = 0.125f;
        if (imageWidth > imageHeight)
        {
            focusHeight *= (imageWidth / imageHeight);
        }
        else
        {
            focusHeight *= (imageHeight / imageWidth);
        }
        return (new RectF(point.x - focusWidth / 2.0f, point.y - focusHeight / 2.0f,
                 point.x + focusWidth / 2.0f, point.y + focusHeight / 2.0f));
    }
}
