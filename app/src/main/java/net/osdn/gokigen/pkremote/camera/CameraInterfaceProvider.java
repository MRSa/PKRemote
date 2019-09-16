package net.osdn.gokigen.pkremote.camera;

import android.content.SharedPreferences;

import net.osdn.gokigen.pkremote.IInformationReceiver;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraButtonControl;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraConnection;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ILiveViewListener;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentsRecognizer;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraHardwareStatus;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraInformation;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraRunMode;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatus;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusReceiver;
import net.osdn.gokigen.pkremote.camera.interfaces.status.ICameraStatusWatcher;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICaptureControl;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.IDisplayInjector;
import net.osdn.gokigen.pkremote.camera.interfaces.control.IFocusingControl;
import net.osdn.gokigen.pkremote.camera.interfaces.IInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.interfaces.liveview.ILiveViewControl;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;
import net.osdn.gokigen.pkremote.camera.interfaces.control.IZoomLensControl;
import net.osdn.gokigen.pkremote.camera.playback.CameraContentsRecognizer;
import net.osdn.gokigen.pkremote.camera.utils.CameraStatusListener;
import net.osdn.gokigen.pkremote.camera.vendor.fujix.wrapper.FujiXInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.olympus.IOlympusInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.olympus.wrapper.OlympusInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.PanasonicCameraWrapper;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.IPtpIpInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.ptpip.wrapper.PtpIpInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.ricoh.wrapper.RicohGr2InterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.sony.ISonyInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.vendor.sony.wrapper.SonyCameraWrapper;
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

/**
 *
 *
 */
public class CameraInterfaceProvider implements IInterfaceProvider
{
    private final SonyCameraWrapper sony;
    private final OlympusInterfaceProvider olympus;
    private final RicohGr2InterfaceProvider ricohGr2;
    private final FujiXInterfaceProvider fujiX;
    private final PanasonicCameraWrapper panasonic;
    private final PtpIpInterfaceProvider ptpip;
    private final IInformationReceiver informationReceiver;
    private final CameraContentsRecognizer cameraContentsRecognizer;
    private final AppCompatActivity context;
    //private final CameraStatusListener statusListener;
    private ICameraConnection.CameraConnectionMethod connectionMethod = ICameraConnection.CameraConnectionMethod.UNKNOWN;

    public static IInterfaceProvider newInstance(@NonNull AppCompatActivity context, @NonNull ICameraStatusReceiver provider, @NonNull IInformationReceiver informationReceiver)
    {
        return (new CameraInterfaceProvider(context, provider, informationReceiver));
    }

    /**
     *
     *
     */
    private CameraInterfaceProvider(@NonNull AppCompatActivity context, @NonNull ICameraStatusReceiver provider, @NonNull IInformationReceiver informationReceiver)
    {
        this.context = context;
        CameraStatusListener statusListener = new CameraStatusListener();
        olympus = new OlympusInterfaceProvider(context, provider);
        ricohGr2 = new RicohGr2InterfaceProvider(context, provider);
        fujiX = new FujiXInterfaceProvider(context, provider, statusListener, informationReceiver);
        sony = new SonyCameraWrapper(context, provider, statusListener, informationReceiver);
        ptpip = new PtpIpInterfaceProvider(context, provider, statusListener, informationReceiver);
        panasonic = new PanasonicCameraWrapper(context, provider, statusListener, informationReceiver);
        this.informationReceiver = informationReceiver;
        this.cameraContentsRecognizer = new CameraContentsRecognizer(context, this);
    }

    @Override
    public IOlympusInterfaceProvider getOlympusInterfaceProvider()
    {
        return (olympus);
    }

    @Override
    public ISonyInterfaceProvider getSonyInterface()
    {
        return (sony);
    }

    @Override
    public IPtpIpInterfaceProvider getPtpIpInterface()
    {
        return (ptpip);
    }

