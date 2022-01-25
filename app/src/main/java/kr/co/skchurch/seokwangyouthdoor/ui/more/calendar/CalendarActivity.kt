package kr.co.skchurch.seokwangyouthdoor.ui.more.calendar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import com.orhanobut.logger.Logger
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.prolificinteractive.materialcalendarview.*
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import kr.co.skchurch.seokwangyouthdoor.R
import kr.co.skchurch.seokwangyouthdoor.SeokwangYouthApplication
import kr.co.skchurch.seokwangyouthdoor.adapter.CalendarInfoListAdapter
import kr.co.skchurch.seokwangyouthdoor.data.AppDatabase
import kr.co.skchurch.seokwangyouthdoor.data.Constants
import kr.co.skchurch.seokwangyouthdoor.data.FirebaseManager
import kr.co.skchurch.seokwangyouthdoor.data.entities.CalendarEntity
import kr.co.skchurch.seokwangyouthdoor.databinding.ActivityCalendarBinding
import kr.co.skchurch.seokwangyouthdoor.utils.Util
import kr.co.skchurch.seokwangyouthdoor.utils.getBirthDate
import java.util.*

class CalendarActivity : AppCompatActivity() {

    companion object {
        private val TAG = CalendarActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityCalendarBinding
    private lateinit var calendarViewModel: CalendarViewModel
    private lateinit var adapter: CalendarInfoListAdapter
    private lateinit var calendarView: MaterialCalendarView
    private var db: AppDatabase = AppDatabase.getDatabase()
    private var calendarTextColor: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        calendarView = binding.calendarView
        initViews()
        bindViews()
    }

    private var lastBottomListData: List<CalendarEntity>? = null
    private fun initViews() = with(binding) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        calendarTextColor = getColor(R.color.bg_purple)

        adapter = CalendarInfoListAdapter()
        bottomList.layoutManager = LinearLayoutManager(bottomList.context)
        bottomList.adapter = adapter
        //bottomList.addItemDecoration(DividerItemDecoration(this@CalendarActivity, LinearLayoutManager.VERTICAL))

        calendarViewModel = ViewModelProvider(this@CalendarActivity).get(CalendarViewModel::class.java)
        calendarViewModel.listData.observe(this@CalendarActivity, {
            //refreshCalendar()
            if(!it.equals(lastBottomListData)) {
                refreshCalendar()
                refreshBottomList(it)
            }
            lastBottomListData = it
        })
        calendarViewModel.setCallback(object: CalendarViewModel.ICalendarEventCallback{
            override fun onChildRemoved() {
                Logger.d("onChildRemoved")
                refreshCalendar()
                requestScheduleByDate(calendarView.selectedDate)
            }
        })

