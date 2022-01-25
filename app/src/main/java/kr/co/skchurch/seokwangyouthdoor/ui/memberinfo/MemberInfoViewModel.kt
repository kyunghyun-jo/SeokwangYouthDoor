package kr.co.skchurch.seokwangyouthdoor.ui.memberinfo

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
import kr.co.skchurch.seokwangyouthdoor.data.entities.MemberInfoEntity
import kr.co.skchurch.seokwangyouthdoor.utils.Util

class MemberInfoViewModel : ViewModel() {

    companion object {
        private val TAG = MemberInfoViewModel::class.java.simpleName
    }

    private val _listData = MutableLiveData<List<MemberInfoEntity>>()
    val listData: LiveData<List<MemberInfoEntity>> = _listData
    private var db: AppDatabase = AppDatabase.getDatabase()
    private var mutableList = mutableListOf<MemberInfoEntity>()
    var targetClassName: String? = null

    init {
        GlobalScope.launch(Dispatchers.IO) {
            requestDB()
        }
    }

    private var isReadyForUseFirebase = false
    private fun requestDB() {
        mutableList = mutableListOf()
        isReadyForUseFirebase = false
        Logger.d("requestDB getCurrentUserId : ${FirebaseManager.instance.getCurrentUserId()}")
        if(FirebaseManager.instance.getCurrentUserId() == FirebaseConstants.EMPTY_USER) {
            //Toast.makeText(this, "로그인이 되어있지 않습니다.", Toast.LENGTH_SHORT).show()
            //finish()
            mutableList.addAll(db.memberInfoDao().getAllData())
            Handler(Looper.getMainLooper()).post(Runnable {
                _listData.value = mutableList.toList()
            })
            return
        }
        FirebaseManager.instance.registerMemberInfoDB(object: FirebaseManager.IFirebaseCallback{
            override fun onValueDataChange(snapshot: DataSnapshot) {
                isReadyForUseFirebase = true
                Logger.d("onValueDataChange snapshot : $snapshot\nsnapshot.childrenCount : ${snapshot.childrenCount}")
                if(snapshot.childrenCount > 0L) {
                    val entity = snapshot.getValue(MemberInfoEntity::class.java)
                    Logger.d("onValueDataChange entity : $entity")
                    if(entity!=null) {
                        Thread(Runnable {
                            if(entity.detailInfo!!.age == null ||
                                entity.detailInfo!!.age == 0) {
                                entity.detailInfo!!.age = Util.calculateAge(entity.birth)
                                Logger.d("calculate age newMember : $entity")
                            }
                            db.memberInfoDao().insertData(entity)
                        }).start()
                    }
                    if(targetClassName!=null) requestMemberDataByClassName(targetClassName!!)
                    //_listData.value = mutableList.toList()
                }
            }

            override fun onValueCancelled(error: DatabaseError) {
                isReadyForUseFirebase = true
            }

            override fun onEventChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                isReadyForUseFirebase = true
                Logger.d("onChildAdded snapshot : $snapshot\npreviousChildName : $previousChildName")
            }

            override fun onEventChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                isReadyForUseFirebase = true
                Logger.d("onChildChanged snapshot : $snapshot / previousChildName : $previousChildName")
                if(snapshot.key == null || snapshot.key?.isEmpty() == true) return
                val memberInfoEntity = snapshot.getValue(MemberInfoEntity::class.java)!!
                var foundedIndex = -1
                mutableList.forEachIndexed { index, entity ->
                    if(foundedIndex!=-1) return@forEachIndexed
                    if(entity.uuid == memberInfoEntity.uuid) {
                        foundedIndex = index
                    }
                }
                Logger.d("onChildChanged foundedIndex : $foundedIndex")
                if(foundedIndex == -1) return
                //firebaseDB.child(snapshot.key!!).child(FirebaseConstants.KEY_TIMESTAMP).setValue(Util.getTimestamp())
                mutableList.removeAt(foundedIndex)
                mutableList.add(foundedIndex, memberInfoEntity)
                Thread(Runnable {
                    Logger.d("onChildChanged insertData : $memberInfoEntity")
                    db.memberInfoDao().insertData(memberInfoEntity)
                }).start()
                Handler(Looper.getMainLooper()).post {
                    _listData.postValue(mutableList.toList())
                }
            }

            override fun onEventChildRemoved(snapshot: DataSnapshot) {
                isReadyForUseFirebase = true
                Logger.d("onChildRemoved snapshot : $snapshot")
                if(snapshot.key == null || snapshot.key?.isEmpty() == true) return
                val memberInfoEntity = snapshot.getValue(MemberInfoEntity::class.java)!!
                Thread(Runnable {
                    if(memberInfoEntity.uuid == null) db.memberInfoDao().deleteDataById(memberInfoEntity.id!!)
                    else db.memberInfoDao().deleteDataByUUID(memberInfoEntity.uuid)
                }).start()
                requestCurrentData()
            }

            override fun onEventChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                isReadyForUseFirebase = true
            }

