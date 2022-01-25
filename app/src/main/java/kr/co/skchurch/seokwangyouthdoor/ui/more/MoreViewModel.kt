package kr.co.skchurch.seokwangyouthdoor.ui.more

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kr.co.skchurch.seokwangyouthdoor.data.AppDatabase
import kr.co.skchurch.seokwangyouthdoor.data.Constants
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
        }
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