package net.osdn.gokigen.pkremote.camera.vendor.ricoh.operation;

import net.osdn.gokigen.pkremote.camera.interfaces.control.ICaptureControl;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IAutoFocusFrameDisplay;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatus;
import net.osdn.gokigen.pkremote.camera.vendor.ricoh.operation.takepicture.RicohGr2MovieShotControl;
import net.osdn.gokigen.pkremote.camera.vendor.ricoh.operation.takepicture.RicohGr2SingleShotControl;

import androidx.annotation.NonNull;

/**
 *
 *
 */
public class RicohGr2CameraCaptureControl implements ICaptureControl
{
    private final RicohGr2SingleShotControl singleShotControl;
    private final RicohGr2MovieShotControl movieShotControl;
    private final ICameraStatus cameraStatus;
    private final boolean useGrCommand;
    private final boolean captureAfterAf;

    /**
     *
     *
     */
    public RicohGr2CameraCaptureControl(boolean useGrCommand, boolean captureAfterAf, @NonNull IAutoFocusFrameDisplay frameDisplayer, @NonNull ICameraStatus cameraStatus)
    {
        this.useGrCommand = useGrCommand;
        this.captureAfterAf = captureAfterAf;
        this.cameraStatus = cameraStatus;
        singleShotControl = new RicohGr2SingleShotControl(frameDisplayer);
        movieShotControl = new RicohGr2MovieShotControl(frameDisplayer);
    }

    /**
     *
     *
     */
    @Override
    public void doCapture(int kind)
    {
        try
        {
            if (cameraStatus.getStatus(ICameraStatus.TAKE_MODE).contains(ICameraStatus.TAKE_MODE_MOVIE))
            {
                movieShotControl.toggleMovie();
            }
            else
            {
                singleShotControl.singleShot(useGrCommand, captureAfterAf);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