            override fun onEventCancelled(error: DatabaseError) {
                isReadyForUseFirebase = true
            }

        })

        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            Logger.d("Firebase check isReadyForUseFirebase : $isReadyForUseFirebase / targetClassName : $targetClassName")
            if(isReadyForUseFirebase || targetClassName==null) return@Runnable
            requestMemberDataByClassName(targetClassName.orEmpty())
        }, Constants.NETWORK_CHECK_DELAY)
    }

    fun add(entity: MemberInfoEntity) = GlobalScope.launch(Dispatchers.IO) {
        //list.add(entity)
        db.memberInfoDao().insertData(entity)
        Handler(Looper.getMainLooper()).post(Runnable {
            _listData.value = db.memberInfoDao().getAllData()
        })
    }

    fun remove(entity: MemberInfoEntity) = GlobalScope.launch(Dispatchers.IO) {
        //list.remove(entity)
        db.memberInfoDao().deleteDataByUUID(entity.uuid)
        Handler(Looper.getMainLooper()).post(Runnable {
            _listData.value = db.memberInfoDao().getAllData()
        })
    }

    private fun requestMemberDataByClassName(className: String) = GlobalScope.launch(Dispatchers.IO) {
        val unitClassName = SeokwangYouthApplication.context!!.getString(R.string.className)
        val targetClassName =
            if(className.isNotEmpty() && className.last().toString() == unitClassName)
                className.substring(0, className.length-unitClassName.length)
            else className
        Logger.d("requestMemberDataByClassName className : $className / targetClassName : $targetClassName")
        var selectedType = -1
        for(i in MemberType.values().indices) {
            if(targetClassName == MemberType.values()[i].value) {
                selectedType = MemberType.values()[i].id
                break
            }
        }
        Logger.d("requestMemberDataByClassName selectedType : $selectedType")
        val filteredList: List<MemberInfoEntity>
        if(selectedType == -1) {
            filteredList =
                db.memberInfoDao().getAllData().filter {
                    val classArr = it.className?.split(",")
                    classArr?.contains(targetClassName) ?: false
                }
            Logger.d("requestMemberDataByClassName 111 list.size : ${filteredList?.size}")
        }
        else {
            if(selectedType == Constants.MEMBER_TYPE_TEACHER) {
                filteredList =
                    db.memberInfoDao().getAllData().filter {
                        it.type == Constants.MEMBER_TYPE_CHIEF_TEACHER
                                || it.type == Constants.MEMBER_TYPE_WORSHIP_TEAM_LEADER
                                || it.type == Constants.MEMBER_TYPE_TEACHER
                    }
                Logger.d("requestMemberDataByClassName 222 list.size : ${filteredList?.size}")
            }
            else {
                filteredList =
                    db.memberInfoDao().getAllData().filter {
                        it.type == selectedType
                    }
                Logger.d("requestMemberDataByClassName 333 list.size : ${filteredList?.size}")
            }
        }
        mutableList.clear()
        mutableList.addAll(filteredList)
        Handler(Looper.getMainLooper()).post(Runnable {
            _listData.value = mutableList.toList()
        })
    }

    fun requestCurrentData() {
        requestMemberDataByClassName(targetClassName.orEmpty())
    }

    /*
    fun requestCurrentData() {
        GlobalScope.launch(Dispatchers.IO) {
            mutableList.clear()
            mutableList.addAll(db.memberInfoDao().getAllData())
        }
        Handler(Looper.getMainLooper()).post(Runnable {
            _listData.value = mutableList.toList()
        })
    }
     */

}