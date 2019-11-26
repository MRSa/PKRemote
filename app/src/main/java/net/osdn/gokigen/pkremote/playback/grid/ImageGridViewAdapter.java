package net.osdn.gokigen.pkremote.playback.grid;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;
import net.osdn.gokigen.pkremote.playback.detail.CameraContentEx;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.LruCache;

public class ImageGridViewAdapter extends BaseAdapter implements AbsListView.OnScrollListener
{
    private final String TAG = toString();
    private final AppCompatActivity activity;
    private final IPlaybackControl playbackControl;
    private ExecutorService executor;
    private LruCache<String, Bitmap> imageCache;
    private LayoutInflater inflater;
    private boolean gridViewIsScrolling = false;
    private List<?> contentList;

    public ImageGridViewAdapter(@NonNull AppCompatActivity activity, @NonNull IPlaybackControl playbackControl, ExecutorService executor, LruCache<String, Bitmap> imageCache,  LayoutInflater inflater, List<?> contentList)
    {
        this.activity = activity;
        this.playbackControl = playbackControl;
        this.executor = executor;
        this.imageCache = imageCache;
        this.inflater = inflater;
        this.contentList = contentList;
    }

    public void setContentList(List<?> contentList)
    {
        this.contentList = contentList;
    }

    private List<?> getItemList()
    {
        return (contentList);
    }

    @Override
    public int getCount()
    {
        if (getItemList() == null)
        {
            return (0);
        }
        return getItemList().size();
    }

    @Override
    public Object getItem(int position)
    {
        try
        {
            if (getItemList() == null)
            {
                return (null);
            }
            return (getItemList().get(position));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (null);
    }

    @Override
    public long getItemId(int position)
    {
        return (position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ImageGridCellViewHolder viewHolder;
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.view_grid_cell, parent, false);
            if (convertView != null)
            {
                viewHolder = new ImageGridCellViewHolder((ImageView) convertView.findViewById(R.id.imageViewY), (ImageView) convertView.findViewById(R.id.imageViewZ), (ImageView) convertView.findViewById(R.id.imageViewX));
                convertView.setTag(viewHolder);
            }
            else
            {
                // Viewが取れない...
                Log.v(TAG, "getView() : FAIL...");
                return (null);
            }
        }
        else
        {
            viewHolder = (ImageGridCellViewHolder) convertView.getTag();
        }

        CameraContentEx infoEx = (CameraContentEx) getItem(position);
        ICameraContent item = (infoEx != null) ? infoEx.getFileInfo() : null;
        if (item == null)
        {
            viewHolder.getImageView().setImageResource(R.drawable.ic_satellite_grey_24dp);


            viewHolder.getIconView().setImageDrawable(null);
            viewHolder.getSelectView().setImageDrawable(null);
            return (convertView);
        }
        String path = new File(item.getContentPath(), item.getContentName()).getPath();
        Bitmap thumbnail = imageCache.get(path);
        if (thumbnail == null)
        {
            viewHolder.getImageView().setImageResource(R.drawable.ic_satellite_grey_24dp);
            viewHolder.getIconView().setImageDrawable(null);
            viewHolder.getSelectView().setImageDrawable(null);
            if (!gridViewIsScrolling)
            {
                if (executor.isShutdown())
                {
                    executor = Executors.newFixedThreadPool(1);
                }
                executor.execute(new ImageThumbnailLoader(activity, playbackControl, imageCache, viewHolder, path, infoEx));
            }
        }
        else
        {
            viewHolder.getImageView().setImageBitmap(thumbnail);
            if (infoEx.getFileInfo().isMovie())
            {
                viewHolder.getIconView().setImageResource(R.drawable.ic_videocam_grey_24dp);
            }
            else if (infoEx.getFileInfo().isRaw())
            {
                viewHolder.getIconView().setImageResource(R.drawable.ic_raw_black_1x);
            }
            else
            {
                viewHolder.getIconView().setImageDrawable(null);
            }
            if (infoEx.isSelected())
            {
                viewHolder.getSelectView().setImageResource(R.drawable.ic_check_green_24dp);
            }
            else
            {
                viewHolder.getSelectView().setImageDrawable(null);
            }
        }
        return (convertView);
    }

    //  AbsListView.OnScrollListener
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        // No operation.
    }

    //  AbsListView.OnScrollListener
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
        if (scrollState == SCROLL_STATE_IDLE)
        {
            gridViewIsScrolling = false;
            GridView gridView = activity.findViewById(R.id.gridView1);
            gridView.invalidateViews();
        }
        else if ((scrollState == SCROLL_STATE_FLING) || (scrollState == SCROLL_STATE_TOUCH_SCROLL))
        {
            gridViewIsScrolling = true;
            if (!executor.isShutdown())
            {
                executor.shutdownNow();
            }
        }
    }
}
