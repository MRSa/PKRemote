package net.osdn.gokigen.pkremote.calendar

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Vibrator
import android.provider.CalendarContract
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import net.osdn.gokigen.pkremote.R
import net.osdn.gokigen.pkremote.camera.interfaces.IInterfaceProvider
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentsRecognizer.ICameraContentsListCallback
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IDownloadThumbnailImageCallback
import net.osdn.gokigen.pkremote.camera.interfaces.playback.IPlaybackControl
import net.osdn.gokigen.pkremote.scene.IChangeScene
import androidx.core.graphics.scale

class CalendarFragment : Fragment(), View.OnClickListener, View.OnLongClickListener,
    TargetMonthSetDialog.DateCallback, ICameraContentsListCallback
{
    private var activity: AppCompatActivity? = null
    private var interfaceProvider: IInterfaceProvider? = null
    private var changeScene: IChangeScene? = null
    private var fragmentIsActive = false

    // 年・月を保持 (Month: 1 ~ 12)
    private var currentYear = 0
    private var currentMonth = 0

    private fun prepare(
        activity: AppCompatActivity,
        sceneSelector: IChangeScene?,
        interfaceProvider: IInterfaceProvider?
    ) {
        Log.v(TAG, "prepare()")

        this.activity = activity
        this.changeScene = sceneSelector
        this.interfaceProvider = interfaceProvider
    }

    override fun onAttach(context: Context)
    {
        super.onAttach(context)
        Log.v(TAG, "onAttach()")
        if (context is IChangeScene)
        {
            changeScene = context
        }
        if (context is IInterfaceProvider)
        {
            interfaceProvider = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(TAG, "onCreateView()")
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.v(TAG, "onViewCreated()")

        triggerVibration()
        prepareButtons(view)
        setCurrentDateToToday()

        // 初期表示用画像リストの取得
        fetchRemoteCameraContents(isFirstTime = true)
    }

    override fun onDetach() {
        super.onDetach()
        changeScene = null
        interfaceProvider = null
    }

    override fun onPause() {
        super.onPause()
        fragmentIsActive = false
    }

    override fun onResume() {
        super.onResume()
        fragmentIsActive = true
    }

    private fun triggerVibration() {
        try {
            val context = context ?: return
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            @Suppress("DEPRECATION")
            vibrator?.vibrate(50)
        } catch (e: Exception) {
            Log.e(TAG, "Vibration failed", e)
        }
    }

    override fun onClick(v: View) {
        val rootView = view ?: return
        when (v.id) {
            R.id.todaySelectButton -> {
                setCurrentDateToToday()
                fetchRemoteCameraContents(isFirstTime = false)
            }
            R.id.showNextMonth -> {
                addMonth(1)
                setCalendarLabels(rootView)
                fetchRemoteCameraContents(isFirstTime = false)
            }
            R.id.showPreviousMonth -> {
                addMonth(-1)
                setCalendarLabels(rootView)
                fetchRemoteCameraContents(isFirstTime = false)
            }
            R.id.showDayYear -> {
                pickYearMonth()
            }
            else -> {
                val dateLabel = getSelectedDate(v.id)
                if (dateLabel.isNotEmpty()) {
                    Log.v(TAG, "SELECTED : $dateLabel")
                    changeScene?.changeScenceDateSelected(dateLabel)
                }
            }
        }
    }

    override fun onLongClick(v: View): Boolean {
        val dateLabel = getSelectedDate(v.id)
        if (dateLabel.length > 1) {
            Log.v(TAG, "LONG SELECTED : $dateLabel")
            openExternalCalendar(dateLabel)
            return true
        }
        return false
    }

    private fun fetchRemoteCameraContents(isFirstTime: Boolean) {
        val recognizer = interfaceProvider?.getCameraContentsRecognizer()
        recognizer?.getRemoteCameraContentsList(isFirstTime, this)
    }

    private fun getSelectedDate(buttonId: Int): String {
        val index = CALENDAR_LIST.indexOf(buttonId)
        if (index == -1) return ""

        val calendar: Calendar = GregorianCalendar()
        calendar.set(currentYear, currentMonth - 1, 1)
        val week = getStartCalendarIndex(calendar)
        calendar.add(Calendar.DATE, index - week)

        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH)
        return dateFormat.format(calendar.time)
    }

    private fun pickYearMonth() {
        val dialog = TargetMonthSetDialog.newInstance(
            getString(R.string.information_month_picker),
            currentYear,
            currentMonth
        )
        dialog.show(parentFragmentManager, "TargetMonthSetDialog")
    }

    private fun prepareButtons(view: View) {
        view.findViewById<ImageButton>(R.id.showNextMonth)?.setOnClickListener(this)
        view.findViewById<ImageButton>(R.id.showPreviousMonth)?.setOnClickListener(this)
        view.findViewById<Button>(R.id.todaySelectButton)?.setOnClickListener(this)
        view.findViewById<TextView>(R.id.showDayYear)?.setOnClickListener(this)

        for (id in CALENDAR_LIST) {
            view.findViewById<ImageButton>(id)?.let { button ->
                button.setOnClickListener(this)
                button.setOnLongClickListener(this)
            }
        }
    }

    private fun setCurrentDateToToday() {
        val calendar: Calendar = GregorianCalendar()
        currentYear = calendar.get(Calendar.YEAR)
        currentMonth = calendar.get(Calendar.MONTH) + 1
        view?.let { setCalendarLabels(it) }
    }

    private fun addMonth(amount: Int) {
        currentMonth += amount
        if (currentMonth > 12) {
            currentMonth = 1
            currentYear++
        } else if (currentMonth < 1) {
            currentMonth = 12
            currentYear--
        }
    }

    private fun setCalendarLabels(view: View) {
        val calendar: Calendar = GregorianCalendar()
        calendar.set(currentYear, currentMonth - 1, 1)

        val yearMonthFormat = SimpleDateFormat("yyyy/MM", Locale.ENGLISH)
        view.findViewById<TextView>(R.id.showDayYear)?.text = yearMonthFormat.format(calendar.time)

        val week = getStartCalendarIndex(calendar)
        calendar.add(Calendar.DATE, -week)

        for (id in DAY_LABEL_LIST) {
            val day = calendar.get(Calendar.DATE)
            view.findViewById<TextView>(id)?.apply {
                text = String.format(Locale.ENGLISH, "%02d", day)
                gravity = Gravity.CENTER_HORIZONTAL
            }
            calendar.add(Calendar.DATE, 1)
        }
    }

    private fun getStartCalendarIndex(calendar: Calendar): Int {
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 0
            else -> 0
        }
    }

    override fun dataSetYearMonth(year: Int, month: Int) {
        Log.v(TAG, "dataSetYearMonth : $year / $month")
        currentYear = year
        currentMonth = month
        view?.let { setCalendarLabels(it) }
        fetchRemoteCameraContents(isFirstTime = false)
    }

    override fun dataSetCancelled() {
        Log.v(TAG, "dataSetCancelled")
    }

    override fun contentsListCreated(nofContents: Int) {
        Log.v(TAG, "contentsListCreated() : $nofContents")
        if (nofContents == 0) {
            view?.let { rootView ->
                Snackbar.make(
                    rootView.findViewById(R.id.fragment1) ?: rootView,
                    R.string.get_camera_contents_is_nothing,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        val recognizer = interfaceProvider?.getCameraContentsRecognizer() ?: return
        val originalList = recognizer.getContentsList() ?: return
        val contentList = synchronized(originalList) {
            ArrayList(originalList)
        }
        val targetYear = currentYear
        val targetMonth = currentMonth

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            val imageMap = HashMap<Int, ICameraContent>()

            val calendar: Calendar = GregorianCalendar()
            calendar.set(targetYear, targetMonth - 1, 1)
            val week = getStartCalendarIndex(calendar)
            calendar.add(Calendar.DATE, -week)

            for (buttonId in CALENDAR_LIST) {
                val checkYear = calendar.get(Calendar.YEAR)
                val checkMonth = calendar.get(Calendar.MONTH)
                val checkDate = calendar.get(Calendar.DATE)

                for (content in contentList) {
                    val picsDate = content.getCapturedDate() ?: continue
                    val capturedDate: Calendar = GregorianCalendar()
                    capturedDate.time = picsDate

                    if (checkYear == capturedDate.get(Calendar.YEAR) &&
                        checkMonth == capturedDate.get(Calendar.MONTH) &&
                        checkDate == capturedDate.get(Calendar.DATE)
                    ) {
                        imageMap[buttonId] = content
                        break
                    }
                }
                calendar.add(Calendar.DATE, 1)
            }

            withContext(Dispatchers.Main) {
                updateCalendarImages(imageMap, targetYear, targetMonth)
            }
        }
    }

    private fun updateCalendarImages(
        imageMap: Map<Int, ICameraContent>,
        targetYear: Int,
        targetMonth: Int
    ) {
        val provider = interfaceProvider ?: return
        val playbackControl = provider.getPlaybackControl() ?: return
        val rootView = view ?: return

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            // 初期プレースホルダー設定
            for (id in CALENDAR_LIST) {
                val targetView = rootView.findViewById<ImageButton>(id) ?: continue
                val content = imageMap[id]
                val drawableId = if (content != null) {
                    R.drawable.ic_satellite_grey_24dp
                } else {
                    R.drawable.ic_crop_original_grey_24dp
                }
                targetView.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, drawableId, null)
                )
            }

            // サムネイル非同期読み込み
            for ((id, content) in imageMap) {
                val targetView = rootView.findViewById<ImageButton>(id) ?: continue
                val drawWidth = targetView.width
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    getImageThumbnail(
                        playbackControl,
                        targetView,
                        content,
                        targetYear,
                        targetMonth,
                        drawWidth
                    )
                }
            }
        }
    }

    private fun getImageThumbnail(
        playbackControl: IPlaybackControl,
        targetView: ImageButton,
        content: ICameraContent,
        targetYear: Int,
        targetMonth: Int,
        drawWidth: Int
    ) {
        val path = "${content.getContentPath()}/${content.getContentName()}".uppercase()
        // Log.v(TAG, "get Thumbnail : $path")
        playbackControl.downloadContentThumbnail(
            path,
            object : IDownloadThumbnailImageCallback {
                override fun onCompleted(bitmap: Bitmap?, metadata: MutableMap<String?, Any?>?) {
                    if (bitmap == null) return

                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                        // 月が変更されていないかチェック
                        if (currentYear == targetYear && currentMonth == targetMonth) {
                            val width = if (drawWidth > 0) drawWidth.toFloat() else targetView.width.toFloat()
                            if (width > 0 && bitmap.width > 0) {
                                val scale = width / bitmap.width.toFloat()
                                val height = bitmap.height.toFloat() * scale
                                val scaledBitmap =
                                    bitmap.scale(width.toInt(), height.toInt(), false)
                                targetView.setImageBitmap(scaledBitmap)
                            }
                        }
                    }
                }

                override fun onErrorOccurred(e: Exception) {
                    Log.e(TAG, "Thumbnail download error", e)
                }
            }
        )
    }

    private fun openExternalCalendar(dateLabel: String) {
        try {
            val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH)
            val date: Date = dateFormat.parse(dateLabel) ?: return
            val startMillis = date.time

            val builder = CalendarContract.CONTENT_URI.buildUpon().apply {
                appendPath("time")
                ContentUris.appendId(this, startMillis)
            }
            val intent = Intent(Intent.ACTION_VIEW).setData(builder.build())
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open calendar", e)
        }
    }

    val isFragmentActive: Boolean
        get() = (fragmentIsActive)

    companion object {
        private val TAG = CalendarFragment::class.java.simpleName

        private val DAY_LABEL_LIST = listOf(
            R.id.DayLabel00, R.id.DayLabel01, R.id.DayLabel02, R.id.DayLabel03, R.id.DayLabel04, R.id.DayLabel05, R.id.DayLabel06,
            R.id.DayLabel10, R.id.DayLabel11, R.id.DayLabel12, R.id.DayLabel13, R.id.DayLabel14, R.id.DayLabel15, R.id.DayLabel16,
            R.id.DayLabel20, R.id.DayLabel21, R.id.DayLabel22, R.id.DayLabel23, R.id.DayLabel24, R.id.DayLabel25, R.id.DayLabel26,
            R.id.DayLabel30, R.id.DayLabel31, R.id.DayLabel32, R.id.DayLabel33, R.id.DayLabel34, R.id.DayLabel35, R.id.DayLabel36,
            R.id.DayLabel40, R.id.DayLabel41, R.id.DayLabel42, R.id.DayLabel43, R.id.DayLabel44, R.id.DayLabel45, R.id.DayLabel46,
            R.id.DayLabel50, R.id.DayLabel51, R.id.DayLabel52, R.id.DayLabel53, R.id.DayLabel54, R.id.DayLabel55, R.id.DayLabel56
        )

        private val CALENDAR_LIST = listOf(
            R.id.Calendar00, R.id.Calendar01, R.id.Calendar02, R.id.Calendar03, R.id.Calendar04, R.id.Calendar05, R.id.Calendar06,
            R.id.Calendar10, R.id.Calendar11, R.id.Calendar12, R.id.Calendar13, R.id.Calendar14, R.id.Calendar15, R.id.Calendar16,
            R.id.Calendar20, R.id.Calendar21, R.id.Calendar22, R.id.Calendar23, R.id.Calendar24, R.id.Calendar25, R.id.Calendar26,
            R.id.Calendar30, R.id.Calendar31, R.id.Calendar32, R.id.Calendar33, R.id.Calendar34, R.id.Calendar35, R.id.Calendar36,
            R.id.Calendar40, R.id.Calendar41, R.id.Calendar42, R.id.Calendar43, R.id.Calendar44, R.id.Calendar45, R.id.Calendar46,
            R.id.Calendar50, R.id.Calendar51, R.id.Calendar52, R.id.Calendar53, R.id.Calendar54, R.id.Calendar55, R.id.Calendar56
        )

        @JvmStatic
        fun newInstance(
            context: AppCompatActivity,
            sceneSelector: IChangeScene?,
            provider: IInterfaceProvider
        ): CalendarFragment {
            return CalendarFragment().apply {
                prepare(context, sceneSelector, provider)
                arguments = Bundle()
            }
        }
    }
}
