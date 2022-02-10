package kr.co.skchurch.seokwangyouthdoor.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.orhanobut.logger.Logger
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kr.co.skchurch.seokwangyouthdoor.MainActivity
import kr.co.skchurch.seokwangyouthdoor.SeokwangYouthApplication
import kr.co.skchurch.seokwangyouthdoor.data.AppDatabase
import kr.co.skchurch.seokwangyouthdoor.data.FirebaseConstants
import kr.co.skchurch.seokwangyouthdoor.databinding.ActivitySplashBinding
import kr.co.skchurch.seokwangyouthdoor.ui.login.LoginActivity
import kr.co.skchurch.seokwangyouthdoor.ui.memberinfo.MemberCategoryViewModel
import kr.co.skchurch.seokwangyouthdoor.ui.memberinfo.MemberInfoViewModel
import kr.co.skchurch.seokwangyouthdoor.ui.more.MoreViewModel
import kr.co.skchurch.seokwangyouthdoor.ui.more.calendar.CalendarViewModel

class SplashActivity : AppCompatActivity() {

    companion object {
        private val TAG = SplashActivity::class.java.simpleName
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppDatabase.context = this
        auth = Firebase.auth

        initAppData()

        if(auth.currentUser == null) startApp()
        else {
            Handler(mainLooper).postDelayed(Runnable {
                startApp()
            }, 1000)
        }
    }

    private fun initAppData() = GlobalScope.launch(Dispatchers.IO) {
        val db = AppDatabase.getDatabase()
        var memberInfoList = db.memberInfoDao().getAllData()
        Logger.d("onCreate memberInfoList : $memberInfoList")
        ViewModelProvider(this@SplashActivity).get(MemberInfoViewModel::class.java)

        var calendarEventList = db.calendarDao().getAllData()
        Logger.d("onCreate calendarEventList : $calendarEventList")
        ViewModelProvider(this@SplashActivity).get(CalendarViewModel::class.java)

        var categoryList = db.memberCategoryDao().getAllData()
        Logger.d("onCreate categoryList : $categoryList")
        ViewModelProvider(this@SplashActivity).get(MemberCategoryViewModel::class.java)

        ViewModelProvider(this@SplashActivity).get(MoreViewModel::class.java)
    }

    private fun startApp() {
        if(auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        else {
            val userId = auth.currentUser?.uid.orEmpty()
            val currentUserDB = Firebase.database.reference.child(FirebaseConstants.TABLE_USERS).child(userId)
            val user = mutableMapOf<String, Any>()
            user[FirebaseConstants.USER_ID] = userId
            currentUserDB.updateChildren(user)
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
}