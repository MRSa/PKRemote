package net.osdn.gokigen.pkremote.playback;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.IInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.interfaces.control.ICameraRunMode;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentsRecognizer;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;
import net.osdn.gokigen.pkremote.playback.detail.CameraContentEx;
import net.osdn.gokigen.pkremote.playback.detail.ImagePagerViewFragment;
import net.osdn.gokigen.pkremote.playback.grid.ImageGridViewAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
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
public class ImageGridViewFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener
{
	private final String TAG = this.toString();

    private static final String MOVIE_SUFFIX = ".mov";
    private static final String MOVIE_SUFFIX_MP4 = ".mp4";
    private static final String JPEG_SUFFIX = ".jpg";
    private static final String DNG_RAW_SUFFIX = ".dng";
	private static final String OLYMPUS_RAW_SUFFIX = ".orf";
	private static final String PENTAX_RAW_PEF_SUFFIX = ".pef";
    private static final String PANASONIC_RAW_SUFFIX = ".rw2";
    private static final String PANASONIC_RAW_SUFFIX2 = ".raw";
    private static final String SONY_RAW_SUFFIX = ".arw";
    private static final String NIKON_RAW_SUFFIX = ".nef";
    private static final String CANON_RAW_SUFFIX = ".crw";
    private static final String CANON_RAW_SUFFIX2 = ".cr2";
    private static final String CANON_RAW_SUFFIX3 = ".cr3";
    private static final String FUJI_RAW_SUFFIX = ".raf";


    private MyContentDownloader contentDownloader;
    private GridView gridView;
	private IInterfaceProvider interfaceProvider;
	private IPlaybackControl playbackControl;
	private ICameraRunMode runMode;

    private LruCache<String, Bitmap> imageCache;
    //private List<ICameraContent> imageContentList;
    private List<CameraContentEx> imageContentList;
	private ExecutorService executor;
	private ImageGridViewAdapter adapter = null;
	private String filterLabel = null;
	private int currentSelectedIndex = 0;
	private boolean fragmentIsActive = false;


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
        Activity activity = getActivity();
        if (activity != null)
        {
            this.contentDownloader = new MyContentDownloader(getActivity(), playbackControl, null);
        }
        else
        {
            this.contentDownloader = null;
        }
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        Log.v(TAG, "ImageGridViewFragment::onCreate()");

