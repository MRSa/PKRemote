package net.osdn.gokigen.pkremote.calendar;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.IInterfaceProvider;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentsRecognizer;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback;
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl;
import net.osdn.gokigen.pkremote.scene.IChangeScene;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 *
 *
 *
 */
public class CalendarFragment extends Fragment  implements View.OnClickListener, View.OnLongClickListener, TargetMonthSetDialog.Callback, ICameraContentsRecognizer.ICameraContentsListCallback
{
    private final String TAG = this.toString();

    private IInterfaceProvider interfaceProvider = null;
    private IChangeScene changeScene = null;
    private AppCompatActivity activity = null;
    private boolean myViewCreated = false;
    private View myView = null;

    private int currentYear = 0;
    private int currentMonth = 0;

    private static final List<Integer> dayLabelList = new ArrayList<Integer>()
    {
        {
            add(R.id.DayLabel00);
            add(R.id.DayLabel01);
            add(R.id.DayLabel02);
            add(R.id.DayLabel03);
            add(R.id.DayLabel04);
            add(R.id.DayLabel05);
            add(R.id.DayLabel06);

            add(R.id.DayLabel10);
            add(R.id.DayLabel11);
            add(R.id.DayLabel12);
            add(R.id.DayLabel13);
            add(R.id.DayLabel14);
            add(R.id.DayLabel15);
            add(R.id.DayLabel16);

            add(R.id.DayLabel20);
            add(R.id.DayLabel21);
            add(R.id.DayLabel22);
            add(R.id.DayLabel23);
            add(R.id.DayLabel24);
            add(R.id.DayLabel25);
            add(R.id.DayLabel26);

            add(R.id.DayLabel30);
            add(R.id.DayLabel31);
            add(R.id.DayLabel32);
            add(R.id.DayLabel33);
            add(R.id.DayLabel34);
            add(R.id.DayLabel35);
            add(R.id.DayLabel36);

            add(R.id.DayLabel40);
            add(R.id.DayLabel41);
            add(R.id.DayLabel42);
            add(R.id.DayLabel43);
            add(R.id.DayLabel44);
            add(R.id.DayLabel45);
            add(R.id.DayLabel46);

            add(R.id.DayLabel50);
            add(R.id.DayLabel51);
            add(R.id.DayLabel52);
            add(R.id.DayLabel53);
            add(R.id.DayLabel54);
            add(R.id.DayLabel55);
            add(R.id.DayLabel56);
        }
    };

    private static final List<Integer> calendarList = new ArrayList<Integer>()
    {
        {
            add(R.id.Calendar00);
            add(R.id.Calendar01);
            add(R.id.Calendar02);
            add(R.id.Calendar03);
            add(R.id.Calendar04);
            add(R.id.Calendar05);
            add(R.id.Calendar06);

            add(R.id.Calendar10);
            add(R.id.Calendar11);
            add(R.id.Calendar12);
            add(R.id.Calendar13);
            add(R.id.Calendar14);
            add(R.id.Calendar15);
            add(R.id.Calendar16);

            add(R.id.Calendar20);
            add(R.id.Calendar21);
            add(R.id.Calendar22);
            add(R.id.Calendar23);
            add(R.id.Calendar24);
            add(R.id.Calendar25);
            add(R.id.Calendar26);

            add(R.id.Calendar30);
            add(R.id.Calendar31);
            add(R.id.Calendar32);
            add(R.id.Calendar33);
            add(R.id.Calendar34);
            add(R.id.Calendar35);
            add(R.id.Calendar36);

            add(R.id.Calendar40);
            add(R.id.Calendar41);
            add(R.id.Calendar42);
            add(R.id.Calendar43);
            add(R.id.Calendar44);
            add(R.id.Calendar45);
            add(R.id.Calendar46);

            add(R.id.Calendar50);
            add(R.id.Calendar51);
            add(R.id.Calendar52);
            add(R.id.Calendar53);
            add(R.id.Calendar54);
            add(R.id.Calendar55);
            add(R.id.Calendar56);
        }
    };


    //private ICalendarDatePickup resultReceiver = null;
    //private AlertDialog dialog = null;

    public static CalendarFragment newInstance(@NonNull AppCompatActivity context, IChangeScene sceneSelector, @NonNull IInterfaceProvider provider)
    {
        CalendarFragment instance = new CalendarFragment();
        instance.prepare(context, sceneSelector, provider);

        // パラメータはBundleにまとめておく
        Bundle arguments = new Bundle();
        //arguments.putString("title", title);
        //arguments.putString("message", message);
        instance.setArguments(arguments);

        return (instance);
    }

