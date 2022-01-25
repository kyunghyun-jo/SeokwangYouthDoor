package kr.co.skchurch.seokwangyouthdoor.ui.memberinfo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.orhanobut.logger.Logger
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import kr.co.skchurch.seokwangyouthdoor.R
import kr.co.skchurch.seokwangyouthdoor.SeokwangYouthApplication
import kr.co.skchurch.seokwangyouthdoor.adapter.MemberInfoCategoryAdapter
import kr.co.skchurch.seokwangyouthdoor.data.AppDatabase
import kr.co.skchurch.seokwangyouthdoor.data.Constants
import kr.co.skchurch.seokwangyouthdoor.data.FirebaseManager
import kr.co.skchurch.seokwangyouthdoor.data.entities.MemberInfoEntity
import kr.co.skchurch.seokwangyouthdoor.data.entities.SimpleEntity
import kr.co.skchurch.seokwangyouthdoor.databinding.FragmentMemberInfoCategoryBinding
import kr.co.skchurch.seokwangyouthdoor.ui.memberinfo.detail.MemberDetailInfoActivity
import kr.co.skchurch.seokwangyouthdoor.utils.Util
import kotlin.math.ceil

class MemberInfoCategoryFragment : Fragment() {

    companion object {
        private val TAG = MemberInfoCategoryFragment::class.java.simpleName
        private const val MSG_REFRESH_LIST = 10
    }

    private var _binding: FragmentMemberInfoCategoryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var adapter: MemberInfoCategoryAdapter
    private lateinit var memberCategoryModel: MemberCategoryViewModel
    private var pasterImgUrl: String? = null
    private val handler by lazy {
        CustomHandler()
    }
    private var isCreated = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMemberInfoCategoryBinding.inflate(inflater, container, false)

        isCreated = true

        Thread(Runnable {
            val memberInfoData = AppDatabase.getDatabase().memberInfoDao().getAllData()
            memberInfoData.forEach { entity ->
                if(entity.type == Constants.MEMBER_TYPE_PASTER) {
                    pasterImgUrl = entity.imageUrl.orEmpty()
                    return@forEach
                }
            }
        }).start()

        initViews()
        return binding.root
    }

    private fun initViews() = with(binding) {
        adapter = MemberInfoCategoryAdapter(onItemClicked = { position, itemData ->
            if(itemData.title.equals(getString(R.string.paster))) {
                Thread(Runnable {
                    val detailInfoList = AppDatabase.getDatabase().memberInfoDao().getAllData()
                    var memberInfoEntity: MemberInfoEntity? = detailInfoList.find { it.type == Constants.MEMBER_TYPE_PASTER }
                    var intent: Intent = Intent(context, MemberDetailInfoActivity::class.java).apply {
                        putExtra(Constants.EXTRA_DETAIL_INFO, memberInfoEntity)
                    }
                    startActivity(intent)
                }).start()
            }
            else {
                var intent: Intent = Intent(context, MemberInfoActivity::class.java).apply {
                    //putExtra(Constants.EXTRA_TITLE, itemData.title)
                    putExtra(Constants.EXTRA_MEMBER_INFO, itemData)
                }
                startActivity(intent)
            }
        })
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = adapter

        //adapter.itemHeight = calculateItemHeight()

        binding.progressBar.visibility = View.VISIBLE
        memberCategoryModel = ViewModelProvider(this@MemberInfoCategoryFragment).get(MemberCategoryViewModel::class.java)
        memberCategoryModel.listData.observe(viewLifecycleOwner, Observer {
            Logger.d("memberCategoryModel pasterImgUrl : $pasterImgUrl")
            binding.progressBar.visibility = View.GONE
            if(it.isNotEmpty()) {
                if(pasterImgUrl!=null) it[0].imageUrl = pasterImgUrl
                //adapter.itemHeight = calculateItemHeight(it.size)
            }
            //adapter.submitList(it)
            pasterImgUrl = null
            val message: Message = Message().also {msg ->
                msg.what = MSG_REFRESH_LIST
                msg.obj = it
            }
            handler.removeMessages(MSG_REFRESH_LIST)
            handler.sendMessageDelayed(message, 100)
        })

        FirebaseManager.instance.registerNotify(object: FirebaseManager.IFirebaseNotify{
            override fun onDataChanged() {
                if(binding == null) return
                Logger.d("Notify onDataChanged")
                binding.progressBar.visibility = View.VISIBLE
                memberCategoryModel.requestCurrentData()
            }
        })
    }

    inner class CustomHandler: Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when(msg.what) {
                MSG_REFRESH_LIST -> {
                    val listData = msg.obj as List<SimpleEntity>
                    if(listData.isNotEmpty()) adapter.itemHeight = calculateItemHeight(listData.size)
                    Logger.d("memberCategoryModel size : ${listData.size} / itemHeight : ${adapter.itemHeight}")
                    adapter.submitList(listData)
                }
            }
            //super.handleMessage(msg)
        }
    }

    private fun calculateItemHeight(listSize: Int): Int {
        val tContext = context as AppCompatActivity
        val displayMetrics = Util.getDisplayMetrics(tContext)
        val realDisplayHeight = displayMetrics.second -
                Util.dpToPx(tContext, SeokwangYouthApplication.navigationHeight.toFloat()).toInt()
        val itemHeight = realDisplayHeight / ceil(listSize.toDouble()/2.toDouble()).toInt()
        return itemHeight
    }

    override fun onResume() {
        super.onResume()
        if(!isCreated) {
            Thread(Runnable {
                val memberInfoData = AppDatabase.getDatabase().memberInfoDao().getAllData()
                memberInfoData.forEach { entity ->
                    if(entity.type == Constants.MEMBER_TYPE_PASTER) {
                        pasterImgUrl = entity.imageUrl.orEmpty()
                        return@forEach
                    }
                }
            }).start()
            memberCategoryModel.requestCurrentData()
        }
        isCreated = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}