        imageCache = new LruCache<>(120);
		executor = Executors.newFixedThreadPool(1);
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		Log.v(TAG, "ImageGridViewFragment::onCreateView()");
		View view = inflater.inflate(R.layout.fragment_image_grid_view, container, false);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null)
        {
            adapter = new ImageGridViewAdapter(activity, playbackControl, executor, imageCache, inflater, imageContentList);
            gridView = view.findViewById(R.id.gridView1);
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(this);
            gridView.setOnItemLongClickListener(this);
            gridView.setOnScrollListener(adapter);
        }
		return (view);
	}
	
	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater)
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
        if (id == R.id.action_batch_download_original_size_raw)
        {
            // オリジナルサイズのダウンロード
            startDownloadBatch(false);
            return (true);
        }
        if (id == R.id.action_batch_download_640x480_raw)
        {
            // 小さいサイズのダウンロード
            startDownloadBatch(true);
            return (true);
        }
        if (id == R.id.action_select_all)
        {
            selectUnselectAll();
            return (true);
        }
		return (super.onOptionsItemSelected(item));
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		Log.v(TAG, "onResume() Start");
        fragmentIsActive = true;
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

            // ここはまだテンポラリで...
            //final AppCompatActivity activity = (AppCompatActivity)getActivity();
            //if (activity != null)
            {
                RadioButton dateButton = activity.findViewById(R.id.radio_date);
                RadioButton pathButton = activity.findViewById(R.id.radio_path);
                Spinner categorySpinner = activity.findViewById(R.id.category_spinner);
                boolean dateChecked = dateButton.isChecked();
                dateButton.setChecked(dateChecked);
                dateButton.setOnCheckedChangeListener(this);
                pathButton.setChecked(!dateChecked);
                pathButton.setOnCheckedChangeListener(this);
                categorySpinner.setOnItemSelectedListener(this);

                try
                {
                    ICameraContentsRecognizer recognizer = interfaceProvider.getCameraContentsRecognizer();
                    if (recognizer != null)
                    {
                        // パス一覧 / 日付一覧
                        List<String> strList = (dateChecked) ? recognizer.getDateList() : recognizer.getPathList();

                        // 先頭に ALLを追加
                        //strList.add("ALL");
                        strList.add(0, "ALL");

                        // デフォルトで設定したいフィルターがある場合...そのフィルターのインデックスを探る
                        if (filterLabel != null)
                        {
                            int index = 0;
                            for (String str : strList)
                            {
                                if (str.equals(filterLabel))
                                {
                                    currentSelectedIndex = index;
                                    break;
                                }
                                index++;
                            }
                        }
                        filterLabel = null;
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, strList);
                        categorySpinner.setAdapter(adapter);
                        categorySpinner.setSelection(currentSelectedIndex);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
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
        fragmentIsActive = false;
        try
        {
/*
            // Playbackモードでしか使わないので、RunMode は変更しない
            if (!runMode.isRecordingMode())
            {
                // Threadで呼んではダメみたいだ...
                runMode.changeRunMode(true);
            }
*/
            //  アクションバーは隠した状態に戻しておく
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity != null)
            {
                ActionBar bar = activity.getSupportActionBar();
                if (bar != null)
                {
                    bar.hide();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.gc();
		if (!executor.isShutdown())
		{
			executor.shutdownNow();
		}
		super.onPause();
        Log.v(TAG, "onPause() End");
    }

    public boolean isFragmentActive()
    {
        return (fragmentIsActive);
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

    private  List<ICameraContent> getContentsList()
    {
        try {
            ICameraContentsRecognizer recognizer = interfaceProvider.getCameraContentsRecognizer();
            if (recognizer == null) {
                Log.v(TAG, "getContentsList() : recognizer is null");
                return (new ArrayList<>());
            }
            List<ICameraContent> contents = recognizer.getContentsList();
            if (contents == null) {
                Log.v(TAG, "getContentsList() : contents is null");
                return (new ArrayList<>());
            }
            FragmentActivity activity = getActivity();
            if (activity == null)
            {
                return (recognizer.getContentsList());
            }

            Spinner spinner = activity.findViewById(R.id.category_spinner);
            String label = (String) spinner.getSelectedItem();
            Log.v(TAG, ":::::SELECTED LABEL  : " + label);

            RadioButton checkDate = activity.findViewById(R.id.radio_date);
            if (checkDate.isChecked())
            {
                return (recognizer.getContentsListAtDate(label));
            }
            return (recognizer.getContentsListAtPath(label));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (new ArrayList<>());
    }

    /**
     *   処理をContentsRecognizer ベースに差し替える
     *
     */
    private void refreshImpl()
    {
        imageContentList = null;
        Log.v(TAG, "refreshImpl() start");

        // 一覧を取得する
        List<ICameraContent> contents = getContentsList();
        List<CameraContentEx> contentItems = new ArrayList<>();
        //HashMap<String, CameraContentEx> rawItems = new HashMap<>();

        for (ICameraContent item : contents)
        {
            //Log.v(TAG, "......contents : [" + item.getContentName() + "]");
            String path = item.getContentName().toLowerCase(Locale.getDefault());
            if ((path.endsWith(JPEG_SUFFIX))||(path.endsWith(MOVIE_SUFFIX))||(path.endsWith(MOVIE_SUFFIX_MP4)))
            {
                contentItems.add(new CameraContentEx(item, false, ""));
            }
            else if (path.endsWith(DNG_RAW_SUFFIX))
            {
                contentItems.add(new CameraContentEx(item, true, DNG_RAW_SUFFIX));
            }
            else if (path.endsWith(OLYMPUS_RAW_SUFFIX))
            {
                //rawItems.put(path, new CameraContentEx(item, true, OLYMPUS_RAW_SUFFIX));
                contentItems.add(new CameraContentEx(item, true, OLYMPUS_RAW_SUFFIX));
            }
            else if (path.endsWith(PENTAX_RAW_PEF_SUFFIX))
            {
                contentItems.add(new CameraContentEx(item, true, PENTAX_RAW_PEF_SUFFIX));
            }
            else if (path.endsWith(PANASONIC_RAW_SUFFIX))
            {
                contentItems.add(new CameraContentEx(item, true, PANASONIC_RAW_SUFFIX));
            }
            else if (path.endsWith(PANASONIC_RAW_SUFFIX2))
            {
                contentItems.add(new CameraContentEx(item, true, PANASONIC_RAW_SUFFIX2));
            }
            else if (path.endsWith(SONY_RAW_SUFFIX))
            {
                contentItems.add(new CameraContentEx(item, true, SONY_RAW_SUFFIX));
            }
            else if (path.endsWith(CANON_RAW_SUFFIX))
            {
                contentItems.add(new CameraContentEx(item, true, CANON_RAW_SUFFIX));
            }
            else if (path.endsWith(CANON_RAW_SUFFIX2))
            {
                contentItems.add(new CameraContentEx(item, true, CANON_RAW_SUFFIX2));
            }
            else if (path.endsWith(CANON_RAW_SUFFIX3))
            {
                contentItems.add(new CameraContentEx(item, true, CANON_RAW_SUFFIX3));
            }
            else if (path.endsWith(NIKON_RAW_SUFFIX))
            {
                contentItems.add(new CameraContentEx(item, true, NIKON_RAW_SUFFIX));
            }
            else if (path.endsWith(FUJI_RAW_SUFFIX))
            {
                contentItems.add(new CameraContentEx(item, true, FUJI_RAW_SUFFIX));
            }
        }

        Log.v(TAG, " NOF CONTENT ITEMS : " + contentItems.size());
/*
        for (CameraContentEx item : contentItems)
        {
            String path = item.getFileInfo().getContentName().toLowerCase(Locale.getDefault());
            if (path.endsWith(JPEG_SUFFIX))
            {
                String target2 = path.replace(JPEG_SUFFIX, OLYMPUS_RAW_SUFFIX);
                CameraContentEx raw2 = rawItems.get(target2);
                if (raw2 != null)
                {
                    // RAW は、JPEGファイルがあった場合にのみリストする
                    item.setHasRaw(true, OLYMPUS_RAW_SUFFIX);
                    Log.v(TAG, "DETECT RAW FILE: " + target2);
                }
            }
        }
*/
        imageContentList = contentItems;
        //Log.v(TAG, ".....imageContentList : " + imageContentList.size());
        adapter.setContentList(imageContentList);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showHideProgressBar(false);
                gridView.invalidateViews();
            }
        });
        Log.v(TAG, "refreshImpl() end");
    }


    /**
     *   全選択・全選択解除
     *
     */
    private void selectUnselectAll()
    {
        if ((imageContentList == null)||(imageContentList.size() == 0))
        {
            // 選択されていない時は終わる。
            return;
        }

        int nofSelected = 0;
        for (CameraContentEx content : imageContentList)
        {
            if (content.isSelected())
            {
                nofSelected++;
            }
        }

        // 全部選択されているときは全選択解除・そうでない時は全選択
        boolean setSelected = (nofSelected != imageContentList.size());
        for (CameraContentEx content : imageContentList)
        {
            content.setSelected(setSelected);
        }

        // グリッドビューの再描画
        redrawGridView();
    }


    /**
     *    一括ダウンロードの開始
     *
     * @param isSmall  小さいサイズ(JPEG)
     */
    private void startDownloadBatch(final boolean isSmall)
    {
        try
        {
            if ((imageContentList == null)||(imageContentList.size() == 0))
            {
                // 画像が選択されていない場合にはなにもしない。
                return;
            }

            // 念のため、contentDownloader がなければ作る
            if (contentDownloader == null)
            {
                Activity activity = getActivity();
                if (activity == null)
                {
                    // activityが取れない時には終わる。
                    return;
                }
                this.contentDownloader = new MyContentDownloader(getActivity(), playbackControl, null);
            }
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        // ダウンロード枚数を取得
                        int totalSize = 0;
                        for (CameraContentEx content : imageContentList)
                        {
                            if (content.isSelected())
                            {
                                totalSize++;
                            }
                        }
                        if (totalSize == 0)
                        {
                            // 画像が選択されていなかった...終了する
                            return;
                        }
                        int count = 1;
                        for (CameraContentEx content : imageContentList)
                        {
                            if (content.isSelected())
                            {
                                contentDownloader.startDownload(content.getFileInfo(), " (" + count + "/" + totalSize + ") ", null, isSmall);
                                count++;

                                // 画像の選択を落とす
                                content.setSelected(false);

                                // ここでダウンロードが終わるまで、すこし待つ
                                do
                                {
                                    try
                                    {
                                        Thread.sleep(300);
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                } while (contentDownloader.isDownloading());
                            }
                        }

                        // グリッドビューの再描画
                        redrawGridView();
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

    private void redrawGridView()
    {
        // グリッドビューの再描画
        Activity activity = getActivity();
        if (activity != null)
        {
            getActivity().runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    if (gridView != null)
                    {
                        gridView.invalidateViews();
                    }
                }
            });
        }
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

    // AdapterView.OnItemClickListener
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        try
        {
            // 現在のスピナーで表示されているラベルを取得する。(返ってきたときのフィルター用)
            Activity  activity = getActivity();
            if (activity != null)
            {
                Spinner categorySpinner = activity.findViewById(R.id.category_spinner);
                SpinnerAdapter adapter = categorySpinner.getAdapter();
                filterLabel = (String) adapter.getItem(currentSelectedIndex);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        ImagePagerViewFragment fragment = ImagePagerViewFragment.newInstance(interfaceProvider, imageContentList, position);
        FragmentActivity activity = getActivity();
        if (activity != null)
        {
            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.replace(getId(), fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    //  AdapterView.OnItemLongClickListener
    @Override
    public boolean onItemLongClick(final AdapterView<?> parent, View view, int position, long id)
    {
        try
        {
            CameraContentEx infoEx = imageContentList.get(position);
            if (infoEx != null)
            {
                boolean isChecked = infoEx.isSelected();
                infoEx.setSelected(!isChecked);
            }
            view.invalidate();
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        ImageGridViewAdapter adapter = (ImageGridViewAdapter) parent.getAdapter();
                        adapter.notifyDataSetChanged();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            return (true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (false);
    }

    // AdapterView.OnItemSelectedListener
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        Log.v(TAG, "onItemSelected()");
        try
        {
            currentSelectedIndex = position;
            refresh();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //  AdapterView.OnItemSelectedListener
    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
        Log.v(TAG, "onNothingSelected()");
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        Log.v(TAG, "onCheckedChanged : " + isChecked);
        try
        {
            if (!isChecked)
            {
                // do nothing!
                return;
            }
            boolean dateChecked = (buttonView.getId() == R.id.radio_date);
            ICameraContentsRecognizer recognizer = interfaceProvider.getCameraContentsRecognizer();
            FragmentActivity activity = getActivity();
            if ((recognizer != null)&&(activity != null))
            {
                Spinner categorySpinner = activity.findViewById(R.id.category_spinner);

                // パス一覧 / 日付一覧
                List<String> strList = (dateChecked) ? recognizer.getDateList() : recognizer.getPathList();

                // 先頭に ALLを追加
                //strList.add("ALL");
                strList.add(0, "ALL");
                ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, strList);
                categorySpinner.setAdapter(adapter);
                categorySpinner.invalidate();

                // 画面更新。
                refresh();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void clearImageCache()
    {
        try
        {
            imageCache.evictAll();
            Log.v(TAG, " clearImageCache()");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setFilterLabel(String filterLabel)
    {
        this.filterLabel = filterLabel;
    }
}
