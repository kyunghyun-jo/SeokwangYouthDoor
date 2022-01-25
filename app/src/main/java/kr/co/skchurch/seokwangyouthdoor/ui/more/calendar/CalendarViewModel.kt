package kr.co.skchurch.seokwangyouthdoor.ui.more.calendar

import android.os.Handler
import android.os.Looper
import com.orhanobut.logger.Logger
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kr.co.skchurch.seokwangyouthdoor.R
import kr.co.skchurch.seokwangyouthdoor.SeokwangYouthApplication
import kr.co.skchurch.seokwangyouthdoor.data.*
import kr.co.skchurch.seokwangyouthdoor.data.entities.CalendarEntity
import kr.co.skchurch.seokwangyouthdoor.utils.Util
import kr.co.skchurch.seokwangyouthdoor.utils.getBirthDate

class CalendarViewModel : ViewModel() {

    companion object {
        private val TAG = CalendarViewModel::class.java.simpleName
    }

    private val _listData = MutableLiveData<List<CalendarEntity>>()
    val listData: LiveData<List<CalendarEntity>> = _listData
    private var db: AppDatabase = AppDatabase.getDatabase()
    //var list: List<CalendarEntity>? = null
    var mutableList = mutableListOf<CalendarEntity>()
    //private lateinit var firebaseDB: DatabaseReference
    private var selectedDate: String? = null

    init {
        GlobalScope.launch(Dispatchers.IO) {
            requestDB()
        }
    }

    private var callback: ICalendarEventCallback? = null
    fun setCallback(_callback: ICalendarEventCallback) {
        callback = _callback
    }

    private fun requestDB() {
        mutableList = mutableListOf()
        if(FirebaseManager.instance.getCurrentUserId() == FirebaseConstants.EMPTY_USER) {
            //Toast.makeText(this, "로그인이 되어있지 않습니다.", Toast.LENGTH_SHORT).show()
            //finish()
            mutableList.addAll(db.calendarDao().getAllData())
            //Handler(Looper.getMainLooper()).post(Runnable {
            //    _listData.value = mutableList.toList()
            //})
            return
        }
        FirebaseManager.instance.registerCalendarDB(object: FirebaseManager.IFirebaseCallback{
            override fun onValueDataChange(snapshot: DataSnapshot) {
                if(snapshot.childrenCount > 0L) {
                    val entity = snapshot.getValue(CalendarEntity::class.java)
                    //Logger.d("onDataChange entity : $entity")
                    if(entity!=null) {
                        Logger.d("onDataChange date : ${entity.date} / selectedDate : $selectedDate")
                        if(!mutableList.contains(entity)) {
                            Thread(Runnable {
                                db.calendarDao().insertData(entity)
                            }).start()
                        }
                        if(selectedDate!=null && entity.date == selectedDate) {
                            if(!mutableList.contains(entity)) {
                                mutableList.add(entity)
                                //_listData.value = mutableList.toList()
                            }
                        }
                    }
                }
            }

            override fun onValueCancelled(error: DatabaseError) {}

            override fun onEventChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                //TODO("Not yet implemented")
            }

            override fun onEventChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("onChildChanged snapshot : $snapshot / previousChildName : $previousChildName")
                if(snapshot.key == null || snapshot.key?.isEmpty() == true) return
                val calendarEntity = snapshot.getValue(CalendarEntity::class.java)!!
                var foundedIndex = -1
                mutableList.forEachIndexed { index, entity ->
                    if(foundedIndex!=-1) return@forEachIndexed
                    if(entity.uuid == calendarEntity.uuid) {
                        foundedIndex = index
                    }
                }
                if(foundedIndex == -1) return
                //firebaseDB.child(snapshot.key!!).child(FirebaseConstants.KEY_TIMESTAMP).setValue(Util.getTimestamp())
                mutableList.removeAt(foundedIndex)
                mutableList.add(foundedIndex, calendarEntity)
                Handler(Looper.getMainLooper()).post {
                    _listData.postValue(mutableList.toList())
                }
                Thread(Runnable {
                    db.calendarDao().insertData(calendarEntity)
                }).start()
            }

            override fun onEventChildRemoved(snapshot: DataSnapshot) {
                Logger.d("onChildRemoved snapshot : $snapshot")
                if(snapshot.key == null || snapshot.key?.isEmpty() == true) return
                val calendarEntity = snapshot.getValue(CalendarEntity::class.java)!!
                Thread(Runnable {
                    db.calendarDao().deleteDataById(calendarEntity.id!!)
                }).start()
                callback?.onChildRemoved()
            }

            override fun onEventChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onEventCancelled(error: DatabaseError) {}

        })
    }

    fun requestScheduleByDate(date: String) = GlobalScope.launch(Dispatchers.IO) {
        selectedDate = date
        mutableList.clear()
        //list = db.calendarDao().getDataByDate(date)
        mutableList.addAll(db.calendarDao().getDataByDate(date))
        Logger.d("requestScheduleByDate before list : $mutableList")
        val memberInfoList = db.memberInfoDao().getAllData()
        Logger.d("requestScheduleByDate memberInfoList : $memberInfoList")
        val today = date.getBirthDate()
        if(memberInfoList.isNotEmpty()) {
            val birthCalendarList: MutableList<CalendarEntity> = mutableListOf()
            var lastId = memberInfoList.last().id!!
            memberInfoList.forEach { member ->
                val birthDay = member.birth.getBirthDate()
                if(today == birthDay) {
                    birthCalendarList.add(
                        CalendarEntity(
                            ++lastId,
                            member.name+" "+SeokwangYouthApplication.context!!.getString(R.string.birthday),
                            null,
                            date,
                            Constants.SCHEDULE_TYPE_BIRTHDAY,
                        Util.getUUID(),
                        Util.getTimestamp()))
                }
            }
            Logger.d("requestScheduleByDate birthCalendarList : $birthCalendarList")
            if(birthCalendarList.isNotEmpty()) {
                mutableList.addAll(birthCalendarList)
                //birthCalendarList.forEach {
                //    list.add(it)
                //}
            }
            Logger.d("requestScheduleByDate after list : $mutableList")
        }
        Handler(Looper.getMainLooper()).post(Runnable {
            _listData.value = mutableList.toList()
        })
    }

    fun add(entity: CalendarEntity) = GlobalScope.launch(Dispatchers.IO) {
        //list.add(entity)
        db.calendarDao().insertData(entity)
        Handler(Looper.getMainLooper()).post(Runnable {
            _listData.value = db.calendarDao().getAllData()
        })
    }

    fun remove(entity: CalendarEntity) = GlobalScope.launch(Dispatchers.IO) {
        //list.remove(entity)
        db.calendarDao().deleteDataByUUID(entity.uuid)
        Handler(Looper.getMainLooper()).post(Runnable {
            _listData.value = db.calendarDao().getAllData()
        })
    }

    interface ICalendarEventCallback {
        fun onChildRemoved()
    }
}