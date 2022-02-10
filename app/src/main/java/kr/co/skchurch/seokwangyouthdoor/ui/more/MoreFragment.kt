package kr.co.skchurch.seokwangyouthdoor.ui.more

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.orhanobut.logger.Logger
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kr.co.skchurch.seokwangyouthdoor.BuildConfig
import kr.co.skchurch.seokwangyouthdoor.R
import kr.co.skchurch.seokwangyouthdoor.SeokwangYouthApplication
import kr.co.skchurch.seokwangyouthdoor.data.AppDatabase
import kr.co.skchurch.seokwangyouthdoor.data.Constants
import kr.co.skchurch.seokwangyouthdoor.data.FirebaseManager
import kr.co.skchurch.seokwangyouthdoor.data.SharedPrefManager
import kr.co.skchurch.seokwangyouthdoor.data.entities.HomeEntity
import kr.co.skchurch.seokwangyouthdoor.data.entities.MemberInfoEntity
import kr.co.skchurch.seokwangyouthdoor.data.entities.SimpleEntity
import kr.co.skchurch.seokwangyouthdoor.databinding.FragmentMoreBinding
import kr.co.skchurch.seokwangyouthdoor.ui.board.BoardActivity
import kr.co.skchurch.seokwangyouthdoor.ui.board.free.FreeBoardActivity
import kr.co.skchurch.seokwangyouthdoor.ui.login.LoginActivity
import kr.co.skchurch.seokwangyouthdoor.ui.more.calendar.CalendarActivity
import kr.co.skchurch.seokwangyouthdoor.ui.widget.ContactDialog
import kr.co.skchurch.seokwangyouthdoor.ui.widget.IDialogCallback
import kr.co.skchurch.seokwangyouthdoor.ui.widget.MessageDialog
import kr.co.skchurch.seokwangyouthdoor.ui.widget.ProfileSettingDialog
import kr.co.skchurch.seokwangyouthdoor.utils.Util

class MoreFragment : Fragment() {

    companion object {
        private val TAG = MoreFragment::class.java.simpleName
    }

    private var _binding: FragmentMoreBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var author: String
    private lateinit var myClassName: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMoreBinding.inflate(inflater, container, false)

        SeokwangYouthApplication.getMyProfile {
            author = it?.name ?: FirebaseManager.instance.getCurrentUser()?.email.orEmpty()
            myClassName = it?.className!!
        }

        initViews()
        bindViews()

