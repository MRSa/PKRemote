package net.osdn.gokigen.pkremote.playback.grid;

import android.graphics.Bitmap;
import android.util.Log;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;

import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.LruCache;

public class ImageThumbnailLoader implements Runnable
{
    private final String TAG = this.toString();
    private final ImageGridCellViewHolder viewHolder;
    private final IPlaybackControl playbackControl;
    private final AppCompatActivity activity;
    private LruCache<String, Bitmap> imageCache;
    private String path;
    private final boolean hasRaw;
    private final boolean isMovie;

    ImageThumbnailLoader(@NonNull AppCompatActivity activity,  @NonNull IPlaybackControl playbackControl, LruCache<String, Bitmap> imageCache, ImageGridCellViewHolder viewHolder, String path, boolean hasRaw, boolean isMovie)
    {
        this.activity = activity;
        this.playbackControl = playbackControl;
        this.imageCache = imageCache;
        this.viewHolder = viewHolder;
        this.path = path;
        this.hasRaw = hasRaw;
        this.isMovie = isMovie;
    }

    @Override
    public void run()
    {
        class Box {
            boolean isDownloading = true;
        }
        final Box box = new Box();

        playbackControl.downloadContentThumbnail(path, new IDownloadThumbnailImageCallback()
        {
            @Override
            public void onCompleted(final Bitmap thumbnail, Map<String, Object> metadata)
            {
                if (thumbnail != null)
                {
                    try {
                        Log.v(TAG, "Thumbnail PATH : " + path + " size : " + thumbnail.getByteCount());
                        imageCache.put(path, thumbnail);
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                viewHolder.getImageView().setImageBitmap(thumbnail);
                                if (isMovie) {
                                    viewHolder.getIconView().setImageResource(R.drawable.ic_videocam_black_24dp);
                                } else if (hasRaw) {
                                    viewHolder.getIconView().setImageResource(R.drawable.ic_raw_black_1x);
                                } else {
                                    viewHolder.getIconView().setImageDrawable(null);
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                box.isDownloading = false;
            }

            @Override
            public void onErrorOccurred(Exception e)
            {
                box.isDownloading = false;
            }
        });

        // Waits to realize the serial download.
        while (box.isDownloading) {
            Thread.yield();
        }
    }
}
