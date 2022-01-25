package kr.co.skchurch.seokwangyouthdoor.ui.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.InputType
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.actionCodeSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.orhanobut.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kr.co.skchurch.seokwangyouthdoor.MainActivity
import kr.co.skchurch.seokwangyouthdoor.R
import kr.co.skchurch.seokwangyouthdoor.data.AppDatabase
import kr.co.skchurch.seokwangyouthdoor.databinding.ActivityLoginBinding
import kr.co.skchurch.seokwangyouthdoor.ui.memberinfo.MemberInfoViewModel
import kr.co.skchurch.seokwangyouthdoor.ui.more.calendar.CalendarViewModel

class LoginActivity : AppCompatActivity() {

    companion object {
        private val TAG = LoginActivity::class.java.simpleName
        private const val MODE_LOGIN = 0
        private const val MODE_REGISTRATION = 1
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding
    private var currentMode: Int = MODE_LOGIN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        initViews()
    }

    private fun initViews() = with(binding) {
        refreshViews(MODE_LOGIN)
        requestAuthCodeBtn.setOnClickListener {
            val addressTxt = emailEditTxt.text.toString()
            val domainTxt = domainEditTxt.text.toString()
            val emailAddress = "$addressTxt@$domainTxt"
            val passwordTxt = passwordEditTxt.text.toString()
            Logger.d("requestAuthCodeBtn emailAddress : $emailAddress / $passwordTxt")

            if(passwordTxt.length < 6) {
                Toast.makeText(this@LoginActivity, R.string.input_wrong_password,
                    Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val actionCodeSettings = actionCodeSettings {
                // URL you want to redirect back to. The domain (www.example.com) for this
                // URL must be whitelisted in the Firebase Console.
                url = "https://skchurch.page.link/test"
                //url = "https://naver.com"
                // This must be true
                handleCodeInApp = true
                setIOSBundleId("com.example.ios")
                setAndroidPackageName(
                    packageName,
                    false, /* installIfNotAvailable */
                    null /* minimumVersion */)
            }
            /*
            auth.sendSignInLinkToEmail(emailAddress, actionCodeSettings).addOnCompleteListener { task ->
                Logger.d("Email send task : ${task.isSuccessful} / ${task.exception}")
                if (task.isSuccessful) {
                    startActivity(Intent(this@LoginActivity, LoginActivity::class.java))
                    finish()
                }
            }
             */

            when(currentMode) {
                MODE_LOGIN -> {
                    loginEvent(emailAddress, passwordTxt)
                }
                MODE_REGISTRATION -> {
                    auth.createUserWithEmailAndPassword(emailAddress, passwordTxt)
                        .addOnCompleteListener(this@LoginActivity) { task ->
                            if(task.isSuccessful) {
                                Toast.makeText(this@LoginActivity, R.string.registration_success,
                                    Toast.LENGTH_SHORT).show()
                                //refreshViews(MODE_LOGIN)
                                loginEvent(emailAddress, passwordTxt)
                            }
                            else {
                                Toast.makeText(this@LoginActivity, R.string.registration_failed,
                                    Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }
        }

        domainEditTxt.setText(resources.getStringArray(R.array.email_spinner_items)[0])
        domainSpinner.adapter = ArrayAdapter.createFromResource(
            this@LoginActivity, R.array.email_spinner_items, android.R.layout.simple_dropdown_item_1line)
        domainSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Logger.d("spinner onItemSelected : $position / ${domainSpinner.adapter.getItem(position)}")
                if(position == resources.getStringArray(R.array.email_spinner_items).size-1) {
                    domainSpinner.visibility = View.GONE
                    domainEditTxt.setText("")
                    domainEditTxt.visibility = View.VISIBLE
                    domainEditTxt.requestFocus()
                }
                else domainEditTxt.setText(domainSpinner.adapter.getItem(position).toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        /*
        signUpBtn.setOnClickListener {
            val email = emailEditTxt.text.toString()
            val password = passwordEditTxt.text.toString()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this@LoginActivity) { task ->
                    if(task.isSuccessful) {
                        Toast.makeText(this@LoginActivity, "회원 가입에 성공했습니다. " +
                                "로그인 버튼을 눌러 로그인해주세요",
                            Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(this@LoginActivity, "이미 가입한 이메일이거나, " +
                                "회원 가입에 실패했습니다.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }
         */

        emailEditTxt.addTextChangedListener {
            requestAuthCodeBtn.isEnabled = emailEditTxt.text.isNotEmpty()
        }

        passwordShowCheck.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked) passwordEditTxt.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
            else passwordEditTxt.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            passwordEditTxt.setSelection(passwordEditTxt.text.length)
        }
    }

    private fun loginEvent(emailAddress: String, passwordTxt: String) {
        auth.signInWithEmailAndPassword(emailAddress, passwordTxt).addOnCompleteListener { task ->
            Logger.d("Email 111 task : ${task.isSuccessful} / ${task.exception}")
            if (task.isSuccessful) {
                initAppData()

                binding.progressBar.visibility = View.VISIBLE
                Handler(mainLooper).postDelayed(Runnable {
                    binding.progressBar.visibility = View.GONE
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }, 2000)
            }
            else {
                if(task.exception?.message?.contains("password") == true) {
                    Toast.makeText(this@LoginActivity, R.string.wrong_password,
                        Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(this@LoginActivity, R.string.not_registed_user,
                        Toast.LENGTH_SHORT).show()
                    refreshViews(MODE_REGISTRATION)
                }
            }
        }
    }

    private fun refreshViews(mode: Int) {
        currentMode = mode
        when(mode) {
            MODE_LOGIN -> {
                binding.messageTxt.text = getString(R.string.how_to_login)
                binding.requestAuthCodeBtn.setText(R.string.login)
            }
            MODE_REGISTRATION -> {
                binding.messageTxt.text = getString(R.string.how_to_authorize)
                binding.requestAuthCodeBtn.setText(R.string.registration)
            }
            else -> {}
        }
    }

    private fun initAppData() = GlobalScope.launch(Dispatchers.IO) {
        val db = AppDatabase.getDatabase()
        var memberInfoList = db.memberInfoDao().getAllData()
        Logger.d("onCreate memberInfoList : $memberInfoList")
        if(memberInfoList!!.isEmpty()) {
            ViewModelProvider(this@LoginActivity).get(MemberInfoViewModel::class.java)
        }

        var calendarEventList = db.calendarDao().getAllData()
        Logger.d("onCreate calendarEventList : $calendarEventList")
        if(calendarEventList!!.isEmpty()) {
            ViewModelProvider(this@LoginActivity).get(CalendarViewModel::class.java)
        }

//        var categoryList = db.memberCategoryDao().getAllData()
//        Logger.d("onCreate categoryList : $categoryList")
//        if(categoryList == null || categoryList!!.isEmpty()) {
//            ViewModelProvider(this@LoginActivity).get(MemberCategoryViewModel::class.java)
//        }
    }

    private fun handleSuccessLogin() {
        if(auth.currentUser == null) {
            Toast.makeText(this, R.string.login_failed,
                Toast.LENGTH_LONG).show()
            return
        }

        //auth.setLanguageCode("kr")
        auth.currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Logger.d("Email sent.")
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }
}