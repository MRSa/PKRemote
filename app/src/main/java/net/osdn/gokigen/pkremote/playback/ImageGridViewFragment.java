package net.osdn.gokigen.pkremote.playback;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.IInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraRunMode;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentsRecognizer;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraFileInfo;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;
import net.osdn.gokigen.pkremote.playback.detail.ImageContentInfoEx;
import net.osdn.gokigen.pkremote.playback.detail.ImagePagerViewFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.LruCache;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

/**
 *
 *
 */
public class ImageGridViewFragment extends Fragment
{
	private final String TAG = this.toString();
    private final String MOVIE_SUFFIX = ".mov";
    private final String JPEG_SUFFIX = ".jpg";
    private final String DNG_RAW_SUFFIX = ".dng";
	private final String OLYMPUS_RAW_SUFFIX = ".orf";
	private final String PENTAX_RAW_PEF_SUFFIX = ".pef";


    private GridView gridView;
	private boolean gridViewIsScrolling;
	private IInterfaceProvider interfaceProvider;
	private IPlaybackControl playbackControl;
	private ICameraRunMode runMode;
		
    private List<ImageContentInfoEx> contentList;
	private ExecutorService executor;
	private LruCache<String, Bitmap> imageCache;

	public static ImageGridViewFragment newInstance(@NonNull IInterfaceProvider interfaceProvider)
	{
		ImageGridViewFragment fragment = new ImageGridViewFragment();
		fragment.setControllers(interfaceProvider);
		return (fragment);
	}

