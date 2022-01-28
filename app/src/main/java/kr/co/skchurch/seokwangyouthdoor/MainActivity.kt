package kr.co.skchurch.seokwangyouthdoor

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kr.co.skchurch.seokwangyouthdoor.data.*
import kr.co.skchurch.seokwangyouthdoor.databinding.ActivityMainBinding
import kr.co.skchurch.seokwangyouthdoor.ui.home.HomeFragment
import kr.co.skchurch.seokwangyouthdoor.ui.memberinfo.MemberInfoCategoryFragment
import kr.co.skchurch.seokwangyouthdoor.ui.more.GuideActivity
import kr.co.skchurch.seokwangyouthdoor.ui.more.MoreFragment
import kr.co.skchurch.seokwangyouthdoor.ui.timetable.TimetableFragment
import kr.co.skchurch.seokwangyouthdoor.utils.Util

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private val REQ_PERMISSIONS = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        private const val REQ_CODE_PERMISSIONS = 10
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppDatabase.context = this
        //Toast.makeText(this, "안녕하세요! ${FirebaseManager.instance.getUserName()} 님!", Toast.LENGTH_SHORT).show()

        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        if(auth.currentUser!=null && auth.currentUser!!.email!=null)
            SharedPrefManager.getInstance(this).setUserEmail(auth.currentUser!!.email.orEmpty())

        Util.getDisplayMetrics(this)

        initCommonData()
        initViews()
        bindViews()

        if(!allPermissionGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQ_PERMISSIONS,
                REQ_CODE_PERMISSIONS
            )
        }

        registerReceiver()

        SeokwangYouthApplication.getMyProfile {
            runOnUiThread {
                val welcomeMessage = String.format(resources.getString(R.string.welcome_message), it?.name)
                Toast.makeText(this, welcomeMessage, Toast.LENGTH_SHORT).show()
            }
        }

        if(SharedPrefManager.getInstance(this).getIsFirstTime() == true) {
            startActivity(Intent(this, GuideActivity::class.java))
            SharedPrefManager.getInstance(this).setIsFirstTime(false)
        }
    }
    
    private fun initCommonData() = GlobalScope.launch(Dispatchers.IO) {
        FirebaseManager.instance.registerCommonDB(object: FirebaseManager.IFirebaseCallback{
            override fun onValueDataChange(snapshot: DataSnapshot) {
                Logger.d("initCommonData onValueDataChange : $snapshot")
                if(snapshot.key == null || snapshot.key?.isEmpty() == true) return
                when(snapshot.key) {
                    FirebaseConstants.KEY_WORSHIP_NOTICE -> {
                        SharedPrefManager.getInstance(this@MainActivity).setWorshipNotice(snapshot.value.toString())
                    }
                    FirebaseConstants.KEY_PRAISE_LINK -> {
                        SharedPrefManager.getInstance(this@MainActivity).setPraiseLink(snapshot.value.toString())
                    }
                    else -> {}
                }
                //SharedPrefManager.getInstance(this@MainActivity).setPraiseLink(snapshot.value.toString())
            }

            override fun onValueCancelled(error: DatabaseError) {}

            override fun onEventChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onEventChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Logger.d("initCommonData onEventChildChanged : $snapshot")
                if(snapshot.key == null || snapshot.key?.isEmpty() == true) return
                when(snapshot.key) {
                    FirebaseConstants.KEY_WORSHIP_NOTICE -> {
                        SharedPrefManager.getInstance(this@MainActivity).setWorshipNotice(snapshot.value.toString())
                    }
                    FirebaseConstants.KEY_PRAISE_LINK -> {
                        SharedPrefManager.getInstance(this@MainActivity).setPraiseLink(snapshot.value.toString())
                    }
                    else -> {}
                }
            }

            override fun onEventChildRemoved(snapshot: DataSnapshot) {}

            override fun onEventChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onEventCancelled(error: DatabaseError) {}

        })
        Logger.d("initCommonData worshipNotice : " +
                "${SharedPrefManager.getInstance(this@MainActivity).getWorshipNotice()}")
        Logger.d("initCommonData praiseLink : " +
                "${SharedPrefManager.getInstance(this@MainActivity).getPraiseLink()}")
    }

    /*
    private fun registerPushToken() {
        //v17.0.0 이전까지는
        ////var pushToken = FirebaseInstanceId.getInstance().token
        //v17.0.1 이후부터는 onTokenRefresh()-depriciated
        var pushToken: String? = null
        var uid = FirebaseAuth.getInstance().currentUser!!.uid
        var map = mutableMapOf<String, Any>()
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
            pushToken = instanceIdResult.token
            map["pushtoken"] = pushToken!!
            FirebaseFirestore.getInstance().collection("pushtokens").document(uid!!).set(map)
        }
    }
     */

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        //val receivedType = intent?.getStringExtra("notiType")
        //Logger.d("onNewIntent receivedType : $receivedType")
        updateResult(true)
    }

    private fun updateResult(isNewIntent: Boolean = false) {
        Logger.d("updateResult isNewIntent : $isNewIntent / intent : ${intent.getStringExtra("notiType")}")
        //resultTxt.text = (intent.getStringExtra("notiType") ?: "앱 런처") +
        //        if(isNewIntent) "(으)로 갱신했습니다." else "(으)로 실행했습니다."
    }

    private fun allPermissionGranted() = REQ_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQ_CODE_PERMISSIONS) {
            var permissionCount = 0
            grantResults.forEach {
                if(it == PackageManager.PERMISSION_GRANTED) permissionCount++
            }
            if(permissionCount == REQ_PERMISSIONS.size) {
                Logger.d(getString(R.string.get_permission_success))
                //Toast.makeText(this, R.string.get_permission_success, Toast.LENGTH_SHORT).show()
            }
            else {
                Logger.d(getString(R.string.get_permission_failed))
                //Toast.makeText(this, R.string.get_permission_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private var isRegisterReceiver = false
    private val mainReceiver by lazy {
        MainReceiver()
    }
    private fun registerReceiver() {
        if(isRegisterReceiver) return
        var intentFilter = IntentFilter().apply {
            addAction(Constants.ACTION_CHANGE_VIEWPAGER_ENABLE)
        }
        registerReceiver(mainReceiver, intentFilter)
        isRegisterReceiver = true
    }

    private fun unregisterReceiver() {
        if(!isRegisterReceiver) return
        unregisterReceiver(mainReceiver)
        isRegisterReceiver = false
    }

    inner class MainReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action) {
                Constants.ACTION_CHANGE_VIEWPAGER_ENABLE -> {
                    val isEnable = intent.getBooleanExtra(Constants.EXTRA_IS_ENABLE, false)
                    Logger.d("ACTION_CHANGE_VIEWPAGER_ENABLE : $isEnable")
                    binding.viewPager.isUserInputEnabled = isEnable
                    binding.navView.menu.forEach {
                        it.isEnabled = isEnable
                    }
                }
            }
        }
    }

    private fun initViews() = with(binding) {
        viewPager.adapter = ViewPagerAdapter(this@MainActivity)

        navView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            Logger.d("navigation22 height : ${navView.height}")
            if(navView.height>0) SeokwangYouthApplication.navigationHeight = navView.height
        }

        /*
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_timetable, R.id.navigation_info, R.id.navigation_more
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        */
    }

    private fun bindViews() = with(binding) {
        viewPager.registerOnPageChangeCallback( object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                navView.menu.getItem(position).isChecked = true
            }
        })

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    Logger.d("OnItemSelected home")
                    binding.viewPager.currentItem = 0
                    true
                }
                R.id.navigation_timetable -> {
                    Logger.d("OnItemSelected timetable")
                    binding.viewPager.currentItem = 1
                    true
                }
                R.id.navigation_info -> {
                    Logger.d("OnItemSelected info")
                    binding.viewPager.currentItem = 2
                    true
                }
                R.id.navigation_more -> {
                    Logger.d("OnItemSelected more")
                    binding.viewPager.currentItem = 3
                    true
                }
                else -> {
                    Logger.d("OnItemSelected unknown")
                    false
                }
            }
        }
    }

    private lateinit var moreFragment: MoreFragment
    inner class ViewPagerAdapter(fragment: FragmentActivity): FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            var fragment: Fragment
            when(position) {
                0 -> fragment = HomeFragment()
                1 -> fragment = TimetableFragment()
                2 -> fragment = MemberInfoCategoryFragment()
                3 -> {
                    fragment = MoreFragment()
                    moreFragment = fragment
                }
                else -> fragment = HomeFragment()
            }
            return fragment
        }
    }

    override fun onBackPressed() {
        showExitWarningDialog()
        //super.onBackPressed()
    }

    private fun showExitWarningDialog() {
        AlertDialog.Builder(this)
            .setTitle(R.string.warning)
            .setMessage(R.string.exit_app)
            .setPositiveButton(R.string.yes) { dialog, which ->
                Logger.d("click yes button!")
                super.onBackPressed()
            }
            .setNegativeButton(R.string.no) { _, _ -> }
            .create()
            .show()
    }

    override fun onDestroy() {
        unregisterReceiver()
        super.onDestroy()
    }
}