    /**
     *
     */
    private void prepare(@NonNull AppCompatActivity activity, IChangeScene sceneSelector, IInterfaceProvider interfaceProvider)
    {
        Log.v(TAG, "prepare()");

        this.activity = activity;
        this.changeScene = sceneSelector;
        this.interfaceProvider = interfaceProvider;
    }

    /**
     *
     *
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate()");
    }

    /**
     *
     *
     */
    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        Log.v(TAG, "onAttach()");
    }

    /**
     *
     *
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);

        Log.v(TAG, "onCreateView()");
        if ((myViewCreated) && (myView != null))
        {
            // Viewを再利用。。。
            Log.v(TAG, "onCreateView() : called again, so do nothing... : " + myView);
            return (myView);
        }

        myView = inflater.inflate(R.layout.fragment_calendar, container, false);
        myViewCreated = true;
        try
        {
            Activity activity = this.getActivity();
            Vibrator vibrator = (activity != null) ? (Vibrator) activity.getSystemService(VIBRATOR_SERVICE) : null;
            if (vibrator != null)
            {
                vibrator.vibrate(50);
            }

            // カレンダー上のラベルを準備する
            prepareLabels(myView);

            // カレンダー上のボタンを準備する
            prepareButtons(myView);

            //  表示用の画像を取得する
            ICameraContentsRecognizer recognizer = interfaceProvider.getCameraContentsRecognizer();
            if (recognizer != null)
            {
                recognizer.getRemoteCameraContentsList(true, this);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (myView);
    }

    /**
     *   クリックされたときの処理
     */
    public void onClick(View v)
    {
        int id = v.getId();
        try
        {
            boolean isUpdateImage = false;

            //  日付を動かす処理
            if (id == R.id.todaySelectButton)
            {
                prepareLabels(myView);
                isUpdateImage = true;
            }
            else if (id == R.id.showNextMonth)
            {
                currentMonth++;
                setCalendarLabels(myView);
                isUpdateImage = true;
            }
            else if (id == R.id.showPreviousMonth)
            {
                currentMonth--;
                setCalendarLabels(myView);
                isUpdateImage = true;
            }
            else if (id == R.id.showDayYear)
            {
                // 年・月 ピッカーを出す
                Log.v(TAG, "SELECT YEAR/MONTH LABEL.");
                pickYearMonth();
            }
            else
            {
                // 画像をタッチした
                String dateLabel = getSelectedDate(id);
                Log.v(TAG, "SELECTED : " + dateLabel);
                changeScene.changeScenceToImageList(dateLabel);
            }

            if (isUpdateImage)
            {
                //  表示用の画像を取得する
                ICameraContentsRecognizer recognizer = interfaceProvider.getCameraContentsRecognizer();
                if (recognizer != null)
                {
                    recognizer.getRemoteCameraContentsList(false, this);
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private String getSelectedDate(int buttonId)
    {
        // カレンダー(画像)ボタン  ::  日付を選択した処理... 画面遷移させたい
        int labelId = 0;
        for (int calId : calendarList)
        {
            if (calId == buttonId)
            {
                // 選択されたボタンと画像のボタンが一致した...
                try
                {
                    Calendar calendar = new GregorianCalendar();
                    calendar.set(currentYear, currentMonth - 1, 1);
                    int week = getStartCalendarIndex(calendar);
                    calendar.add(Calendar.DATE, labelId - week);

                    DateFormat dateF = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
                    return (dateF.format(calendar.getTime()));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            labelId++;
        }
        return ("");
    }

    private void pickYearMonth()
    {
        try
        {
            TargetMonthSetDialog dialog2 = TargetMonthSetDialog.newInstance(getString(R.string.information_month_picker), currentYear, currentMonth, this);
            FragmentManager manager = getFragmentManager();
            if (manager != null)
            {
                dialog2.show(manager, "dialog2");
            }
            else
            {
                Log.v(TAG, "FragmentManager is NULL...");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *  月の動きボタンを移動させる
     *
     *
     */
    private void prepareButtons(View view)
    {
        try
        {
            ImageButton btnImage = view.findViewById(R.id.showNextMonth);
            btnImage.setOnClickListener(this);

            btnImage = view.findViewById(R.id.showPreviousMonth);
            btnImage.setOnClickListener(this);

            Button btn = view.findViewById(R.id.todaySelectButton);
            btn.setOnClickListener(this);

            TextView month = view.findViewById(R.id.showDayYear);
            month.setOnClickListener(this);

            // カレンダー(画像)ボタン
            for (int id : calendarList)
            {
                ImageButton imageBtn = view.findViewById(id);
                imageBtn.setOnClickListener(this);
                imageBtn.setOnLongClickListener(this);

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    /**
     *   ラベルを設定する(初期値)
     *
     */
    private void prepareLabels(@NonNull View view)
    {
        try
        {
            // カレンダーに今日の日付を設定する
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(System.currentTimeMillis());

            currentYear = calendar.get(Calendar.YEAR);
            currentMonth = calendar.get(Calendar.MONTH) + 1;

            setCalendarLabels(view);
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
    private void setCalendarLabels(@NonNull View view)
    {
        try
        {
            if (currentMonth > 12)
            {
                currentMonth = 1;
                currentYear++;
            }
            if (currentMonth < 1)
            {
                currentMonth = 12;
                currentYear--;
            }

            Calendar calendar = new GregorianCalendar();
            calendar.set(currentYear, currentMonth - 1, 1);
            int week = getStartCalendarIndex(calendar);
            calendar.set(currentYear, currentMonth, 0);
            int lastDay = calendar.get(Calendar.DATE);

            // テキストで 年/月 を表示する
            DateFormat dateF = new SimpleDateFormat("yyyy/MM", Locale.ENGLISH);
            String yearMonth = dateF.format(calendar.getTime());
            TextView field = view.findViewById(R.id.showDayYear);
            field.setText(yearMonth);

            int day = 1;
            int index = 0;
            for (int id : dayLabelList)
            {
                TextView area = view.findViewById(id);
                if ((index >= week)&&(day <= lastDay))
                {
                    area.setText(String.format(Locale.ENGLISH, "%02d", day));
                    area.setGravity(Gravity.CENTER_HORIZONTAL);
                    day++;
                }
                else
                {
                    area.setText(R.string.dummy);
                    area.setGravity(Gravity.CENTER_HORIZONTAL);
                }
                index++;
            }
            view.invalidate();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *
     *
     *
     */
    private int getStartCalendarIndex(Calendar calendar)
    {
        // その月の最初の曜日を取得する
        int week = 0;
        switch (calendar.get(Calendar.DAY_OF_WEEK))
        {
            case Calendar.MONDAY:
                week = 1;
                break;
            case Calendar.TUESDAY:
                week = 2;
                break;
            case Calendar.WEDNESDAY:
                week = 3;
                break;
            case Calendar.THURSDAY:
                week = 4;
                break;
            case Calendar.FRIDAY:
                week = 5;
                break;
            case Calendar.SATURDAY:
                week = 6;
                break;
            case Calendar.SUNDAY:
            default:
                break;
        }
        return (week);
    }

    /**
     *  年・月 ダイアログの結果を反映させる
     *
     */
    @Override
    public void dataSetYearMonth(int year, int month)
    {
        Log.v(TAG, "dataSetYearMonth : " + year + " / " + month);
        currentYear = year;
        currentMonth = month;

        setCalendarLabels(myView);

        //  表示用の画像を取得する
        ICameraContentsRecognizer recognizer = interfaceProvider.getCameraContentsRecognizer();
        if (recognizer != null)
        {
            recognizer.getRemoteCameraContentsList(false, this);
        }
    }

    /**
     *
     *
     *
     */
    @Override
    public void dataSetCancelled()
    {
        Log.v(TAG, "dataSetCancelled");
    }

    /**
     *
     *
     *
     */
    @Override
    public void contentsListCreated(int nofContents)
    {
        Log.v(TAG, "contentsListCreated() : " + nofContents);
        try
        {
            SparseArray<ICameraContent> imageMaps = new SparseArray<>();
            ICameraContentsRecognizer recognizer = interfaceProvider.getCameraContentsRecognizer();
            if (recognizer != null)
            {
                List<ICameraContent> contentList = recognizer.getContentsList();

                Calendar calendar = new GregorianCalendar();
                calendar.set(currentYear, currentMonth - 1, 1);
                int week = getStartCalendarIndex(calendar);
                calendar.add(Calendar.DATE, week * (-1));
                for (int index = 0; index < calendarList.size(); index++)
                {
                    int checkYear = calendar.get(Calendar.YEAR);
                    int checkMonth = calendar.get(Calendar.MONTH);
                    int checkDate = calendar.get(Calendar.DATE);
                    for (ICameraContent content : contentList)
                    {
                        Date picsDate = content.getCapturedDate();
                        Calendar capturedDate = new GregorianCalendar();
                        capturedDate.setTime(picsDate);
                        int picYear = capturedDate.get(Calendar.YEAR);
                        int picMonth = capturedDate.get(Calendar.MONTH);
                        int picDate = capturedDate.get(Calendar.DATE);
                        if ((checkYear == picYear)&&(checkMonth == picMonth)&&(checkDate == picDate))
                        {
                            // 日時一致...抜ける
                            imageMaps.append(calendarList.get(index), content);
                            Log.v(TAG, "MATCHED : " + content.getContentPath() + "/" + content.getContentName());
                            break;
                        }
                    }
                    // 一日進める
                    calendar.add(Calendar.DATE, 1);
                }

                // カレンダーに載せる画像の一覧ができた！
                updateCalendarImages(imageMaps);
            }
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
    private void updateCalendarImages(final SparseArray<ICameraContent> imageMaps)
    {
        try
        {
            if (interfaceProvider == null)
            {
                Log.v(TAG, "interfaceProvider is null...");
                return;
            }
            final IPlaybackControl playbackControl = interfaceProvider.getPlaybackControl();
            if (playbackControl == null)
            {
                Log.v(TAG, "getPlaybackControl is null...");
                return;
            }

            if (imageMaps == null)
            {
                Log.v(TAG, "imageMaps is null...");
                return;
            }

            if (activity == null)
            {
                Log.v(TAG, "Activity is null...");
                return;
            }
            final int targetYear = currentYear;
            final int targetMonth = currentMonth;
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run() {
                    int width = -1;
                    for (int id : calendarList)
                    {
                        try {
                            final ImageButton targetView = activity.findViewById(id);
                            final ICameraContent content = imageMaps.get(id);
                            width = (targetView != null) ? targetView.getWidth() : -1;

                            activity.runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    int drawableId = (content != null) ? R.drawable.ic_satellite_grey_24dp : R.drawable.ic_crop_original_grey_24dp;
                                    if (targetView != null)
                                    {
                                        targetView.setImageDrawable(ResourcesCompat.getDrawable(activity.getResources(), drawableId, null));
                                    }
                                }
                            });
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    for (int index = 0; index < imageMaps.size(); index++)
                    {
                        getImageThumbnail(playbackControl, imageMaps.keyAt(index), imageMaps.valueAt(index), targetYear, targetMonth, width);
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
     *
     */
    private void getImageThumbnail(@NonNull IPlaybackControl playbackControl, final int id, @NonNull final ICameraContent content, final int targetYear, final int targetMonth, final int drawWidth)
    {
        try
        {
            final ImageButton targetView = activity.findViewById(id);
            playbackControl.downloadContentThumbnail(content.getContentPath() + "/" + content.getContentName(), new IDownloadThumbnailImageCallback() {
                @Override
                public void onCompleted(final Bitmap bitmap, Map<String, Object> metadata)
                {
                    if (activity != null)
                    {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if ((targetView != null)&&(currentYear == targetYear)&&(currentMonth == targetMonth))
                                    {
                                        float width = drawWidth;
                                        if (width < 0)
                                        {
                                            width = targetView.getWidth();
                                        }
                                        float scale = width / (float) bitmap.getWidth();
                                        float height = (float) bitmap.getHeight() * scale;
                                        targetView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, (int) width, (int) height, false));
                                    }
                                    else
                                    {
                                        Log.v(TAG, "" + currentYear + "(" + targetYear + ") " + currentMonth + "" + " [" + targetMonth + "]");
                                    }
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }

                @Override
                public void onErrorOccurred(Exception e)
                {
                    e.printStackTrace();
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onLongClick(View v)
    {
        int id = v.getId();
        try
        {
            // 画像をロングタッチした...
            String dateLabel = getSelectedDate(id);
            if (dateLabel.length() > 1)
            {
                Log.v(TAG, "LONG SELECTED : " + dateLabel);

                return (true);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return false;
    }
}
