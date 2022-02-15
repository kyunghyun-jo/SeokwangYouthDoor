package kr.co.skchurch.seokwangyouthdoor.ui.timetable

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
import kr.co.skchurch.seokwangyouthdoor.SeokwangYouthApplication
import kr.co.skchurch.seokwangyouthdoor.data.AppDatabase
import kr.co.skchurch.seokwangyouthdoor.data.Constants
import kr.co.skchurch.seokwangyouthdoor.data.FirebaseConstants
import kr.co.skchurch.seokwangyouthdoor.data.FirebaseManager
import kr.co.skchurch.seokwangyouthdoor.data.entities.TimetableEntity
import kr.co.skchurch.seokwangyouthdoor.utils.Util

class TimetableViewModel() : ViewModel() {

    companion object {
        private val TAG = TimetableViewModel::class.java.simpleName
    }
    
    private val _listData = MutableLiveData<List<TimetableEntity>>()
    val listData: LiveData<List<TimetableEntity>> = _listData
    private var db: AppDatabase = AppDatabase.getDatabase()
    //var list: List<TimetableEntity>? = null
    private var mutableList: MutableList<TimetableEntity> = mutableListOf()

    init {
        GlobalScope.launch(Dispatchers.IO) {
            requestDB()
        }
    }

    private fun requestDB() {
        mutableList = mutableListOf()
        if(FirebaseManager.instance.getCurrentUserId() == FirebaseConstants.EMPTY_USER) {
            //Toast.makeText(this, "로그인이 되어있지 않습니다.", Toast.LENGTH_SHORT).show()
            //finish()
            mutableList.addAll(db.timetableDao().getAllData())
            Handler(Looper.getMainLooper()).post(Runnable {
                _listData.value = mutableList.toList()
            })
            return
        }
        if(!Util.isOnline(SeokwangYouthApplication.context!!)) {
            Handler(Looper.getMainLooper()).post(Runnable {
                requestCurrentData()
            })
            return
        }
        Thread(Runnable {
            db.timetableDao().deleteAllData()
        }).start()
        FirebaseManager.instance.registerTimetableDB(object: FirebaseManager.IFirebaseCallback{
            override fun onValueDataChange(snapshot: DataSnapshot) {
                if(snapshot.childrenCount>0L) {
                    val entity = snapshot.getValue(TimetableEntity::class.java)
                    Logger.d("onDataChange entity : $entity")
                    if(entity!=null && !mutableList.contains(entity)) {
                        mutableList.add(entity)
                        Thread(Runnable {
                            db.timetableDao().insertData(entity)
                        }).start()
                        mutableList.sortBy { it.id }
                        _listData.value = mutableList.toList()
                    }
                }
            }

            override fun onValueCancelled(error: DatabaseError) {}

            override fun onEventChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onEventChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("onChildChanged snapshot : $snapshot / previousChildName : $previousChildName")
                if(snapshot.key == null || snapshot.key?.isEmpty() == true) return
                val timetableEntity = snapshot.getValue(TimetableEntity::class.java)!!
                var foundedIndex = -1
                mutableList.forEachIndexed { index, entity ->
                    if(foundedIndex!=-1) return@forEachIndexed
                    if(entity.uuid == timetableEntity.uuid) {
                        foundedIndex = index
                    }
                }
                if(foundedIndex == -1) return
                Logger.d("onChildChanged mutableList.size : ${mutableList.size} / foundedIndex : $foundedIndex")
                //firebaseDB.child(snapshot.key!!).child(FirebaseConstants.KEY_TIMESTAMP).setValue(Util.getTimestamp())
                Thread(Runnable {
                    db.timetableDao().insertData(timetableEntity)
                }).start()
                mutableList.removeAt(foundedIndex)
                mutableList.add(foundedIndex, timetableEntity)
                _listData.postValue(mutableList.toList())
            }

            override fun onEventChildRemoved(snapshot: DataSnapshot) {}

            override fun onEventChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onEventCancelled(error: DatabaseError) {}

        })
        /*
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            Logger.d("Firebase check isReadyForUseFirebase : $isReadyForUseFirebase")
            if(isReadyForUseFirebase) return@Runnable
            requestCurrentData()
        }, Constants.NETWORK_CHECK_DELAY)
         */
    }

    fun requestCurrentData() {
        GlobalScope.launch(Dispatchers.IO) {
            mutableList.clear()
            mutableList.addAll(db.timetableDao().getAllData())
            Handler(Looper.getMainLooper()).post(Runnable {
                _listData.value = mutableList.toList()
            })
        }
    }

    fun add(entity: TimetableEntity) = GlobalScope.launch(Dispatchers.IO) {
        //list.add(entity)
        db.timetableDao().insertData(entity)
        Handler(Looper.getMainLooper()).post(Runnable {
            _listData.value = db.timetableDao().getAllData()
        })
    }

    fun remove(entity: TimetableEntity) = GlobalScope.launch(Dispatchers.IO) {
        //list.remove(entity)
        db.timetableDao().deleteDataByUUID(entity.uuid)
        Handler(Looper.getMainLooper()).post(Runnable {
            _listData.value = db.timetableDao().getAllData()
        })
    }
}