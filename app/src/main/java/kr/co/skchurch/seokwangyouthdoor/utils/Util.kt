package kr.co.skchurch.seokwangyouthdoor.utils

import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.DisplayMetrics
import com.orhanobut.logger.Logger
import android.util.TypedValue
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import kr.co.skchurch.seokwangyouthdoor.SeokwangYouthApplication
import java.text.SimpleDateFormat
import java.util.*

object Util {

    /**
     * 생년월일을 기준으로 현재 나이 계산
     * @param unix unixtimestamp
     */
    fun calculateAge(birthDate: String): Int {
        val dateArr = birthDate.split(".")
        val birthCalendar = Calendar.getInstance()
        birthCalendar.set(Calendar.YEAR, dateArr[0].toInt())
        birthCalendar.set(Calendar.MONTH, dateArr[1].toInt()-1)
        birthCalendar.set(Calendar.DATE, dateArr[2].toInt())
        val current = Calendar.getInstance()
        val currentYear = current[Calendar.YEAR]
        val currentMonth = current[Calendar.MONTH]
        val currentDay = current[Calendar.DAY_OF_MONTH]
        var age = currentYear - birthCalendar[Calendar.YEAR]
        if (birthCalendar[Calendar.MONTH] * 100 +
            birthCalendar[Calendar.DAY_OF_MONTH] > currentMonth * 100 + currentDay
        ) age--
        return age+1
    }

    lateinit var displayMetrics: Pair<Int, Int>
    fun getDisplayMetrics(activity: AppCompatActivity): Pair<Int, Int> {
        if(::displayMetrics.isInitialized) return displayMetrics
        displayMetrics = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            val width = windowMetrics.bounds.width() - insets.left - insets.right
            val height = windowMetrics.bounds.height() - insets.bottom
            Pair<Int, Int>(width, height)
        } else {
            var tDisplayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(tDisplayMetrics)
            Pair<Int, Int>(tDisplayMetrics.widthPixels, tDisplayMetrics.heightPixels)
        }
        return displayMetrics
    }

    fun dpToPx(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.resources.displayMetrics)
    }

    fun isNightMode(): Boolean {
        return SeokwangYouthApplication.context!!.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    fun getTimestamp() : String {
        //현재시간
        val curTime = Date().time
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        //TimeZone  설정 (GMT +9)
        //format.timeZone = TimeZone.getTimeZone("Asia/Seoul")
        //결과물
        return format.format(curTime)
    }

    fun getUUID() = UUID.randomUUID().toString()

    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Logger.i("NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Logger.i("NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Logger.i("NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }
}