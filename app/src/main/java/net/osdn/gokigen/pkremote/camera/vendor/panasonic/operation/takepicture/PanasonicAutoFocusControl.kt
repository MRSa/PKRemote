package net.osdn.gokigen.pkremote.camera.vendor.panasonic.operation.takepicture;

import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import androidx.annotation.NonNull;

import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IIndicatorControl;
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient;
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.IPanasonicCamera;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 *
 */
public class PanasonicAutoFocusControl
{
    private static final String TAG = PanasonicAutoFocusControl.class.getSimpleName();
    private static final int TIMEOUT_MS = 3000;
    private final IIndicatorControl indicator;
    private final IAutoFocusFrameDisplay frameDisplayer;
    private IPanasonicCamera camera = null;

    /**
     *
     *
     */
    public PanasonicAutoFocusControl(@NonNull final IAutoFocusFrameDisplay frameDisplayer, final IIndicatorControl indicator)
    {
        this.frameDisplayer = frameDisplayer;
        this.indicator = indicator;
    }

    /**
     *
     *
     */
    public void setCamera(@NonNull IPanasonicCamera panasonicCamera)
    {
        this.camera = panasonicCamera;
    }

    /**
     *
     *
     */
    public void lockAutoFocus(@NonNull final PointF point)
    {
        Log.v(TAG, "lockAutoFocus() : [" + point.x + ", " + point.y + "]");
        if (camera == null)
        {
            Log.v(TAG, "ISonyCameraApi is null...");
            return;
        }
        try
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    RectF preFocusFrameRect = getPreFocusFrameRect(point);
                    try
                    {
                        showFocusFrame(preFocusFrameRect, IAutoFocusFrameDisplay.FocusFrameStatus.Running, 0.0);

                        int posX = (int) (Math.floor(point.x * 1000.0));
                        int posY = (int) (Math.floor(point.y * 1000.0));
                        Log.v(TAG, "AF (" + posX + ", " + posY + ")");
                        String reply = SimpleHttpClient.httpGet(camera.getCmdUrl() + "cam.cgi?mode=camctrl&type=touch&value=" + posX + "/" + posY + "&value2=on", TIMEOUT_MS);

                        if (!reply.contains("ok"))
                        {
                            Log.v(TAG, "setTouchAFPosition() reply is null.");
                        }
/*
                        if (findTouchAFPositionResult(resultsObj))
                        {
                            // AF FOCUSED
                            Log.v(TAG, "lockAutoFocus() : FOCUSED");
                            showFocusFrame(preFocusFrameRect, IAutoFocusFrameDisplay.FocusFrameStatus.Focused, 0.0);
                        }
                        else
                        {
                            // AF ERROR
                            Log.v(TAG, "lockAutoFocus() : ERROR");
                            showFocusFrame(preFocusFrameRect, IAutoFocusFrameDisplay.FocusFrameStatus.Failed, 1.0);
                        }
*/
                        showFocusFrame(preFocusFrameRect, IAutoFocusFrameDisplay.FocusFrameStatus.Errored, 1.0);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        try
                        {
                            showFocusFrame(preFocusFrameRect, IAutoFocusFrameDisplay.FocusFrameStatus.Errored, 1.0);
                        }
                        catch (Exception ee)
                        {
                            ee.printStackTrace();
                        }
                    }
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *   シャッター半押し処理
     *
     */
    public void halfPressShutter(final boolean isPressed)
    {
        Log.v(TAG, "halfPressShutter() : " + isPressed);
        if (camera == null)
        {
            Log.v(TAG, "IPanasonicCamera is null...");
            return;
        }
        try
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        String status = (isPressed) ? "on" : "off";
                        String reply = SimpleHttpClient.httpGet(camera.getCmdUrl() + "cam.cgi?mode=camctrl&type=touch&value=500/500&value2=" + status, TIMEOUT_MS);
                        if (!reply.contains("ok"))
                        {
                            Log.v(TAG, "CENTER FOCUS (" + status + ") FAIL...");
                        }
                        else
                        {
                            indicator.onAfLockUpdate(isPressed);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     *
     */
    public void unlockAutoFocus()
    {
        Log.v(TAG, "unlockAutoFocus()");
        if (camera == null)
        {
            Log.v(TAG, "IPanasonicCamera is null...");
            return;
        }
        try
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        String reply = SimpleHttpClient.httpGet(camera.getCmdUrl() + "cam.cgi?mode=camctrl&type=touch&value=500/500&value2=off", TIMEOUT_MS);
                        if (!reply.contains("ok"))
                        {
                            Log.v(TAG, "CENTER FOCUS (UNLOCK) FAIL...");
                        }
                        hideFocusFrame();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     *
     */
    private void showFocusFrame(RectF rect, IAutoFocusFrameDisplay.FocusFrameStatus status, double duration)
    {
        frameDisplayer.showFocusFrame(rect, status, duration);
        indicator.onAfLockUpdate(IAutoFocusFrameDisplay.FocusFrameStatus.Focused == status);
    }

    /**
     *
     *
     */
    private void hideFocusFrame()
    {
        frameDisplayer.hideFocusFrame();
        indicator.onAfLockUpdate(false);
    }

    /**
     *
     *
     */
    private RectF getPreFocusFrameRect(@NonNull PointF point)
    {
        float imageWidth =  frameDisplayer.getContentSizeWidth();
        float imageHeight =  frameDisplayer.getContentSizeHeight();

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

    /**
     *
     *
     */
    private static boolean findTouchAFPositionResult(JSONObject replyJson)
    {
        boolean afResult = false;
        try
        {
            int indexOfTouchAFPositionResult = 1;
            JSONArray resultsObj = replyJson.getJSONArray("result");
            if (!resultsObj.isNull(indexOfTouchAFPositionResult))
            {
                JSONObject touchAFPositionResultObj = resultsObj.getJSONObject(indexOfTouchAFPositionResult);
                afResult = touchAFPositionResultObj.getBoolean("AFResult");
                Log.v(TAG, "AF Result : " + afResult);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (afResult);
    }
}
