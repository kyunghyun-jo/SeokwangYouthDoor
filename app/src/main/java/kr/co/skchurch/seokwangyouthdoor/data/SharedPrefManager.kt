package kr.co.skchurch.seokwangyouthdoor.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SharedPrefManager {

    companion object {
        private val TAG = SharedPrefManager::class.java.simpleName
        private var pref: SharedPreferences? = null
        private lateinit var instance: SharedPrefManager
        fun getInstance(context: Context): SharedPrefManager {
            if(!::instance.isInitialized) {
                instance = SharedPrefManager()
            }
            pref = context.getSharedPreferences("seokwangPref", Context.MODE_PRIVATE)
            return instance
        }
    }

    fun getWorshipNotice(): String? = pref?.getString("worshipNotice", null)

    fun setWorshipNotice(notice: String) {
        pref?.edit(true) {
            putString("worshipNotice", notice)
        }
    }

    fun getPraiseLink(): String? = pref?.getString("praiseLink", null)

    fun setPraiseLink(link: String) {
        pref?.edit(true) {
            putString("praiseLink", link)
        }
    }

    fun getUserEmail(): String? = pref?.getString("userEmail", null)

    fun setUserEmail(email: String) {
        pref?.edit(true) {
            putString("userEmail", email)
        }
    }

    fun getIsFirstTime(): Boolean? = pref?.getBoolean("isFirstTime", true)

    fun setIsFirstTime(isFirstTime: Boolean) {
        pref?.edit(true) {
            putBoolean("isFirstTime", isFirstTime)
        }
    }
}