	private void setControllers(@NonNull IInterfaceProvider interfaceProvider)
	{
		this.interfaceProvider = interfaceProvider;
		this.playbackControl = interfaceProvider.getPlaybackControl();
		this.runMode = interfaceProvider.getCameraRunMode();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        Log.v(TAG, "ImageGridViewFragment::onCreate()");

		executor = Executors.newFixedThreadPool(1);
		imageCache = new LruCache<>(160);
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.v(TAG, "ImageGridViewFragment::onCreateView()");
		View view = inflater.inflate(R.layout.fragment_image_grid_view, container, false);
		
		gridView = view.findViewById(R.id.gridView1);
		gridView.setAdapter(new GridViewAdapter(inflater));
		gridView.setOnItemClickListener(new GridViewOnItemClickListener());
		gridView.setOnScrollListener(new GridViewOnScrollListener());
		
		return (view);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.image_grid_view, menu);
		String title = getString(R.string.app_name);
		AppCompatActivity activity = (AppCompatActivity) getActivity();
		if (activity != null)
		{
            ActionBar bar = activity.getSupportActionBar();
            if (bar != null)
            {
                bar.setTitle(title);
            }
        }
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
	    int id = item.getItemId();
		if (id == R.id.action_refresh)
		{
			refresh();
			return (true);
		}
		return (super.onOptionsItemSelected(item));
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		Log.v(TAG, "onResume() Start");
		AppCompatActivity activity = (AppCompatActivity)getActivity();
		if (activity != null)
		{
		    try {
                ActionBar bar = activity.getSupportActionBar();
                if (bar != null) {
                    // アクションバーの表示をするかどうか
                    boolean isShowActionBar = false;
                    Context context = getContext();
                    if (context != null)
                    {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                        if (preferences != null)
                        {
                            isShowActionBar = preferences.getBoolean("use_playback_menu", false);
                        }
                        if (isShowActionBar) {
                            bar.show();  // ActionBarの表示を出す
                        } else {
                            bar.hide();   // ActionBarの表示を消す
                        }
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        try
        {
            refresh();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Log.v(TAG, "onResume() End");
	}
	
	@Override
	public void onPause()
	{
        Log.v(TAG, "onPause() Start");
        if (!runMode.isRecordingMode())
        {
            // Threadで呼んではダメみたいだ...
            runMode.changeRunMode(true);
        }

		if (!executor.isShutdown())
		{
			executor.shutdownNow();
		}
		super.onPause();
        Log.v(TAG, "onPause() End");
    }

	@Override
	public void onStop()
	{
		Log.v(TAG, "onStop()");
		super.onStop();
	}

	private void refresh()
    {
        try
        {
            if (runMode.isRecordingMode())
            {
                runMode.changeRunMode(false);
            }

            // ここはテンポラリで...
            AppCompatActivity activity = (AppCompatActivity)getActivity();
            if (activity != null)
            {
                RadioButton dateButton = activity.findViewById(R.id.radio_date);
                RadioButton pathButton = activity.findViewById(R.id.radio_path);
                Spinner categorySpinner = activity.findViewById(R.id.category_spinner);
                boolean dateChecked = dateButton.isChecked();
                dateButton.setChecked(dateChecked);
                pathButton.setChecked(!dateChecked);

                ICameraContentsRecognizer recognizer = interfaceProvider.getCameraContentsRecognizer();
                if (recognizer != null)
                {
                    // パス一覧 / 日付一覧
                    List<String> strList = (dateChecked) ? recognizer.getDateList() : recognizer.getPathList();

                    // 先頭に ALLを追加
                    //strList.add("ALL");
                    strList.add(0, "ALL");
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, strList);
                    categorySpinner.setAdapter(adapter);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
/*
                if (runMode.isRecordingMode())
                {
                    runMode.changeRunMode(false);
                }
*/
                refreshImpl();
            }
        });
        try
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showHideProgressBar(true);
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void showHideProgressBar(final boolean isVisible)
    {
        Activity activity = getActivity();
        if (activity != null)
        {
            ProgressBar bar = getActivity().findViewById(R.id.progress_bar);
            if (bar != null)
            {
                bar.setVisibility((isVisible) ? View.VISIBLE : View.GONE);
                bar.invalidate();
            }
        }
    }

	private void refreshImpl()
	{
		contentList = null;
		Log.v(TAG, "refreshImpl() start");

		playbackControl.downloadContentList(new IDownloadContentListCallback() {
			@Override
			public void onCompleted(List<ICameraFileInfo> list) {
				// Sort contents in chronological order (or alphabetical order).
				Collections.sort(list, new Comparator<ICameraFileInfo>() {
					@Override
					public int compare(ICameraFileInfo lhs, ICameraFileInfo rhs)
					{
						long diff = rhs.getDatetime().getTime() - lhs.getDatetime().getTime();
						if (diff == 0)
                        {
							diff = rhs.getFilename().compareTo(lhs.getFilename());
						}
						return (int)Math.min(Math.max(-1, diff), 1);
					}
				});

                List<ImageContentInfoEx> contentItems = new ArrayList<>();
                HashMap<String, ImageContentInfoEx> rawItems = new HashMap<>();
                for (ICameraFileInfo item : list)
                {
                    String path = item.getFilename().toLowerCase(Locale.getDefault());
                    if ((path.toLowerCase().endsWith(JPEG_SUFFIX))||(path.toLowerCase().endsWith(MOVIE_SUFFIX)))
                    {
                        contentItems.add(new ImageContentInfoEx(item, false, ""));
                    }
                    else if (path.toLowerCase().endsWith(DNG_RAW_SUFFIX))
                    {
                        //rawItems.put(path, new ImageContentInfoEx(item, true, DNG_RAW_SUFFIX));
                        contentItems.add(new ImageContentInfoEx(item, true, DNG_RAW_SUFFIX));
                    }
                    else if (path.toLowerCase().endsWith(OLYMPUS_RAW_SUFFIX))
                    {
                        rawItems.put(path, new ImageContentInfoEx(item, true, OLYMPUS_RAW_SUFFIX));
                    }
                    else if (path.toLowerCase().endsWith(PENTAX_RAW_PEF_SUFFIX))
                    {
                        //rawItems.put(path, new ImageContentInfoEx(item, true, PENTAX_RAW_PEF_SUFFIX));
                        contentItems.add(new ImageContentInfoEx(item, true, PENTAX_RAW_PEF_SUFFIX));
                    }
                }

                //List<ImageContentInfoEx> appendRawContents = new ArrayList<>();
                for (ImageContentInfoEx item : contentItems)
                {
                    String path = item.getFileInfo().getFilename().toLowerCase(Locale.getDefault());
                    if (path.toLowerCase().endsWith(JPEG_SUFFIX))
                    {
/*
                        String target1 = path.replace(JPEG_SUFFIX, DNG_RAW_SUFFIX);
                        ImageContentInfoEx raw1 = rawItems.get(target1);
                        if (raw1 != null)
                        {
                        	// JPEGファイルとRAWファイルがあるので、それをマークする
                            item.setHasRaw(true, DNG_RAW_SUFFIX);
                            Log.v(TAG, "DETECT RAW FILE: " + target1);
                        }
                        else
                        {
                            // RAWだけあった場合、一覧に追加する
                            appendRawContents.add(rawItems.get(path));
                        }
*/
                        String target2 = path.replace(JPEG_SUFFIX, OLYMPUS_RAW_SUFFIX);
                        ImageContentInfoEx raw2 = rawItems.get(target2);
                        if (raw2 != null)
                        {
                            // RAW は、JPEGファイルがあった場合にのみリストする
                            item.setHasRaw(true, OLYMPUS_RAW_SUFFIX);
                            Log.v(TAG, "DETECT RAW FILE: " + target2);
                        }
/*
                        String target3 = path.replace(JPEG_SUFFIX, PENTAX_RAW_PEF_SUFFIX);
                        ImageContentInfoEx raw3 = rawItems.get(target3);
                        if (raw3 != null)
                        {
                            // RAW は、JPEGファイルがあった場合にのみリストする
                            item.setHasRaw(true, PENTAX_RAW_PEF_SUFFIX);
                            Log.v(TAG, "DETECT RAW FILE: " + target3);
                        }
                        else
                        {
                            // RAWだけあった場合、一覧に追加する
                            appendRawContents.add(rawItems.get(path));
                        }
*/
                    }
                }
                //contentItems.addAll(appendRawContents);
                contentList = contentItems;

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
                        showHideProgressBar(false);
                        gridView.invalidateViews();
					}
				});
			}
			
			@Override
			public void onErrorOccurred(Exception e) {
				final String message = e.getMessage();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
                        showHideProgressBar(false);
						presentMessage("Load failed", message);
					}
				});
			}
		});
        Log.v(TAG, "refreshImpl() end");
    }

	private static class GridCellViewHolder
    {
		ImageView imageView;
		ImageView iconView;
	}
	
	private class GridViewAdapter extends BaseAdapter
    {
		private LayoutInflater inflater;

		GridViewAdapter(LayoutInflater inflater)
		{
			this.inflater = inflater;
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
			if (getItemList() == null)
			{
				return null;
			}
			return (getItemList().get(position));
		}

		@Override
		public long getItemId(int position)
        {
			return (position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
        {
			GridCellViewHolder viewHolder;
			if (convertView == null)
			{
				convertView = inflater.inflate(R.layout.view_grid_cell, parent, false);
				
				viewHolder = new GridCellViewHolder();
				viewHolder.imageView = convertView.findViewById(R.id.imageViewY);
				viewHolder.iconView = convertView.findViewById(R.id.imageViewZ);

				convertView.setTag(viewHolder);
			}
            else
            {
				viewHolder = (GridCellViewHolder)convertView.getTag();
			}

			ImageContentInfoEx infoEx = (ImageContentInfoEx) getItem(position);
            ICameraFileInfo item = (infoEx != null) ? infoEx.getFileInfo() : null;
			if (item == null)
            {
				viewHolder.imageView.setImageDrawable(null);
				viewHolder.iconView.setImageDrawable(null);
				return convertView;
			}
			String path = new File(item.getDirectoryPath(), item.getFilename()).getPath();
			Bitmap thumbnail = imageCache.get(path);
			if (thumbnail == null)
            {
				viewHolder.imageView.setImageDrawable(null);
				viewHolder.iconView.setImageDrawable(null);
				if (!gridViewIsScrolling)
                {
					if (executor.isShutdown())
                    {
						executor = Executors.newFixedThreadPool(1);
					}
					executor.execute(new ThumbnailLoader(viewHolder, path, infoEx.hasRaw()));
				}
			}
            else
            {
				viewHolder.imageView.setImageBitmap(thumbnail);
				if (path.toLowerCase().endsWith(MOVIE_SUFFIX))
                {
					viewHolder.iconView.setImageResource(R.drawable.ic_videocam_black_24dp);
				}
                else if (infoEx.hasRaw())
                {
                    viewHolder.iconView.setImageResource(R.drawable.ic_raw_black_1x);
                }
                else
                {
					viewHolder.iconView.setImageDrawable(null);
				}
			}
			return convertView;
		}
	}
	
	private class GridViewOnItemClickListener implements AdapterView.OnItemClickListener
    {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	        ImagePagerViewFragment fragment = ImagePagerViewFragment.newInstance(playbackControl, runMode, contentList, position);
            FragmentActivity activity = getActivity();
	        if (activity != null)
	        {
                FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
                transaction.replace(getId(), fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
		}
	}
	
	private class GridViewOnScrollListener implements AbsListView.OnScrollListener
    {
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
        {
			// No operation.
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState)
        {
			if (scrollState == SCROLL_STATE_IDLE)
			{
				gridViewIsScrolling = false;
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

	private class ThumbnailLoader implements Runnable
    {
		private GridCellViewHolder viewHolder;
		private String path;
        private final boolean hasRaw;
		
		ThumbnailLoader(GridCellViewHolder viewHolder, String path, boolean hasRaw)
        {
			this.viewHolder = viewHolder;
			this.path = path;
            this.hasRaw = hasRaw;
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
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    viewHolder.imageView.setImageBitmap(thumbnail);
                                    if (path.toLowerCase().endsWith(MOVIE_SUFFIX)) {
                                        viewHolder.iconView.setImageResource(R.drawable.ic_videocam_black_24dp);
                                    } else if (hasRaw) {
                                        viewHolder.iconView.setImageResource(R.drawable.ic_raw_black_1x);
                                    } else {
                                        viewHolder.iconView.setImageDrawable(null);
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
	
	
	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------
	
	private void presentMessage(String title, String message)
    {
		Context context = getActivity();
		if (context == null)
		{
            return;
        }
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title).setMessage(message);
		builder.show();
	}
	
	private void runOnUiThread(Runnable action)
    {
		Activity activity = getActivity();
		if (activity == null)
		{
            return;
        }
		activity.runOnUiThread(action);
	}

/*
	private Bitmap createRotatedBitmap(byte[] data, Map<String, Object> metadata)
    {
		Bitmap bitmap = null;
		try
        {
			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		}
		catch (Throwable e)
        {
			e.printStackTrace();
		}
		if (bitmap == null)
		{
		    Log.v(TAG, "createRotatedBitmap() : bitmap is null : " + data.length);
			return (null);
		}
		
		int degrees = getRotationDegrees(data, metadata);
		if (degrees != 0)
		{
			Matrix m = new Matrix();
			m.postRotate(degrees);
			try
            {
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
			}
			catch (Throwable e)
            {
				e.printStackTrace();
			}
		}
		return (bitmap);
	}
	
	private int getRotationDegrees(byte[] data, Map<String, Object> metadata)
    {
		int degrees = 0;
		int orientation = ExifInterface.ORIENTATION_UNDEFINED;
		
		if (metadata != null && metadata.containsKey("Orientation")) {
			orientation = Integer.parseInt((String)metadata.get("Orientation"));
		} else {
			// Gets image orientation to display a picture.
			try {
				File tempFile = File.createTempFile("temp", null);
				{
					FileOutputStream outStream = new FileOutputStream(tempFile.getAbsolutePath());
					outStream.write(data);
					outStream.close();
				}
				
				ExifInterface exifInterface = new ExifInterface(tempFile.getAbsolutePath());
				orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

				if (!tempFile.delete())
                {
                    Log.v(TAG, "File delete fail...");
                }
			}
			catch (IOException e)
            {
				e.printStackTrace();
			}
		}

		switch (orientation)
        {
            case ExifInterface.ORIENTATION_NORMAL:
                degrees = 0;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                degrees = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degrees = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degrees = 270;
                break;
            default:
                break;
		}
		return (degrees);
	}
*/
}
