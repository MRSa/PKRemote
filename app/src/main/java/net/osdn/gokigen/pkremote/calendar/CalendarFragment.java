package net.osdn.gokigen.pkremote.calendar;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.IInterfaceProvider;
import net.osdn.gokigen.pkremote.scene.IChangeScene;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import static android.content.Context.VIBRATOR_SERVICE;

public class CalendarFragment extends Fragment  implements View.OnClickListener
{
    private final String TAG = this.toString();

    private IInterfaceProvider interfaceProvider = null;
    private IChangeScene changeScene = null;
    private boolean myViewCreated = false;
    private View myView = null;

    private Context context = null;

    private int showYear = 2017;
    private int showMonth = 11;
    private int showDay = 11;

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
    private void prepare(@NonNull AppCompatActivity context, IChangeScene sceneSelector, IInterfaceProvider interfaceProvider)
    {
        Log.v(TAG, "prepare()");

        this.context = context;
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
            //  日付を動かす処理
            if (id == R.id.todaySelectButton)
            {
                prepareLabels(myView);
            }
            else if (id == R.id.showNextMonth)
            {
                currentMonth++;
                setCalendarLabels(myView);
            }
            else if (id == R.id.showPreviousMonth)
            {
                currentMonth--;
                setCalendarLabels(myView);
            }
            else if (id == R.id.showDayYear)
            {
                // 日付ピッカーを出したい。
                Log.v(TAG, "SELECT YEAR/MONTH LABEL.");
                /*
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                decideDate(year, month, day);
                */
            }
            else
            {
                // 日付を選択した処理
                Log.v(TAG, "onClick : " + id);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
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
                btn = view.findViewById(id);
                btn.setOnClickListener(this);
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
     *   カレンダーの設定
     *
     *
     */
    private void updateCalendar()
    {
/*
        Calendar calendar = new GregorianCalendar();
        calendar.set(currentYear, (currentMonth - 1), 1);

        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH) + 1;

        char [] appendChar = new char[NUMBER_OF_CALENDAR_BUTTONS];
        for (int index = 0; index < NUMBER_OF_CALENDAR_BUTTONS; index++)
        {
            appendChar[index] = ' ';
        }
        // 追加のテキストデータをもらう。
        try
        {
            resultReceiver.setAppendCharacter(currentYear, currentMonth, appendChar);
        }
        catch (Exception ex)
        {
            // ダイアログを閉じる
            try
            {
                dialog.dismiss();
            }
            catch (Exception e)
            {
                // 何もしない
            }
        }

        // テキストで日時を表示する
        TextView field = (TextView) layout.findViewById(R.id.showDayYear);
        field.setText(currentYear + "/" + currentMonth);
//        DateFormat dateF = new SimpleDateFormat("yyyy/MM");
//        field.setText(dateF.format(calendar.getTime()));

        // その月の最初の曜日を取得する
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        if (week == Calendar.SUNDAY)
        {
            monthStartIndex = 0;
        }
        else if (week == Calendar.MONDAY)
        {
            monthStartIndex = 1;
        }
        else if (week == Calendar.TUESDAY)
        {
            monthStartIndex = 2;
        }
        else if (week == Calendar.WEDNESDAY)
        {
            monthStartIndex = 3;
        }
        else if (week == Calendar.THURSDAY)
        {
            monthStartIndex = 4;
        }
        else if (week == Calendar.FRIDAY)
        {
            monthStartIndex = 5;
        }
        else if (week == Calendar.SATURDAY)
        {
            monthStartIndex = 6;
        }

        for (int index = 0; index < monthStartIndex; index++)
        {
            setButtonLabel(false, layout, index, "");
        }

        calendar.set(currentYear, currentMonth, 0);
        int lastIndex =calendar.get(Calendar.DATE);
        for (int index = 1; index <= lastIndex; index++)
        {
            if (appendChar[index - 1] == '_')
            {
                // アンダーバーの時にはアンダーラインを引く
                setButtonLabel(true, layout, (index + monthStartIndex - 1), "" + index + appendChar[index - 1]);
            }
            else
            {
                setButtonLabel(true, layout, (index + monthStartIndex - 1), index + "" + appendChar[index - 1]);
            }
        }

        for (int index = (monthStartIndex + lastIndex); index < NUMBER_OF_CALENDAR_BUTTONS; index++)
        {
            setButtonLabel(false, layout, index, "");
        }
*/
    }


    /**
     *   日時情報を設定する
     *
     */
    public void decideDate(int year, int month, int day)
    {
        showYear = year;
        showMonth = month;
        showDay = day;

        updateDateList();
    }

    /**
     *   一覧を指定した日付のものに更新する
     *
     */
    private void updateDateList()
    {
/*
        // ボタンに一覧を表示する日付を設定する
        Button dateSelectionButton =  context.findViewById(R.id.dateSelectionButton);
        String dateString = "" + showYear + "/" + showMonth + "/" + showDay;
        dateSelectionButton.setText(dateString);

        updateDataListView();
*/
    }

    /**
     *   一覧を今日の日付に更新する
     *
     */
    private void moveToToday()
    {
        Calendar calendar = Calendar.getInstance();
        showYear = calendar.get(Calendar.YEAR);
        showMonth = calendar.get(Calendar.MONTH) + 1;
        showDay = calendar.get(Calendar.DAY_OF_MONTH);

        updateDateList();
    }

    /**
     *  一覧表示情報を更新する
     *
     */
    private void updateDataListView()
    {


    }

}