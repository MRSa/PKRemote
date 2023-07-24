package net.osdn.gokigen.pkremote.camera.vendor.visionkids.wrapper.connection;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver;

/**
 *   VisionKidsとの接続シーケンス
 *
 */
public class VisionKidsCameraConnectSequence implements Runnable
{
    private final String TAG = this.toString();
    private final AppCompatActivity context;
    private final ICameraConnection cameraConnection;
    private final ICameraStatusReceiver cameraStatusReceiver;

    VisionKidsCameraConnectSequence(@NonNull AppCompatActivity context, @NonNull ICameraStatusReceiver statusReceiver, @NonNull final ICameraConnection cameraConnection)
    {
        Log.v(TAG, "VisionKidsCameraConnectSequence");
        this.context = context;
        this.cameraConnection = cameraConnection;
        this.cameraStatusReceiver = statusReceiver;
    }

    @Override
    public void run()
    {
        try
        {
            onConnectNotify();
        }
        catch (Exception e)
        {
            e.printStackTrace();
         }
    }

    private void onConnectNotify()
    {
        try
        {
            final Thread thread = new Thread(() -> {
                // カメラとの接続確立を通知する
                cameraStatusReceiver.onStatusNotify(context.getString(R.string.connect_connected));
                cameraStatusReceiver.onCameraConnected();
                Log.v(TAG, "onConnectNotify()");
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            onConnectError(e.getLocalizedMessage());
        }
    }

    private void onConnectError(String reason)
    {
        cameraConnection.alertConnectingFailed(reason);
    }
}