        return binding.root
    }

    private fun initViews() = with(binding) {
        btnCalendar.btnIcon.setImageDrawable(context?.getDrawable(
            if(Util.isNightMode()) R.drawable.ic_calendar_24
            else R.drawable.ic_calendar_24_black
        ))
        btnCalendar.btnTxt.text = context?.getString(R.string.calendar)

        btnBoard.btnIcon.setImageDrawable(context?.getDrawable(
            if(Util.isNightMode()) R.drawable.ic_list_alt_24
            else R.drawable.ic_list_alt_24_black
        ))
        btnBoard.btnTxt.text = context?.getString(R.string.board)

        btnContact.btnIcon.setImageDrawable(context?.getDrawable(
            if(Util.isNightMode()) R.drawable.ic_contact_24
            else R.drawable.ic_contact_24_black
        ))
        btnContact.btnTxt.text = context?.getString(R.string.contacts)

        btnFreeBoard.btnIcon.setImageDrawable(context?.getDrawable(
            if(Util.isNightMode()) R.drawable.ic_forum_24
            else R.drawable.ic_forum_24_black
        ))

        btnFreeBoard.btnTxt.text = context?.getString(R.string.free_board)

        btnStudyMusic.btnIcon.setImageDrawable(context?.getDrawable(
            if(Util.isNightMode()) R.drawable.ic_queue_music_24
            else R.drawable.ic_queue_music_24_black
        ))
        btnStudyMusic.btnTxt.text = context?.getString(R.string.study_music)
        //btnFreeBoard.root.visibility = View.GONE

        btnInfo.btnIcon.setImageDrawable(context?.getDrawable(
            if(Util.isNightMode()) R.drawable.ic_info_outline_24
            else R.drawable.ic_info_outline_24_black
        ))
        btnInfo.btnIcon.rotation = 180f
        btnInfo.btnTxt.text = context?.getString(R.string.app_info)
    }

    private fun bindViews() = with(binding) {
        btnSearch.setOnClickListener {
            Logger.d("btnSearch click!")
        }
        btnSetting.setOnClickListener {
            Logger.d("btnSetting click!")
            showProfileSettingDialog()
        }
        btnCalendar.root.setOnClickListener {
            Logger.d("btnCalendar click!")
            val tIntent = Intent(context, CalendarActivity::class.java)
            startActivity(tIntent)
        }
        btnBoard.root.setOnClickListener {
            Logger.d("btnBoard click!")
            GlobalScope.launch(Dispatchers.IO) {
                val memberList = AppDatabase.getDatabase().memberInfoDao().getAllData()
                val userEntity = memberList.find {
                    it.detailInfo?.email == SharedPrefManager.getInstance(requireContext()).getUserEmail()
                }
                Logger.d("userEntity : $userEntity")
                Handler(Looper.getMainLooper()).post(Runnable {
                    if(userEntity == null) {
                        Toast.makeText(requireContext(), R.string.not_founded_class_name, Toast.LENGTH_SHORT).show()
                    }
                    else {
                        if(checkMaster(userEntity)) return@Runnable
                        val tIntent = Intent(context, BoardActivity::class.java)
                        val classNameArr = userEntity.className!!.split(",")
                        if(classNameArr.size == 1) {
                            tIntent.putExtra(Constants.EXTRA_CLASS_NAME, classNameArr[0])
                            startActivity(tIntent)
                        }
                        else {
                            showChoiceClassDialog(classNameArr)
                        }
                    }
                })
            }
        }
        btnContact.root.setOnClickListener {
            Logger.d("btnContact click!")
            showContactDialog()
        }
        btnFreeBoard.root.setOnClickListener {
            Logger.d("btnFreeBoard click!")
            val intent = Intent(context, FreeBoardActivity::class.java).also {
                it.putExtra(Constants.EXTRA_AUTHOR, author)
            }
            startActivity(intent)
        }
        btnStudyMusic.root.setOnClickListener {
            Logger.d("btnStudyMusic click!")
            //startActivity(Intent(context, MusicStudyActivity::class.java))
            val praiseLink = SharedPrefManager.getInstance(requireContext()).getPraiseLink()
            Logger.d("praiseLink : $praiseLink")
            if(praiseLink!=null && praiseLink!="null") {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(praiseLink))
                startActivity(intent)
            }
        }
        btnInfo.root.setOnClickListener {
            Logger.d("btnInfo click!")
            showMessageDialog()
        }
    }

    private val moreViewModel by lazy {
        MoreViewModel()
    }
    override fun onResume() {
        super.onResume()
        val classNameArr = myClassName.split(",")
        var isBoardNoti = false
        classNameArr.forEach { className ->
            if(moreViewModel.isBoardNoti(className))
                isBoardNoti = moreViewModel.isBoardNoti(className)
        }
        Logger.d("onResume isBoardNoti : $isBoardNoti / isFreeBoardNoti : ${moreViewModel.isFreeBoardNoti()}")
        binding.btnBoard.btnNotiIcon.visibility =
            if(isBoardNoti) View.VISIBLE else View.GONE
        binding.btnFreeBoard.btnNotiIcon.visibility =
            if(moreViewModel.isFreeBoardNoti()) View.VISIBLE else View.GONE
    }

    private fun checkMaster(userEntity: MemberInfoEntity): Boolean {
        if(!(userEntity.type == Constants.MEMBER_TYPE_PASTER ||
                    userEntity.type == Constants.MEMBER_TYPE_CHIEF_TEACHER)) return false
        GlobalScope.launch(Dispatchers.IO) {
            val memberCategoryList = AppDatabase.getDatabase().memberCategoryDao().getAllData()
            Logger.d("checkMaster memberCategoryList : $memberCategoryList")
            var classNameList = mutableListOf<String>()
            memberCategoryList.forEach {
                if(it.title!=null &&
                    it.title != getString(R.string.paster) &&
                    it.title != getString(R.string.teachers)) {
                    classNameList.add(it.title!!.replace(getString(R.string.className), ""))
                }
            }
            Handler(Looper.getMainLooper()).post(Runnable {
                showChoiceClassDialog(classNameList.toList())
            })
        }
        return true
    }

    private var contactDialog: ContactDialog? = null
    private fun showContactDialog() {
        removeMessageDialog()
        contactDialog = ContactDialog(
            getString(R.string.contacts),
            object: IDialogCallback{
                override fun dialogItemClicked(position: Int, data: Any?) {
                    val memberData = data as MemberInfoEntity
                    Logger.d("ContactDialog dialogItemClicked pos : $position / memberData : $memberData")
                    val tel = "tel:${memberData?.phoneNumber?.replace("-", "")}"
                    //context?.startActivity(Intent(Intent.ACTION_CALL, Uri.parse(tel)))
                    context?.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(tel)))
                }

                override fun dialogBtnClicked(id: Int) {
                    Logger.d("ContactDialog dialogBtnClicked")
                    removeContactDialog()
                }

            })
        contactDialog?.show(parentFragmentManager, "ContactDialog")
    }

    private fun removeContactDialog() {
        contactDialog?.dismiss()
        contactDialog = null
    }

    private val ID_APP_VERSION = 0L
    private val ID_AUTHOR = 1L
    private val ID_USER_GUIDE = 3L
    private fun generateAppInfoData(): List<SimpleEntity> {
        return mutableListOf<SimpleEntity>(
            SimpleEntity(ID_APP_VERSION, getString(R.string.info_app_version), "v${BuildConfig.VERSION_NAME}", null,
                Util.getUUID(), Util.getTimestamp()),
            SimpleEntity(ID_AUTHOR, getString(R.string.info_app_author), "Kyunghyun, Cho", null,
                Util.getUUID(), Util.getTimestamp()),
            SimpleEntity(ID_USER_GUIDE, getString(R.string.info_app_guide), null, null,
                Util.getUUID(), Util.getTimestamp())
        )
    }

    private var messageDialog: MessageDialog? = null
    private fun showMessageDialog() {
        removeMessageDialog()
        val btnData = arrayListOf<Pair<Int, String>>(
            Pair(0, getString(R.string.ok))
        )
        messageDialog = MessageDialog(
                getString(R.string.detail_info),
                generateAppInfoData().toList(),
                object: IDialogCallback{
                    override fun dialogItemClicked(position: Int, data: Any?) {
                        val simpleEntity = data as SimpleEntity
                        when(simpleEntity.id) {
                            ID_USER_GUIDE -> {
                                startActivity(Intent(activity, GuideActivity::class.java))
                            }
                            else -> {}
                        }
                    }

                    override fun dialogBtnClicked(id: Int) {
                        removeMessageDialog()
                    }

                },
                btnData)
        messageDialog?.show(parentFragmentManager, "MessageDialog")
    }

    private fun removeMessageDialog() {
        messageDialog?.dismiss()
        messageDialog = null
    }

    private var logoutDialog: MessageDialog? = null
    private fun showLogoutDialog() {
        removeLogoutDialog()
        val btnData = arrayListOf<Pair<Int, String>>(
            Pair(0, getString(R.string.no)),
            Pair(1, getString(R.string.yes))
        )
        val listData = arrayListOf<SimpleEntity>(
            SimpleEntity(1L, getString(R.string.check_logout), null, null, Util.getUUID(), Util.getTimestamp())
        )
        logoutDialog = MessageDialog(
            getString(R.string.warning),
            listData,
            object: IDialogCallback{
                override fun dialogItemClicked(position: Int, data: Any?) {}

                override fun dialogBtnClicked(id: Int) {
                    when(id) {
                        1 -> {
                            Logger.d("Logout Event!!")
                            Firebase.auth.signOut()
                            startActivity(Intent(activity, LoginActivity::class.java))
                            activity?.finish()
                        }
                        else -> removeLogoutDialog()
                    }
                }

            },
            btnData)
        logoutDialog?.show(parentFragmentManager, "LogoutDialog")
    }

    private fun removeLogoutDialog() {
        logoutDialog?.dismiss()
        logoutDialog = null
    }

    private var choiceClassDialog: MessageDialog? = null
    private fun showChoiceClassDialog(classNameArr: List<String>) {
        removeChoiceClassDialog()
        val btnData = arrayListOf<Pair<Int, String>>(
            Pair(0, getString(R.string.ok))
        )
        var listData: MutableList<SimpleEntity> = mutableListOf()
        classNameArr.forEachIndexed { index, name ->
            listData.add(SimpleEntity(index.toLong(), name, null, null, Util.getUUID(), Util.getTimestamp()))
        }
        choiceClassDialog = MessageDialog(
            getString(R.string.choice_class),
            listData,
            object: IDialogCallback{
                override fun dialogItemClicked(position: Int, data: Any?) {
                    removeChoiceClassDialog()
                    val simpleEntity = data as SimpleEntity
                    val tIntent = Intent(context, BoardActivity::class.java)
                    tIntent.putExtra(Constants.EXTRA_CLASS_NAME, simpleEntity.title)
                    startActivity(tIntent)
                }

                override fun dialogBtnClicked(id: Int) {
                    removeChoiceClassDialog()
                }

            },
            btnData)
        choiceClassDialog?.show(parentFragmentManager, "ChoiceClassDialog")
    }

    private fun removeChoiceClassDialog() {
        choiceClassDialog?.dismiss()
        choiceClassDialog = null
    }

    private val ID_PROFILE_IMAGE = 0L
    private val ID_LOGOUT = 1L
    private fun generateProfileSettingData(myProfile: MemberInfoEntity): List<HomeEntity> {
        Logger.d("generateProfileSettingData myProfile : ${myProfile.imageUrl}")
        return mutableListOf<HomeEntity>(
            HomeEntity(ID_PROFILE_IMAGE, Constants.ITEM_TYPE_PROFILE_IMAGE, getString(R.string.change_profile_image),
                null, myProfile.imageUrl, 0, Util.getUUID(), Util.getTimestamp()),
            HomeEntity(ID_LOGOUT, Constants.ITEM_TYPE_NORMAL, getString(R.string.logout)+" : "+myProfile.name,
                Constants.OPTION_ALIGN_CENTER, null, 0, Util.getUUID(), Util.getTimestamp())
        )
    }

    private var profileSettingDialog: ProfileSettingDialog? = null
    private fun showProfileSettingDialog() {
        removeProfileSettingDialog()
        val btnData = arrayListOf<Pair<Int, String>>(
            Pair(0, getString(R.string.ok))
        )
        SeokwangYouthApplication.getMyProfile {
            Logger.d("getMyProfile : $it")
            if(it!=null) {
                profileSettingDialog = ProfileSettingDialog(
                    getString(R.string.profile_settings),
                    generateProfileSettingData(it).toList(),
                    object: IDialogCallback{
                        override fun dialogItemClicked(position: Int, data: Any?) {
                            val entity = data as HomeEntity
                            when(entity.id) {
                                ID_LOGOUT -> {
                                    showLogoutDialog()
                                }
                                ID_PROFILE_IMAGE -> {

                                }
                                else -> {}
                            }
                        }

                        override fun dialogBtnClicked(id: Int) {
                            removeProfileSettingDialog()
                        }

                    },
                    btnData)
                profileSettingDialog?.show(parentFragmentManager, "ProfileSettingDialog")
            }
        }
    }

    private fun removeProfileSettingDialog() {
        profileSettingDialog?.dismiss()
        profileSettingDialog = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}