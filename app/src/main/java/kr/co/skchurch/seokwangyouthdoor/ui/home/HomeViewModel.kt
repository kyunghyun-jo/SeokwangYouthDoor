package kr.co.skchurch.seokwangyouthdoor.ui.home

import android.os.Handler
import android.os.Looper
import com.orhanobut.logger.Logger
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import kotlinx.coroutines.*
import kr.co.skchurch.seokwangyouthdoor.R
import kr.co.skchurch.seokwangyouthdoor.SeokwangYouthApplication
import kr.co.skchurch.seokwangyouthdoor.data.*
import kr.co.skchurch.seokwangyouthdoor.data.entities.HomeEntity
import kr.co.skchurch.seokwangyouthdoor.data.entities.MemberInfoEntity
import kr.co.skchurch.seokwangyouthdoor.data.entities.SimpleEntity
import kr.co.skchurch.seokwangyouthdoor.utils.Util
import java.lang.Runnable
import java.util.*

class HomeViewModel() : ViewModel() {
    companion object {
        private val TAG = HomeViewModel::class.java.simpleName
        private const val HEADER_NOTICE_ID = 1000L
        private const val HEADER_WEEK_EVENT_ID = 1100L
        private const val HEADER_NEW_MEMBER_LIST_ID = 1200L
        private const val HEADER_LAST_WEEK_ATTENDANCE_ID = 1300L
    }
    //private val scope = MainScope()
    private val _listData = MutableLiveData<List<HomeEntity>>()
    val listData: LiveData<List<HomeEntity>> = _listData
    private var db: AppDatabase = AppDatabase.getDatabase()
    var list: List<HomeEntity>? = null
    var noticeList: MutableList<HomeEntity> = mutableListOf()
    var attendanceList: MutableList<HomeEntity> = mutableListOf()
    var allEventList: MutableList<HomeEntity> = mutableListOf()

    init {
        GlobalScope.launch(Dispatchers.IO) {
            requestDB()
        }
    }

    private var newMemberClassData: SimpleEntity? = null
    private var isReadyForUseFirebase = false
    private fun requestDB() {
        if(FirebaseManager.instance.getCurrentUserId() == FirebaseConstants.EMPTY_USER) {
            //Toast.makeText(this, "로그인이 되어있지 않습니다.", Toast.LENGTH_SHORT).show()
            //finish()
            Handler(Looper.getMainLooper()).post(Runnable {
                _listData.value = db.homeDao().getAllData()
            })
            return
        }

        // New member class data
        val tempList = db.memberCategoryDao().getAllData().filter {
            it.title == (SeokwangYouthApplication.context?.getString(R.string.new_member)+
                            SeokwangYouthApplication.context?.getString(R.string.className))
        }
        Logger.d("requestDB tempList : $tempList")
        if(tempList.isNotEmpty()) newMemberClassData = tempList[0]

        // Notice Data
        noticeList = mutableListOf()
        noticeList.add(HomeEntity(
            HEADER_NOTICE_ID, Constants.ITEM_TYPE_HEADER,
            SeokwangYouthApplication.context!!.getString(R.string.notice),
            null, null, 0,
            Util.getUUID(), Util.getTimestamp()))
        // Attendance Data
        attendanceList = mutableListOf()
        attendanceList.add(HomeEntity(
            HEADER_LAST_WEEK_ATTENDANCE_ID, Constants.ITEM_TYPE_HEADER,
            SeokwangYouthApplication.context!!.getString(R.string.last_week_attendance),
            null, null, 0,
            Util.getUUID(), Util.getTimestamp()))
        isReadyForUseFirebase = false

        FirebaseManager.instance.registerHomeDB(object: FirebaseManager.IFirebaseCallback{
            override fun onValueDataChange(snapshot: DataSnapshot) {
                isReadyForUseFirebase = true
                Logger.d("onDataChange snapshot : $snapshot / snapshot.childrenCount : ${snapshot.childrenCount}")
                if(snapshot.childrenCount>0L) {
                    val entity = snapshot.getValue(HomeEntity::class.java)
                    Logger.d("onDataChange entity : $entity")
                    if(entity!=null) {
                        Thread(Runnable {
                            db.homeDao().insertData(entity)
                        }).start()
                        val tempArr = entity.title!!.split("_")
                        //entity.title = tempArr[1]
                        if(tempArr[0] == FirebaseConstants.PREFIX_NOTICE) {
                            if(!noticeList.contains(entity)) noticeList.add(entity)
                        }
                        else if(tempArr[0] == FirebaseConstants.PREFIX_CHECK) {
                            if(!attendanceList.contains(entity)) attendanceList.add(entity)
                        }
                    }
                    Handler(Looper.getMainLooper()).post(Runnable {
                        val mutableList = mutableListOf<HomeEntity>()
                        mutableList.addAll(noticeList)
                        mutableList.addAll(allEventList)
                        mutableList.addAll(attendanceList)
                        _listData.value = mutableList.toList()
                    })
                }
            }

            override fun onValueCancelled(error: DatabaseError) {
                isReadyForUseFirebase = true
                Logger.w("onCancelled error : $error")
                Handler(Looper.getMainLooper()).post(Runnable {
                    _listData.value = db.homeDao().getAllData()
                })
            }

            override fun onEventChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                isReadyForUseFirebase = true
                Logger.d("onChildAdded snapshot : $snapshot / previousChildName : $previousChildName")
            }

            override fun onEventChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                isReadyForUseFirebase = true
                Logger.d("onChildChanged snapshot : $snapshot / previousChildName : $previousChildName")
                if(snapshot.key == null || snapshot.key?.isEmpty() == true) return
                val homeEntity = snapshot.getValue(HomeEntity::class.java)!!
                val tempArr = homeEntity.title!!.split("_")
                homeEntity.title = tempArr[1]
                var foundedIndex = -1
                var mutableList = _listData.value?.toMutableList()
                mutableList?.forEachIndexed { index, entity ->
                    if(foundedIndex!=-1) return@forEachIndexed
                    if(homeEntity.uuid == entity.uuid) {
                        foundedIndex = index
                    }
                }
                if(foundedIndex == -1) return
                //firebaseDB.child(snapshot.key!!).child(FirebaseConstants.KEY_TIMESTAMP).setValue(Util.getTimestamp())
                mutableList?.removeAt(foundedIndex)
                mutableList?.add(foundedIndex, homeEntity)
                _listData.postValue(mutableList?.toList())
            }

            override fun onEventChildRemoved(snapshot: DataSnapshot) {
                isReadyForUseFirebase = true
                Logger.d("onChildRemoved snapshot : $snapshot")
                //requestCurrentData()
            }

            override fun onEventChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                isReadyForUseFirebase = true
            }

