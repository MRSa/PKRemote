package net.osdn.gokigen.pkremote.camera.interfaces.liveview;

public interface IIndicatorControl
{
    // 撮影状態の記録
    enum shootingStatus
    {
        Unknown,
        Starting,
        Stopping,
    }

    void onAfLockUpdate(boolean isAfLocked);
    void onShootingStatusUpdate(shootingStatus status);
    void onMovieStatusUpdate(shootingStatus status);
    void onBracketingStatusUpdate(String message);
}
