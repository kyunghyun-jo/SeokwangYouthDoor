package kr.co.skchurch.seokwangyouthdoor.ui.memberinfo

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kr.co.skchurch.seokwangyouthdoor.R
import kr.co.skchurch.seokwangyouthdoor.SeokwangYouthApplication
import kr.co.skchurch.seokwangyouthdoor.data.AppDatabase
import kr.co.skchurch.seokwangyouthdoor.data.Constants
import kr.co.skchurch.seokwangyouthdoor.data.FirebaseConstants
import kr.co.skchurch.seokwangyouthdoor.data.FirebaseManager
import kr.co.skchurch.seokwangyouthdoor.data.entities.MemberInfoEntity
import kr.co.skchurch.seokwangyouthdoor.data.entities.SimpleEntity
import kr.co.skchurch.seokwangyouthdoor.utils.Util

class MemberCategoryViewModel() : ViewModel() {

    companion object {
        private val TAG = MemberCategoryViewModel::class.java.simpleName
    }
    
    private val _listData = MutableLiveData<List<SimpleEntity>>()
    val listData: LiveData<List<SimpleEntity>> = _listData
    private var db: AppDatabase = AppDatabase.getDatabase()
    //var list: List<SimpleEntity>? = null
    private var mutableList: MutableList<SimpleEntity> = mutableListOf()

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
            mutableList.addAll(db.memberCategoryDao().getAllData())
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
            db.memberCategoryDao().deleteAllData()
        }).start()
        FirebaseManager.instance.registerMemberCategoryDB(object: FirebaseManager.IFirebaseCallback{
            override fun onValueDataChange(snapshot: DataSnapshot) {
                if(snapshot.childrenCount > 0L) {
                    val fixedNames = arrayListOf<String>(
                        SeokwangYouthApplication.context!!.getString(R.string.paster),
                        SeokwangYouthApplication.context!!.getString(R.string.teachers),
                        SeokwangYouthApplication.context!!.getString(R.string.worship_team)
                    )
                    val entity = snapshot.getValue(SimpleEntity::class.java)
                    Logger.d("onDataChange entity : $entity")
                    if(entity!=null) {
                        if(!fixedNames.contains(entity.title)) entity.title = entity.title+SeokwangYouthApplication.context!!.getString(R.string.className)
                        if(!mutableList.contains(entity)) {
                            mutableList.add(entity)
                            Thread(Runnable {
                                db.memberCategoryDao().insertData(entity)
                            }).start()
                            //db.memberCategoryDao().insertData(entity)
                            _listData.value = mutableList.toList()
                        }
                    }
                }
            }

            override fun onValueCancelled(error: DatabaseError) {}

            override fun onEventChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onEventChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("onChildChanged snapshot : $snapshot / previousChildName : $previousChildName")
                if(snapshot.key == null || snapshot.key?.isEmpty() == true) return
                val simpleEntity = snapshot.getValue(SimpleEntity::class.java)!!
                if(simpleEntity!=null && !mutableList.contains(simpleEntity)) {
                    Thread(Runnable {
                        db.memberCategoryDao().insertData(simpleEntity)
                    }).start()
                    var foundedIndex = -1
                    mutableList.forEachIndexed { index, entity ->
                        if(foundedIndex!=-1) return@forEachIndexed
                        if(entity.uuid == simpleEntity.uuid) {
                            foundedIndex = index
                        }
                    }
                    if(foundedIndex == -1) return
                    //firebaseDB.child(snapshot.key!!).child(FirebaseConstants.KEY_TIMESTAMP).setValue(Util.getTimestamp())
                    mutableList.removeAt(foundedIndex)
                    mutableList.add(foundedIndex, simpleEntity)
                    _listData.postValue(mutableList.toList())
                }
            }

            override fun onEventChildRemoved(snapshot: DataSnapshot) {}

            override fun onEventChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onEventCancelled(error: DatabaseError) {}

        })

        FirebaseManager.instance.registerMemberInfoDB(object: FirebaseManager.IFirebaseCallback{
            override fun onValueDataChange(snapshot: DataSnapshot) {}

            override fun onValueCancelled(error: DatabaseError) {}

            override fun onEventChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onEventChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("MemberInfo onChildChanged snapshot : $snapshot / previousChildName : $previousChildName")
                if(snapshot.key == null || snapshot.key?.isEmpty() == true) return
                val memberInfoEntity = snapshot.getValue(MemberInfoEntity::class.java)!!
                Logger.d("MemberInfo memberInfoEntity : $memberInfoEntity")
                Thread(Runnable {
                    db.memberInfoDao().insertData(memberInfoEntity)
                }).start()
                if(mutableList.size == 0) return
                if(memberInfoEntity.type == Constants.MEMBER_TYPE_PASTER) {
                    val pasterEntity = mutableList[0].copy()
                    pasterEntity.imageUrl = memberInfoEntity.imageUrl
                    mutableList.removeAt(0)
                    mutableList.add(0, pasterEntity)
                    Thread(Runnable {
                        db.memberCategoryDao().insertData(pasterEntity)
                    }).start()
                    _listData.value = mutableList.toList()
                }
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
        Logger.d("requestCurrentData")
        GlobalScope.launch(Dispatchers.IO) {
            mutableList.clear()
            mutableList.addAll(db.memberCategoryDao().getAllData())
            Handler(Looper.getMainLooper()).post(Runnable {
                _listData.value = mutableList.toList()
            })
        }
    }

    fun add(entity: SimpleEntity) = GlobalScope.launch(Dispatchers.IO) {
        //list.add(entity)
        db.memberCategoryDao().insertData(entity)
        Handler(Looper.getMainLooper()).post(Runnable {
            _listData.value = db.memberCategoryDao().getAllData()
        })
    }

    fun remove(entity: SimpleEntity) = GlobalScope.launch(Dispatchers.IO) {
        //list.remove(entity)
        db.memberCategoryDao().deleteDataById(entity.id!!)
        Handler(Looper.getMainLooper()).post(Runnable {
            _listData.value = db.memberCategoryDao().getAllData()
        })
    }
}