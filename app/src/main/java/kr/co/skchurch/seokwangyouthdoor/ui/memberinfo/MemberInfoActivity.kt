package kr.co.skchurch.seokwangyouthdoor.ui.memberinfo

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.orhanobut.logger.Logger
import kr.co.skchurch.seokwangyouthdoor.adapter.MemberInfoListAdapter
import kr.co.skchurch.seokwangyouthdoor.data.Constants
import kr.co.skchurch.seokwangyouthdoor.data.FirebaseManager
import kr.co.skchurch.seokwangyouthdoor.data.entities.SimpleEntity
import kr.co.skchurch.seokwangyouthdoor.databinding.ActivityMemberInfoBinding
import kr.co.skchurch.seokwangyouthdoor.ui.memberinfo.detail.MemberDetailInfoActivity

class MemberInfoActivity : AppCompatActivity() {

    companion object {
        private val TAG = MemberInfoActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityMemberInfoBinding
    private lateinit var adapter: MemberInfoListAdapter
    private lateinit var memberInfoViewModel: MemberInfoViewModel
    private lateinit var receivedData: SimpleEntity
    private var isAfterCreate = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isAfterCreate = true
        binding = ActivityMemberInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() = with(binding) {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        receivedData = intent.getParcelableExtra<SimpleEntity>(Constants.EXTRA_MEMBER_INFO)!!
        collapsingToolbar.title = receivedData.title

        topInfoText.text = "${collapsingToolbar.title}${receivedData.value}"
        Glide.with(topInfoImage)
            .load(receivedData.imageUrl)
            .apply(RequestOptions.centerCropTransform())
            .into(topInfoImage)

        adapter = MemberInfoListAdapter(onItemClicked = { position, itemData ->
            var intent: Intent = Intent(this@MemberInfoActivity, MemberDetailInfoActivity::class.java).apply {
                putExtra(Constants.EXTRA_DETAIL_INFO, itemData)
            }
            startActivity(intent)
        })
        recyclerView.layoutManager = GridLayoutManager(this@MemberInfoActivity, 2)
        recyclerView.adapter = adapter

        memberInfoViewModel = ViewModelProvider(this@MemberInfoActivity).get(MemberInfoViewModel::class.java)
        memberInfoViewModel.targetClassName = collapsingToolbar.title.toString()
        Logger.d("initViews targetClassName : ${collapsingToolbar.title.toString()}")

        binding.progressBar.visibility = View.VISIBLE

        memberInfoViewModel.listData.observe(this@MemberInfoActivity, Observer {
            Logger.d("filtered result : $it")
            binding.progressBar.visibility = View.GONE
            adapter.submitList(it)
        })

        FirebaseManager.instance.registerNotify(object: FirebaseManager.IFirebaseNotify{
            override fun onDataChanged() {
                Logger.d("Notify onDataChanged")
                binding.progressBar.visibility = View.VISIBLE
                memberInfoViewModel.requestCurrentData()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if(!isAfterCreate) {
            binding.progressBar.visibility = View.VISIBLE
            memberInfoViewModel.requestCurrentData()
        }
        isAfterCreate = false
    }
}