package kr.co.skchurch.seokwangyouthdoor.ui.memberinfo.detail

import android.os.Bundle
import com.orhanobut.logger.Logger
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kr.co.skchurch.seokwangyouthdoor.R
import kr.co.skchurch.seokwangyouthdoor.adapter.HomeListAdapter
import kr.co.skchurch.seokwangyouthdoor.data.Constants
import kr.co.skchurch.seokwangyouthdoor.data.FirebaseManager
import kr.co.skchurch.seokwangyouthdoor.data.MemberType
import kr.co.skchurch.seokwangyouthdoor.data.entities.HomeEntity
import kr.co.skchurch.seokwangyouthdoor.data.entities.MemberDetailInfoEntity
import kr.co.skchurch.seokwangyouthdoor.data.entities.MemberInfoEntity
import kr.co.skchurch.seokwangyouthdoor.databinding.ActivityMemberDetailInfoBinding
import kr.co.skchurch.seokwangyouthdoor.utils.Util

class MemberDetailInfoActivity : AppCompatActivity() {

    companion object {
        private val TAG = MemberDetailInfoActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityMemberDetailInfoBinding
    private lateinit var receivedData: MemberInfoEntity
    private lateinit var adapter: HomeListAdapter
    private var listData: MutableList<HomeEntity> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMemberDetailInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(intent == null || intent.getParcelableExtra<MemberInfoEntity>(Constants.EXTRA_DETAIL_INFO) == null) {
            finish()
        }
        receivedData = intent.getParcelableExtra<MemberInfoEntity>(Constants.EXTRA_DETAIL_INFO)!!

        initViews()
    }

    private fun initViews() = with(binding) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        collapsingToolbar.title = receivedData.name + " ${MemberType.values()[receivedData.type].value}"

        refreshView()

        adapter = HomeListAdapter(onItemClicked = { _, _ -> })
        adapter.setItemFocusable(false)
        recyclerView.layoutManager = LinearLayoutManager(this@MemberDetailInfoActivity)
        recyclerView.adapter = adapter
        adapter.submitList(listData.toList())

        FirebaseManager.instance.registerNotify(object: FirebaseManager.IFirebaseNotify{
            override fun onDataChanged() {
                Logger.d("Notify onDataChanged")
                //binding.progressBar.visibility = View.VISIBLE
                refreshView()
            }
        })
    }

    private fun refreshView() = with(binding) {
        Glide.with(topInfoImage)
            .load(receivedData.imageUrl)
            .error(getDefaultImgId(receivedData))
            .apply(RequestOptions.centerCropTransform())
            .into(topInfoImage)

        listData.clear()
        var count: Long = 0
        val GENDER_ARR = resources.getStringArray(R.array.gender_values)
        listData.add(HomeEntity(count++, Constants.ITEM_TYPE_HEADER, getString(R.string.gender),
            null, null, 0, Util.getUUID(), Util.getTimestamp()))
        listData.add(HomeEntity(count++, Constants.ITEM_TYPE_NORMAL, GENDER_ARR[receivedData.gender],
            null, null, 0, Util.getUUID(), Util.getTimestamp()))
        listData.add(HomeEntity(count++, Constants.ITEM_TYPE_HEADER, getString(R.string.member_birthday),
            null, null, 0, Util.getUUID(), Util.getTimestamp()))
        listData.add(HomeEntity(count++, Constants.ITEM_TYPE_NORMAL, receivedData.birth,
            null, null, 0, Util.getUUID(), Util.getTimestamp()))
        listData.add(HomeEntity(count++, Constants.ITEM_TYPE_HEADER, getString(R.string.phone_number),
            null, null, 0, Util.getUUID(), Util.getTimestamp()))
        listData.add(HomeEntity(count++, Constants.ITEM_TYPE_NORMAL, receivedData.phoneNumber,
            null, null, 0, Util.getUUID(), Util.getTimestamp()))
        if(receivedData.detailInfo!=null) {
            val detailInfo: MemberDetailInfoEntity = receivedData.detailInfo!!
            if(detailInfo.age!=null) {
                listData.add(HomeEntity(count++, Constants.ITEM_TYPE_HEADER, getString(R.string.age),
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
                listData.add(HomeEntity(count++, Constants.ITEM_TYPE_NORMAL, detailInfo.age.toString(),
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
            }
            if(detailInfo.school!=null) {
                listData.add(HomeEntity(count++, Constants.ITEM_TYPE_HEADER,
                    if(receivedData.type == Constants.MEMBER_TYPE_STUDENT)
                        getString(R.string.school) else getString(R.string.job),
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
                listData.add(HomeEntity(count++, Constants.ITEM_TYPE_NORMAL, detailInfo.school,
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
            }
            if(detailInfo.email!=null) {
                listData.add(HomeEntity(count++, Constants.ITEM_TYPE_HEADER, getString(R.string.email),
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
                listData.add(HomeEntity(count++, Constants.ITEM_TYPE_NORMAL, detailInfo.email,
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
            }
            if(detailInfo.snsId!=null) {
                listData.add(HomeEntity(count++, Constants.ITEM_TYPE_HEADER, getString(R.string.sns),
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
                listData.add(HomeEntity(count++, Constants.ITEM_TYPE_NORMAL, detailInfo.snsId,
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
            }
            if(detailInfo.hobby!=null) {
                listData.add(HomeEntity(count++, Constants.ITEM_TYPE_HEADER, getString(R.string.hobby),
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
                listData.add(HomeEntity(count++, Constants.ITEM_TYPE_NORMAL, detailInfo.hobby,
                    null, null, 0, Util.getUUID(), Util.getTimestamp()))
            }
        }
        if(::adapter.isInitialized) adapter.submitList(listData.toList())
    }

    private fun getDefaultImgId(entity: MemberInfoEntity): Int {
        return if(entity.gender == Constants.GENDER_WOMAN) {
            if(Util.isNightMode()) R.mipmap.default_woman_white
            else R.mipmap.default_woman_black
        }
        else {
            if(Util.isNightMode()) R.mipmap.default_man_white
            else R.mipmap.default_man_black
        }
    }
}