    /**
     *
     *
     */
    @Override
    public ICameraConnection getCameraConnection()
    {
        try
        {
            ICameraConnection.CameraConnectionMethod connectionMethod = getCammeraConnectionMethodImpl();
            if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
            {
                return (olympus.getOlyCameraConnection());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.FUJI_X)
            {
                return (fujiX.getFujiXCameraConnection());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.PANASONIC)
            {
                return (panasonic.getPanasonicCameraConnection());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.SONY)
            {
                return (sony.getSonyCameraConnection());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.CANON)
            {
                return (ptpip.getPtpIpCameraConnection());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.NIKON)
            {
                return (ptpip.getPtpIpCameraConnection());
            }
            else // if (connectionMethod == ICameraConnection.CameraConnectionMethod.RICOH)
            {
                return (ricohGr2.getRicohGr2CameraConnection());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (null);
    }

    @Override
    public ICameraButtonControl getButtonControl()
    {
        try
        {
            ICameraConnection.CameraConnectionMethod connectionMethod = getCammeraConnectionMethodImpl();
            if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
            {
                return (olympus.getButtonControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.FUJI_X)
            {
                return (fujiX.getButtonControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.PANASONIC)
            {
                return (panasonic.getButtonControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.SONY)
            {
                return (sony.getButtonControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.CANON)
            {
                return (ptpip.getButtonControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.NIKON)
            {
                return (ptpip.getButtonControl());
            }
            else // if (connectionMethod == ICameraConnection.CameraConnectionMethod.RICOH)
            {
                return (ricohGr2.getButtonControl());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (null);
    }

    @Override
    public IDisplayInjector getDisplayInjector()
    {
        try
        {
            ICameraConnection.CameraConnectionMethod connectionMethod = getCammeraConnectionMethodImpl();
            if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
            {
                return (olympus.getDisplayInjector());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.FUJI_X)
            {
                return (fujiX.getDisplayInjector());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.PANASONIC)
            {
                return (panasonic.getDisplayInjector());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.SONY)
            {
                return (sony.getDisplayInjector());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.CANON)
            {
                return (ptpip.getDisplayInjector());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.NIKON)
            {
                return (ptpip.getDisplayInjector());
            }
            else // if (connectionMethod == ICameraConnection.CameraConnectionMethod.RICOH)
            {
                return (ricohGr2.getDisplayInjector());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (null);
    }

    @Override
    public ILiveViewControl getLiveViewControl()
    {
        try
        {
            ICameraConnection.CameraConnectionMethod connectionMethod = getCammeraConnectionMethodImpl();
            if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
            {
                return (olympus.getLiveViewControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.FUJI_X)
            {
                return (fujiX.getLiveViewControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.PANASONIC)
            {
                return (panasonic.getPanasonicLiveViewControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.SONY)
            {
                return (sony.getLiveViewControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.CANON)
            {
                return (ptpip.getLiveViewControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.NIKON)
            {
                return (ptpip.getLiveViewControl());
            }
            else // if (connectionMethod == ICameraConnection.CameraConnectionMethod.RICOH)
            {
                return (ricohGr2.getLiveViewControl());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (null);
    }

    @Override
    public ILiveViewListener getLiveViewListener()
    {
        try
        {
            ICameraConnection.CameraConnectionMethod connectionMethod = getCammeraConnectionMethodImpl();
            if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
            {
                return (olympus.getLiveViewListener());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.FUJI_X)
            {
                return (fujiX.getLiveViewListener());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.PANASONIC)
            {
                return (panasonic.getLiveViewListener());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.SONY)
            {
                return (sony.getLiveViewListener());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.CANON)
            {
                return (ptpip.getLiveViewListener());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.NIKON)
            {
                return (ptpip.getLiveViewListener());
            }
            else // if (connectionMethod == ICameraConnection.CameraConnectionMethod.RICOH)
            {
                return (ricohGr2.getLiveViewListener());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (null);
    }

    @Override
    public IFocusingControl getFocusingControl()
    {
        try
        {
            ICameraConnection.CameraConnectionMethod connectionMethod = getCammeraConnectionMethodImpl();
            if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
            {
                return (olympus.getFocusingControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.FUJI_X)
            {
                return (fujiX.getFocusingControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.PANASONIC)
            {
                return (panasonic.getFocusingControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.SONY)
            {
                return (sony.getFocusingControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.CANON)
            {
                return (ptpip.getFocusingControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.NIKON)
            {
                return (ptpip.getFocusingControl());
            }
            else // if (connectionMethod == ICameraConnection.CameraConnectionMethod.RICOH)
            {
                return (ricohGr2.getFocusingControl());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (null);
    }

    @Override
    public ICameraInformation getCameraInformation()
    {
        try
        {
            ICameraConnection.CameraConnectionMethod connectionMethod = getCammeraConnectionMethodImpl();
            if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
            {
                return (olympus.getCameraInformation());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.FUJI_X)
            {
                return (fujiX.getCameraInformation());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.PANASONIC)
            {
                return (panasonic.getCameraInformation());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.SONY)
            {
                return (sony.getCameraInformation());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.CANON)
            {
                return (ptpip.getCameraInformation());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.NIKON)
            {
                return (ptpip.getCameraInformation());
            }
            else // if (connectionMethod == ICameraConnection.CameraConnectionMethod.RICOH)
            {
                return (ricohGr2.getCameraInformation());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (null);
    }

    @Override
    public IZoomLensControl getZoomLensControl()
    {
        try
        {
            ICameraConnection.CameraConnectionMethod connectionMethod = getCammeraConnectionMethodImpl();
            if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
            {
                return (olympus.getZoomLensControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.FUJI_X)
            {
                return (fujiX.getZoomLensControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.PANASONIC)
            {
                return (panasonic.getZoomLensControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.SONY)
            {
                return (sony.getZoomLensControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.CANON)
            {
                return (ptpip.getZoomLensControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.NIKON)
            {
                return (ptpip.getZoomLensControl());
            }
            else // if (connectionMethod == ICameraConnection.CameraConnectionMethod.RICOH)
            {
                return (ricohGr2.getZoomLensControl());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (null);
    }

    @Override
    public ICaptureControl getCaptureControl()
    {
        try
        {
            ICameraConnection.CameraConnectionMethod connectionMethod = getCammeraConnectionMethodImpl();
            if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
            {
                return (olympus.getCaptureControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.FUJI_X)
            {
                return (fujiX.getCaptureControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.PANASONIC)
            {
                return (panasonic.getCaptureControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.SONY)
            {
                return (sony.getCaptureControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.CANON)
            {
                return (ptpip.getCaptureControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.NIKON)
            {
                return (ptpip.getCaptureControl());
            }
            else // if (connectionMethod == ICameraConnection.CameraConnectionMethod.RICOH)
            {
                return (ricohGr2.getCaptureControl());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (null);
    }

    @Override
    public ICameraStatus getCameraStatusListHolder()
    {
        try
        {
            ICameraConnection.CameraConnectionMethod connectionMethod = getCammeraConnectionMethodImpl();
            if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
            {
                return (olympus.getCameraStatusListHolder());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.FUJI_X)
            {
                return (fujiX.getCameraStatusListHolder());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.PANASONIC)
            {
                return (panasonic.getCameraStatusListHolder());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.SONY)
            {
                return (sony.getCameraStatusListHolder());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.CANON)
            {
                return (ptpip.getCameraStatusListHolder());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.NIKON)
            {
                return (ptpip.getCameraStatusListHolder());
            }
            else // if (connectionMethod == ICameraConnection.CameraConnectionMethod.RICOH)
            {
                return (ricohGr2.getCameraStatusListHolder());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (null);
    }

    @Override
    public ICameraStatusWatcher getCameraStatusWatcher()
    {
        try
        {
            ICameraConnection.CameraConnectionMethod connectionMethod = getCammeraConnectionMethodImpl();
            if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
            {
                return (olympus.getCameraStatusWatcher());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.FUJI_X)
            {
                return (fujiX.getCameraStatusWatcher());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.PANASONIC)
            {
                return (panasonic.getCameraStatusWatcher());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.SONY)
            {
                return (sony.getCameraStatusWatcher());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.CANON)
            {
                return (ptpip.getCameraStatusWatcher());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.NIKON)
            {
                return (ptpip.getCameraStatusWatcher());
            }
            else // if (connectionMethod == ICameraConnection.CameraConnectionMethod.RICOH)
            {
                return (ricohGr2.getCameraStatusWatcher());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (null);
    }

    @Override
    public IPlaybackControl getPlaybackControl()
    {
        try
        {
            ICameraConnection.CameraConnectionMethod connectionMethod = getCammeraConnectionMethodImpl();
            if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
            {
                return (olympus.getPlaybackControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.FUJI_X)
            {
                return (fujiX.getPlaybackControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.PANASONIC)
            {
                return (panasonic.getPlaybackControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.SONY)
            {
                return (sony.getPlaybackControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.CANON)
            {
                return (ptpip.getPlaybackControl());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.NIKON)
            {
                return (ptpip.getPlaybackControl());
            }
            else // if (connectionMethod == ICameraConnection.CameraConnectionMethod.RICOH)
            {
                return (ricohGr2.getPlaybackControl());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (null);
    }

    @Override
    public ICameraHardwareStatus getHardwareStatus()
    {
        try
        {
            ICameraConnection.CameraConnectionMethod connectionMethod = getCammeraConnectionMethodImpl();
            if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
            {
                return (olympus.getHardwareStatus());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.FUJI_X)
            {
                return (fujiX.getHardwareStatus());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.PANASONIC)
            {
                return (panasonic.getHardwareStatus());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.SONY)
            {
                return (sony.getHardwareStatus());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.CANON)
            {
                return (ptpip.getHardwareStatus());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.NIKON)
            {
                return (ptpip.getHardwareStatus());
            }
            else // if (connectionMethod == ICameraConnection.CameraConnectionMethod.RICOH)
            {
                return (ricohGr2.getHardwareStatus());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (null);
    }

    @Override
    public ICameraRunMode getCameraRunMode()
    {
        try
        {
            ICameraConnection.CameraConnectionMethod connectionMethod = getCammeraConnectionMethodImpl();
            if (connectionMethod == ICameraConnection.CameraConnectionMethod.OPC)
            {
                return (olympus.getCameraRunMode());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.FUJI_X)
            {
                return (fujiX.getCameraRunMode());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.PANASONIC)
            {
                return (panasonic.getCameraRunMode());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.SONY)
            {
                return (sony.getCameraRunMode());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.CANON)
            {
                return (ptpip.getCameraRunMode());
            }
            else if (connectionMethod == ICameraConnection.CameraConnectionMethod.NIKON)
            {
                return (ptpip.getCameraRunMode());
            }
            else // if (connectionMethod == ICameraConnection.CameraConnectionMethod.RICOH)
            {
                return (ricohGr2.getCameraRunMode());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (null);
    }

    /**
     *   OPC/GR2/SONY カメラを使用するかどうか
     *
     * @return OPC / SONY / RICOH  (ICameraConnection.CameraConnectionMethod)
     */
    public ICameraConnection.CameraConnectionMethod getCammeraConnectionMethod()
    {
        return (getCammeraConnectionMethodImpl());
    }

    /**
     *
     *
     */
    @Override
    public void resetCameraConnectionMethod()
    {
        connectionMethod = ICameraConnection.CameraConnectionMethod.UNKNOWN;
    }

    /**
     *
     *
     */
    @Override
    public IInformationReceiver getInformationReceiver()
    {
        return (informationReceiver);
    }

    @Override
    public ICameraContentsRecognizer getCameraContentsRecognizer()
    {
        return (cameraContentsRecognizer);
    }

    /**
     *
     *
     */
    private ICameraConnection.CameraConnectionMethod getCammeraConnectionMethodImpl()
    {
        if (connectionMethod != ICameraConnection.CameraConnectionMethod.UNKNOWN)
        {
            return (connectionMethod);
        }
        ICameraConnection.CameraConnectionMethod ret;
        try
        {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String connectionMethod = preferences.getString(IPreferencePropertyAccessor.CONNECTION_METHOD, "RICOH");
            if (connectionMethod.contains("RICOH"))
            {
                ret = ICameraConnection.CameraConnectionMethod.RICOH;
            }
            else if (connectionMethod.contains("FUJI_X"))
            {
                ret = ICameraConnection.CameraConnectionMethod.FUJI_X;
            }
            else if (connectionMethod.contains("PANASONIC"))
            {
                ret = ICameraConnection.CameraConnectionMethod.PANASONIC;
            }
            else if (connectionMethod.contains("SONY"))
            {
                ret = ICameraConnection.CameraConnectionMethod.SONY;
            }
            else if (connectionMethod.contains("CANON"))
            {
                ret = ICameraConnection.CameraConnectionMethod.CANON;
            }
            else if (connectionMethod.contains("NIKON"))
            {
                ret = ICameraConnection.CameraConnectionMethod.NIKON;
            }
            else // if (connectionMethod.contains("OPC"))
            {
                ret = ICameraConnection.CameraConnectionMethod.OPC;
            }
/*
            else if (connectionMethod.contains("SONY"))
            {
                ret = ICameraConnection.CameraConnectionMethod.SONY;
            }
*/
        }
        catch (Exception e)
        {
            e.printStackTrace();
            ret = ICameraConnection.CameraConnectionMethod.UNKNOWN;
        }
        connectionMethod = ret;
        return (connectionMethod);
    }
}
