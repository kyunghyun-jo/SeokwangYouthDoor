package kr.co.skchurch.seokwangyouthdoor

import android.app.Application
import android.content.Context
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kr.co.skchurch.seokwangyouthdoor.data.AppDatabase
import kr.co.skchurch.seokwangyouthdoor.data.Constants
import kr.co.skchurch.seokwangyouthdoor.data.FirebaseManager
import kr.co.skchurch.seokwangyouthdoor.data.entities.MemberInfoEntity

class SeokwangYouthApplication: Application() {
    companion object {
        var context: Context? = null
        var navigationHeight: Int = 0
        var firebaseBoardSizeMap: HashMap<String, Int> = HashMap()
        var firebaseFreeBoardSize: Int = 0
        var dbBoardSizeMap: HashMap<String, Int> = HashMap()
        var dbFreeBoardSize = 0
        fun getMyProfile(onResult: (MemberInfoEntity?) -> Unit) = GlobalScope.launch(Dispatchers.IO) {
            val myEmail = FirebaseManager.instance.getCurrentUser()?.email
            onResult(AppDatabase.getDatabase().memberInfoDao().getAllData().find {
                it.detailInfo?.email == myEmail
            })
        }
    }

    override fun onCreate() {
        super.onCreate()

        context = this

        val formatStrategy: FormatStrategy = PrettyFormatStrategy.newBuilder()
            .showThreadInfo(false) // (Optional) Whether to show thread info or not. Default true
            .methodCount(2) // (Optional) How many method line to show. Default 2
            .methodOffset(5) // (Optional) Hides internal method calls up to offset. Default 5
            .tag("SKChurch") // (Optional) Global tag for every log. Default PRETTY_LOGGER
            .build()
        Logger.addLogAdapter(CustomLogAdapter(formatStrategy))
    }

    private class CustomLogAdapter(formatStrategy: FormatStrategy) :
        AndroidLogAdapter(formatStrategy) {
        override fun isLoggable(priority: Int, tag: String?): Boolean {
            //return super.isLoggable(priority, tag)
            //return BuildConfig.DEBUG
            return Constants.LOG_ENABLE
        }
    }
}