            override fun onEventCancelled(error: DatabaseError) {
                isReadyForUseFirebase = true
            }

        })

        getEventData()

        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            Logger.d("Firebase check isReadyForUseFirebase : $isReadyForUseFirebase")
            if(isReadyForUseFirebase) return@Runnable
            requestCurrentData()
        }, Constants.NETWORK_CHECK_DELAY)
    }

    private fun getEventData() {
        allEventList = mutableListOf()

        // This week events list
        val curCalendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
        }
        val eventList = db.calendarDao().getAllDataOrderByDate().filter { entity ->
            val dateArr = entity.date.split(".")
            val tCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, dateArr[0].toInt())
                set(Calendar.MONTH, dateArr[1].toInt()-1)
                set(Calendar.DATE, dateArr[2].toInt())
            }
            //Logger.d("curCalendar week : ${curCalendar.get(Calendar.WEEK_OF_YEAR)} / tCalendar week : ${tCalendar.get(Calendar.WEEK_OF_YEAR)}")
            curCalendar.get(Calendar.WEEK_OF_YEAR) == tCalendar.get(Calendar.WEEK_OF_YEAR) ||
                    curCalendar.get(Calendar.WEEK_OF_YEAR)+1 == tCalendar.get(Calendar.WEEK_OF_YEAR)
        }
        val birthDayList = db.memberInfoDao().getAllDataOrderByBirth().filter { member ->
            val dateArr = member.birth.split(".")
            val tCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, dateArr[0].toInt())
                set(Calendar.MONTH, dateArr[1].toInt()-1)
                set(Calendar.DATE, dateArr[2].toInt())
            }
            //Logger.d("curBirth date : ${curCalendar.get(Calendar.YEAR)}.${curCalendar.get(Calendar.MONTH)+1}.${curCalendar.get(Calendar.DATE)} / week : ${curCalendar.get(Calendar.WEEK_OF_YEAR)}")
            //Logger.d("tBirth date : ${tCalendar.get(Calendar.YEAR)}.${tCalendar.get(Calendar.MONTH)+1}.${tCalendar.get(Calendar.DATE)} / week : ${tCalendar.get(Calendar.WEEK_OF_YEAR)} / name : ${member.name}")
            curCalendar.get(Calendar.WEEK_OF_YEAR)-1 == tCalendar.get(Calendar.WEEK_OF_YEAR) ||
                    curCalendar.get(Calendar.WEEK_OF_YEAR) == tCalendar.get(Calendar.WEEK_OF_YEAR)
        }
        if(eventList.isNotEmpty() || birthDayList.isNotEmpty()) {
            allEventList.add(HomeEntity(
                HEADER_WEEK_EVENT_ID, Constants.ITEM_TYPE_HEADER, SeokwangYouthApplication.context!!.getString(R.string.this_week_events),
                null, null, 0,
                Util.getUUID(), Util.getTimestamp()))
        }
        var count = HEADER_WEEK_EVENT_ID
        if(eventList.isNotEmpty()) {
            eventList.forEach { event ->
                allEventList.add(HomeEntity(
                    count++, Constants.ITEM_TYPE_NORMAL,
                    Constants.SCHEDULE_TYPE_EVENT.toString() + "^" + event.title + "^" + event.date,
                    FirebaseConstants.PREFIX_EVENT, null, 0,
                    Util.getUUID(), Util.getTimestamp()))
            }
        }
        if(birthDayList.isNotEmpty()) {
            birthDayList.forEach { member ->
                allEventList.add(HomeEntity(
                    count++, Constants.ITEM_TYPE_NORMAL,
                    Constants.SCHEDULE_TYPE_BIRTHDAY.toString() + "^" + member.name + "^" + member.birth,
                    FirebaseConstants.PREFIX_EVENT, null, 0,
                    Util.getUUID(), Util.getTimestamp()))
            }
        }

        // New member list
        val newMemberList = db.memberInfoDao().getAllDataOrderByName().filter { entity ->
            entity.className.equals(
                SeokwangYouthApplication.context?.getString(R.string.new_member))
        }
        if(newMemberList.isNotEmpty()) {
            allEventList.add(HomeEntity(
                HEADER_NEW_MEMBER_LIST_ID, Constants.ITEM_TYPE_HEADER, SeokwangYouthApplication.context!!.getString(R.string.new_member),
                null, null, 0,
                Util.getUUID(), Util.getTimestamp()))
            count = HEADER_NEW_MEMBER_LIST_ID
            newMemberList.forEach { newMember ->
                allEventList.add(HomeEntity(
                    count++, Constants.ITEM_TYPE_NORMAL, newMember.name,
                    Constants.NEW_MEMBER_VALUE, null, 0,
                    Util.getUUID(), Util.getTimestamp()))
            }
        }
    }

    fun add(entity: HomeEntity) = GlobalScope.launch(Dispatchers.IO) {
        //list.add(entity)
        db.homeDao().insertData(entity)
        _listData.value = db.homeDao().getAllData()
    }

    fun remove(entity: HomeEntity) = GlobalScope.launch(Dispatchers.IO) {
        //list.remove(entity)
        db.homeDao().deleteDataById(entity.id!!)
        _listData.value = db.homeDao().getAllData()
    }

    fun requestCurrentData() {
        Logger.d("requestCurrentData")
        GlobalScope.launch(Dispatchers.IO) {
            // Notice Data
            noticeList = mutableListOf()
            noticeList.add(HomeEntity(
                HEADER_NOTICE_ID, Constants.ITEM_TYPE_HEADER,
                SeokwangYouthApplication.context!!.getString(R.string.notice),
                null, null, 0,
                Util.getUUID(), Util.getTimestamp()))
            // Attendance Data
            attendanceList = mutableListOf()
            attendanceList.add(HomeEntity(
                HEADER_LAST_WEEK_ATTENDANCE_ID, Constants.ITEM_TYPE_HEADER,
                SeokwangYouthApplication.context!!.getString(R.string.last_week_attendance),
                null, null, 0,
                Util.getUUID(), Util.getTimestamp()))
            db.homeDao().getAllData().forEach { entity ->
                Logger.d("requestCurrentData entity : $entity")
                val tempArr = entity.title!!.split("_")
                //entity.title = tempArr[1]
                if(tempArr[0] == FirebaseConstants.PREFIX_NOTICE
                    && !noticeList.contains(entity)) {
                    noticeList.add(entity)
                }
                else if(tempArr[0] == FirebaseConstants.PREFIX_CHECK
                    && !attendanceList.contains(entity)) {
                    attendanceList.add(entity)
                }
            }
            getEventData()
            Handler(Looper.getMainLooper()).post(Runnable {
                val mutableList = mutableListOf<HomeEntity>()
                mutableList.addAll(noticeList)
                mutableList.addAll(allEventList)
                mutableList.addAll(attendanceList)
                _listData.value = mutableList.toList()
            })
        }
    }

    fun getNewMemberClassData(): SimpleEntity? = newMemberClassData

    /*
    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text
     */
}