        initCalendarView()
    }

    private fun initBottomList() {
        refreshBottomList(arrayListOf())
    }

    private fun initCalendarView() {
        //calendarView.topbarVisible = false
        //calendarView.showOtherDates = MaterialCalendarView.SHOW_OUT_OF_RANGE
        calendarView.isDynamicHeightEnabled = true
        //calendarView.setPadding(0, -20, 0, 30)
        calendarView.setDateTextAppearance(android.R.style.TextAppearance)
        //calendarView.setSelectedDate(calendarView.currentDate)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            calendarView.state().edit()
                .isCacheCalendarPositionEnabled(false)
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit()
        }
        //refreshCalendar()

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis()
        calendarView.setSelectedDate(calendar.time)
        //requestScheduleByDate(calendarView.currentDate)
        requestScheduleByDate(calendarView.selectedDate)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.board_toolbar_menu, menu)
        binding.toolbar.menu.forEach {
            if(it.itemId == R.id.tool_btn1) {
                it.icon = getDrawable(R.drawable.ic_home_24)
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.tool_btn1 -> {
                Logger.d("tool_btn1 click!")
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = System.currentTimeMillis()
                calendarView.setCurrentDate(calendar.time)
                calendarView.setSelectedDate(calendar.time)
                requestScheduleByDate(calendarView.selectedDate)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private var eventList: MutableList<CalendarEntity> = arrayListOf()
    private fun refreshCalendar() {
        eventList.clear()
        Thread(Runnable {
            db.calendarDao().getAllData().forEach { entity ->
                val dateArr = entity.date.split(".")
                if(calendarView.currentDate.year == dateArr[0].toInt() &&
                    calendarView.currentDate.month+1 == dateArr[1].toInt()) {
                    eventList.add(entity)
                }
            }
            //Logger.d("refreshCalendar before list : $eventList")
            val memberInfoList = db.memberInfoDao().getAllData()
            if(memberInfoList.isNotEmpty()) {
                var lastId = memberInfoList.last().id!!
                memberInfoList.forEach { member ->
                    val birthDay = member.birth.getBirthDate()
                    val tempDateArr = birthDay.split(".")
                    if(tempDateArr[0].toInt() == calendarView.currentDate.month+1) {
                        eventList.add(
                            CalendarEntity(
                                ++lastId,
                                member.name+" "+ SeokwangYouthApplication.context!!.getString(R.string.birthday),
                                null,
                                member.birth,
                                Constants.SCHEDULE_TYPE_BIRTHDAY,
                            Util.getUUID(),
                            Util.getTimestamp()))
                    }
                }
                //Logger.d("refreshCalendar after list : $eventList")
            }
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                resetCalendarView(eventList)
            }, 500)
        }).start()
    }

    private fun resetCalendarView(eventList: MutableList<CalendarEntity>) {
        eventList.forEach { entity ->
            Logger.d("resetCalendarView entity.date : ${entity.date}")
            val dateArr = entity.date.split(".")
            val eventDay = CalendarDay.from(calendarView.currentDate.year, dateArr[1].toInt()-1, dateArr[2].toInt())
            calendarView.addDecorator(EventDecorator(this, eventDay))
        }
    }

    private fun refreshBottomList(listData: List<CalendarEntity>) {
        adapter.submitList(listData)
    }

    private inner class EventDecorator(val context: Context, val dates: CalendarDay): DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay?): Boolean = dates == day

        override fun decorate(view: DayViewFacade?) {
            view?.addSpan(StyleSpan(Typeface.BOLD))
            view?.addSpan(ForegroundColorSpan(calendarTextColor))
            view?.addSpan(CustomDotSpan(5F, calendarTextColor, Util.dpToPx(context, 5f).toInt()))
            //view?.addSpan(DotSpan(5F, Color.parseColor("#1D872A")))
        }
    }

    private inner class CustomDotSpan(radius: Float, color: Int, val bottomMargin: Int): DotSpan(radius, color) {

        override fun drawBackground(
            canvas: Canvas,
            paint: Paint,
            left: Int,
            right: Int,
            top: Int,
            baseline: Int,
            bottom: Int,
            charSequence: CharSequence,
            start: Int,
            end: Int,
            lineNum: Int
        ) {
            super.drawBackground(
                canvas,
                paint,
                left,
                right,
                top,
                baseline,
                bottom + bottomMargin,
                charSequence,
                start,
                end,
                lineNum
            )
        }
    }

    private fun bindViews() = with(binding) {
        calendarView.setOnDateChangedListener { widget, date, selected ->
            Logger.d("onDateChange : $date / $selected")
            requestScheduleByDate(date)
        }
        calendarView.setOnMonthChangedListener { widget, date ->
            Logger.d("OnMonthChange : $date")
            initBottomList()
            refreshCalendar()
        }
        /*
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            Logger.d("onDateChange : $year / $month / $dayOfMonth")
            val requestDate = year.toString()+"."+(month+1).toString()+"."+dayOfMonth.toString()
            calendarViewModel.requestScheduleByDate(requestDate)
        }
         */

        FirebaseManager.instance.registerNotify(object: FirebaseManager.IFirebaseNotify{
            override fun onDataChanged() {
                Logger.d("Notify onDataChanged")
                //binding.progressBar.visibility = View.VISIBLE
                requestScheduleByDate(calendarView.selectedDate)
            }
        })
    }

    private fun requestScheduleByDate(currentDate: CalendarDay) {
        val requestDate = currentDate.year.toString()+"."+
                (currentDate.month+1).toString()+"."+
                currentDate.day.toString()
        Logger.d("requestScheduleByDate : $requestDate")
        calendarViewModel.requestScheduleByDate(requestDate)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}