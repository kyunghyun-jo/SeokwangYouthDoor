package kr.co.skchurch.seokwangyouthdoor.ui.more

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
import kr.co.skchurch.seokwangyouthdoor.SeokwangYouthApplication
import kr.co.skchurch.seokwangyouthdoor.data.AppDatabase
import kr.co.skchurch.seokwangyouthdoor.data.Constants
import kr.co.skchurch.seokwangyouthdoor.data.FirebaseManager
import kr.co.skchurch.seokwangyouthdoor.data.entities.MemberInfoEntity
import kr.co.skchurch.seokwangyouthdoor.data.generateTestMemberInfoData
import kr.co.skchurch.seokwangyouthdoor.utils.Util

class MoreViewModel : ViewModel() {

    private val _listData = MutableLiveData<List<MemberInfoEntity>>()
    val listData: LiveData<List<MemberInfoEntity>> = _listData
    private var db: AppDatabase = AppDatabase.getDatabase()
    var list: List<MemberInfoEntity>? = null
    var mutableList: List<MemberInfoEntity> = arrayListOf()

    init {
        GlobalScope.launch(Dispatchers.IO) {
            list = db.memberInfoDao().getAllDataOrderByName()
            if(Constants.IS_TEST_MODE) {
                if (list!!.isEmpty()) {
                    list = generateTestMemberInfoData()
                }
            }
            val freeBoardList = db.freeBoardDao().getAllData()
            SeokwangYouthApplication.dbFreeBoardSize = freeBoardList.size
            SeokwangYouthApplication.getMyProfile {
                val classNameArr = it?.className!!.split(",")
                classNameArr.forEach { className ->
                    val boardList = db.boardDao().getDataByClassName(className)
                    SeokwangYouthApplication.dbBoardSizeMap[className] = boardList.size
                    FirebaseManager.instance.requestClassBoardDataLength(className, object: FirebaseManager.IFirebaseCallback {
                        override fun onValueDataChange(snapshot: DataSnapshot) {
                            FirebaseManager.instance.requestFreeBoardDataLength(object: FirebaseManager.IFirebaseCallback {
                                override fun onValueDataChange(snapshot: DataSnapshot) {}

                                override fun onValueCancelled(error: DatabaseError) {}

                                override fun onEventChildAdded(
                                    snapshot: DataSnapshot,
                                    previousChildName: String?
                                ) {}

                                override fun onEventChildChanged(
                                    snapshot: DataSnapshot,
                                    previousChildName: String?
                                ) {}

                                override fun onEventChildRemoved(snapshot: DataSnapshot) {}

                                override fun onEventChildMoved(
                                    snapshot: DataSnapshot,
                                    previousChildName: String?
                                ) {}

                                override fun onEventCancelled(error: DatabaseError) {}

                            })
                        }

                        override fun onValueCancelled(error: DatabaseError) {}

                        override fun onEventChildAdded(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {}

                        override fun onEventChildChanged(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {}

                        override fun onEventChildRemoved(snapshot: DataSnapshot) {}

                        override fun onEventChildMoved(
                            snapshot: DataSnapshot,
                            previousChildName: String?
                        ) {}

                        override fun onEventCancelled(error: DatabaseError) {}

                    })

                }
            }
        }
    }

    fun isBoardNoti(className: String): Boolean {
        Logger.d("isBoardNoti : ${SeokwangYouthApplication.firebaseBoardSizeMap[className]}/" +
                "${SeokwangYouthApplication.dbBoardSizeMap[className]}")
        return SeokwangYouthApplication.firebaseBoardSizeMap[className] != SeokwangYouthApplication.dbBoardSizeMap[className]
    }

    fun isFreeBoardNoti(): Boolean {
        Logger.d("isFreeBoardNoti : ${SeokwangYouthApplication.firebaseFreeBoardSize}/" +
                "${SeokwangYouthApplication.dbFreeBoardSize}")
        return SeokwangYouthApplication.firebaseFreeBoardSize != SeokwangYouthApplication.dbFreeBoardSize
    }

    fun requestContactData(searchText: String?) = GlobalScope.launch(Dispatchers.IO) {
        //list = db.memberInfoDao().getAllDataOrderByName()
        if(searchText == null) mutableList = db.memberInfoDao().getAllDataOrderByName()
        else {
            mutableList = db.memberInfoDao().getAllDataOrderByName().filter { member ->
                member.name.contains(searchText)
            }
        }
        Handler(Looper.getMainLooper()).post(Runnable {
            //_listData.value = list
            _listData.value = mutableList.toList()
        })
    }
}