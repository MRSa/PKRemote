package net.osdn.gokigen.pkremote.playback.detail;


import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraRunMode;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraFileInfo;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IContentInfoCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;
import net.osdn.gokigen.pkremote.playback.MyContentDownloader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.LruCache;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class ImagePagerViewFragment extends Fragment
{
    private final String TAG = this.toString();
    private static final String JPEG_SUFFIX = ".JPG";

    private IPlaybackControl playbackControl;
    private ICameraRunMode runMode;

	private List<CameraContentEx> contentList = null;
	private int contentIndex = 0;

	private LayoutInflater layoutInflater = null;
	private ViewPager viewPager = null;
	private LruCache<String, Bitmap> imageCache =null;


    public static ImagePagerViewFragment newInstance(@NonNull IPlaybackControl playbackControl, @NonNull ICameraRunMode runMode, @NonNull List<CameraContentEx> contentList, int contentIndex)
	{
		ImagePagerViewFragment fragment = new ImagePagerViewFragment();
		fragment.setInterface(playbackControl, runMode);
		fragment.setContentList(contentList, contentIndex);
		return (fragment);
	}


	private void setInterface(@NonNull IPlaybackControl playbackControl, @NonNull ICameraRunMode runMode)
    {
        this.playbackControl = playbackControl;
        this.runMode = runMode;
    }


	private void setContentList(@NonNull List<CameraContentEx> contentList, int contentIndex)
	{
		this.contentList = contentList;
		this.contentIndex = contentIndex;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		imageCache = new LruCache<>(5);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		layoutInflater = inflater;
		View view = layoutInflater.inflate(R.layout.fragment_image_pager_view, container, false);
		viewPager = view.findViewById(R.id.viewPager1);
		viewPager.setAdapter(new ImagePagerAdapter());
		viewPager.addOnPageChangeListener(new ImagePageChangeListener());
		
		return (view);
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
	{
		try
		{
            CameraContentEx info  = contentList.get(contentIndex);
            ICameraContent file = info.getFileInfo();
			String path = file.getContentPath() + "/" + file.getContentName();

			AppCompatActivity activity = (AppCompatActivity) getActivity();
			if (activity != null)
			{
                ActionBar bar = activity.getSupportActionBar();
                if (bar != null)
                {
                    bar.show();
                    bar.setTitle(path);
                }
            }
			String lowerCasePath = path.toUpperCase();
			if (lowerCasePath.endsWith(JPEG_SUFFIX))
            {
                if (info.hasRaw())
                {
                    inflater.inflate(R.menu.image_view_with_raw, menu);
                    MenuItem downloadMenuItem = menu.findItem(R.id.action_download_with_raw);
                    downloadMenuItem.setEnabled(true);
                }
                else
                {
                    inflater.inflate(R.menu.image_view, menu);
                    MenuItem downloadMenuItem = menu.findItem(R.id.action_download);
                    downloadMenuItem.setEnabled(true);
                }
			}
            else
            {
				inflater.inflate(R.menu.movie_view, menu);
				MenuItem downloadMenuItem = menu.findItem(R.id.action_download_movie);
				downloadMenuItem.setEnabled(true);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		boolean doDownload = false;
        boolean getInformation = false;
		boolean isSmallSize = false;
		boolean isRaw = false;
        if ((item.getItemId() == R.id.action_get_information)||(item.getItemId() == R.id.action_get_information_raw))
        {
            getInformation = true;
        }
        else if ((item.getItemId() == R.id.action_download_original_size)||(item.getItemId() == R.id.action_download_original_size_raw))
        {
			doDownload = true;
		}
        else if ((item.getItemId() == R.id.action_download_640x480)||(item.getItemId() == R.id.action_download_640x480_raw))
        {
            isSmallSize = true;
            doDownload = true;
        }
        else if (item.getItemId() == R.id.action_download_original_movie)
        {
            doDownload = true;
        }
        else if (item.getItemId() == R.id.action_download_raw)
        {
            doDownload = true;
            isRaw = true;
		}

		if (getInformation)
        {
        	Thread thread = new Thread(new Runnable() {
				@Override
				public void run()
                {
                    showFileInformation((contentList.get(contentIndex)).getFileInfo());
                }
        	});
        	try
            {
                thread.start();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

		if (doDownload)
		{
			try
			{
				//  ダイアログを表示して保存する
				saveImageWithDialog(isRaw, isSmallSize);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return (super.onOptionsItemSelected(item));
	}

    /**
     *
     *
     */
    private void showFileInformation(final ICameraContent contentInfo)
    {
        if (playbackControl != null)
        {
            playbackControl.getContentInfo(contentInfo.getContentPath(), contentInfo.getContentName(), new IContentInfoCallback() {
                /**
                 *
                 *
                 */
                @Override
                public void onCompleted(final ICameraFileInfo fileInfo)
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String message = "";
                            try {
                                String model = fileInfo.getModel();
                                String dateTime = "";
                                Date date = fileInfo.getDatetime();
                                if (date != null) {
                                    dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(date);
                                }
                                String path = fileInfo.getDirectoryPath() + "/" + fileInfo.getFilename();
                                String shutter = fileInfo.getShutterSpeed();
                                shutter = shutter.replace(".", "/");
                                String aperture = fileInfo.getAperature();
                                String iso = fileInfo.getIsoSensitivity();
                                String expRev = fileInfo.getExpRev();

                                message = path + "\r\n" + dateTime + "\r\n" + "- - - - - - - - - -\r\n  " + shutter + "  F" + aperture + " (" + expRev + ")" + " ISO" + iso + "\r\n" + "- - - - - - - - - -\r\n" + "  model : " + model + "\r\n";
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                            presentMessage(getString(R.string.download_control_get_information_title), message);
                            System.gc();
                        }
                    });
                }

                /**
                 *
                 *
                 */
                @Override
                public void onErrorOccurred(Exception e)
                {
                    e.printStackTrace();
                }
            });
        }

    }

    @Override
	public void onResume()
    {
		super.onResume();
		AppCompatActivity activity = (AppCompatActivity)getActivity();
		if (activity != null)
		{
            ActionBar bar = activity.getSupportActionBar();
            if (bar != null)
            {
                bar.setDisplayShowHomeEnabled(true);
                bar.show();
            }
        }

        if (runMode.isRecordingMode())
        {
            // Threadで呼んではダメみたいだ...
            runMode.changeRunMode(false);
        }

		try
        {
            // 画像表示が開始することを通知する
            playbackControl.showPictureStarted();
        }
		catch (Exception e)
        {
            e.printStackTrace();
        }

        viewPager.setCurrentItem(contentIndex);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		AppCompatActivity activity = (AppCompatActivity)getActivity();
		if (activity != null)
		{
            ActionBar bar = activity.getSupportActionBar();
            if (bar != null)
            {
                bar.hide();
            }
        }

        try
        {
            // 画像表示が終わったことを通知する
            playbackControl.showPictureFinished();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (!runMode.isRecordingMode())
        {
            // Threadで呼んではダメみたいだ...
            runMode.changeRunMode(true);
        }
    }

	private class ImagePagerAdapter extends PagerAdapter
    {

		@Override
		public int getCount()
        {
            int count = 0;
		    try
            {
                count = contentList.size();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
			return (count);
		}

		@Override
		public boolean isViewFromObject(@NonNull View view, @NonNull Object object)
        {
			return (view.equals(object));
		}
		
		@Override
		public @NonNull Object instantiateItem(@NonNull ViewGroup container, int position)
        {
			ImageView view = (ImageView)layoutInflater.inflate(R.layout.view_image_page, container, false);
			container.addView(view);
			downloadImage(position, view);
			return (view);
		}
		
		@Override
		public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object)
		{
			container.removeView((ImageView)object);
		}
		
	}

	private class ImagePageChangeListener implements ViewPager.OnPageChangeListener
	{

		@Override
		public void onPageScrollStateChanged(int state)
		{

		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
		{

		}

		@Override
		public void onPageSelected(int position)
		{
			contentIndex = position;
			try
			{
                ICameraContent file = (contentList.get(contentIndex)).getFileInfo();
                String path = file.getContentPath() + "/" + file.getContentName();

                AppCompatActivity activity = (AppCompatActivity) getActivity();
                if (activity != null)
                {
                    ActionBar bar = activity.getSupportActionBar();
                    if (bar != null)
                    {
                        bar.setTitle(path);
                    }
                    //activity.getSupportActionBar().setTitle(path);
                    activity.getFragmentManager().invalidateOptionsMenu();
                }
			}
			catch (Exception e)
            {
                e.printStackTrace();
            }
		}
	}

    /**
     *
     *
     */
	private void downloadImage(final int position, final ImageView view)
	{
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				downloadImageImpl(position, view);
			}
		});
		try
		{
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
	private void downloadImageImpl(int position, final ImageView view)
    {
        try
        {
            ICameraContent file = (contentList.get(position)).getFileInfo();
            final String path = file.getContentPath() + "/" + file.getContentName();

            // Get the cached image.
            final Bitmap bitmap = imageCache.get(path);
            if (bitmap != null)
            {
                if (view != null && viewPager.indexOfChild(view) > -1)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            view.setImageBitmap(bitmap);
                        }
                    });
                }
                return;
            }

            // Download the image.
            playbackControl.downloadContentScreennail(path, new IDownloadThumbnailImageCallback() {
                @Override
				//public void onCompleted(final byte[] data, final Map<String, Object> metadata) {
                public void onCompleted(final Bitmap bitmap, final Map<String, Object> metadata) {
                    // Cache the downloaded image.

                    //final Bitmap bitmap = createRotatedBitmap(data, metadata);
                    try {
                        if (bitmap == null)
                        {
                            System.gc();
                            return;
                        }
                        if (imageCache != null)
						{
                            imageCache.put(path, bitmap);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if ((bitmap != null) && (view != null) && (viewPager.indexOfChild(view) > -1))
                            {
                                view.setImageBitmap(bitmap);
                            }
                        }
                    });
                }

                @Override
                public void onErrorOccurred(Exception e) {
                    final String message = e.getMessage();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            presentMessage("Load failed", message);
                        }
                    });
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
	}

	/**
	 *   デバイスに画像ファイルをダウンロード（保存）する
	 *
	 * @param isRaw           RAWファイルをダウンロードするか
	 * @param isSmallSize    小さいサイズの量にするか
     */
	private void saveImageWithDialog(final boolean isRaw, final boolean isSmallSize)
	{
        Log.v(TAG, "saveImageWithDialog() : raw : " + isRaw + " (small : " + isSmallSize + ")");
        try
        {
            final Activity activity = getActivity();
            if (activity != null)
            {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MyContentDownloader contentDownloader = new MyContentDownloader(activity, playbackControl, null);
                        CameraContentEx infoEx = contentList.get(contentIndex);
                        if (infoEx != null)
                        {
                            ICameraContent fileInfo = infoEx.getFileInfo();
                            contentDownloader.startDownload(fileInfo, "", (isRaw) ? infoEx.getRawSuffix() : null, isSmallSize);
                        }
                    }
                });
                thread.start();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
	}

	// -------------------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------------------
	
	private void presentMessage(final String title, final String message)
    {
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    FragmentActivity activity = getActivity();
                    if (activity != null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle(title).setMessage(message);
                        builder.show();
                    }
                }
        });
	}
	
	private void runOnUiThread(Runnable action)
    {
		if (getActivity() == null)
        {
			return;
		}
		getActivity().runOnUiThread(action);
	}

/*
	private Bitmap createRotatedBitmap(byte[] data, Map<String, Object> metadata)
	{
		Bitmap bitmap;
		try
		{
			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
		}
		catch (OutOfMemoryError e)
		{
			e.printStackTrace();
			bitmap = null;
			System.gc();
		}
		if (bitmap == null)
		{
			return (null);
		}

		// ビットマップの回転を行う
		int degrees = getRotationDegrees(data, metadata);
		if (degrees != 0)
		{
			Matrix m = new Matrix();
			m.postRotate(degrees);
			try
			{
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
			}
			catch (OutOfMemoryError e)
			{
				e.printStackTrace();
				bitmap = null;
				System.gc();
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
		}
		else
		{
			// Gets image orientation to display a picture.
			try
            {
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
                    Log.v(TAG, "temp file delete failure.");
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
		return degrees;
	}
*/
}
