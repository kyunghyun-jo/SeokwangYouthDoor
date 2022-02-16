package kr.co.skchurch.seokwangyouthdoor.ui.home

import android.content.Intent
import android.os.Bundle
import com.orhanobut.logger.Logger
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.skchurch.seokwangyouthdoor.R
import kr.co.skchurch.seokwangyouthdoor.adapter.HomeListAdapter
import kr.co.skchurch.seokwangyouthdoor.data.Constants
import kr.co.skchurch.seokwangyouthdoor.data.FirebaseConstants
import kr.co.skchurch.seokwangyouthdoor.data.FirebaseManager
import kr.co.skchurch.seokwangyouthdoor.data.entities.HomeEntity
import kr.co.skchurch.seokwangyouthdoor.data.entities.SimpleEntity
import kr.co.skchurch.seokwangyouthdoor.databinding.FragmentHomeBinding
import kr.co.skchurch.seokwangyouthdoor.ui.memberinfo.MemberInfoActivity
import kr.co.skchurch.seokwangyouthdoor.ui.more.calendar.CalendarActivity
import kr.co.skchurch.seokwangyouthdoor.ui.widget.IDialogCallback
import kr.co.skchurch.seokwangyouthdoor.ui.widget.MessageDialog
import kr.co.skchurch.seokwangyouthdoor.utils.Util

class HomeFragment : Fragment() {

    companion object {
        private val TAG = HomeFragment::class.java.simpleName
    }

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var adapter:HomeListAdapter
    private var isAfterCreate = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isAfterCreate = true
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        initViews()

        return binding.root
    }

    private fun initViews() = with(binding) {
        adapter = HomeListAdapter(onItemClicked = { position, itemData ->
            Logger.d("HomeList clicked : $position / $itemData")
            if(itemData.value?.isNotEmpty() == true) {
                if(itemData.value == FirebaseConstants.PREFIX_EVENT) {
                    val tIntent = Intent(context, CalendarActivity::class.java)
                    startActivity(tIntent)
                }
                else if(itemData.value == Constants.NEW_MEMBER_VALUE) {
                    Logger.d("getNewMemberClassData : ${homeViewModel.getNewMemberClassData()}")
                    if(homeViewModel.getNewMemberClassData()!=null) {
                        var tIntent: Intent = Intent(context, MemberInfoActivity::class.java).apply {
                            putExtra(Constants.EXTRA_MEMBER_INFO, homeViewModel.getNewMemberClassData())
                        }
                        startActivity(tIntent)
                    }
                }
                else showMessageDialog(itemData)
            }
        })
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))

        binding.progressBar.visibility = View.VISIBLE

        homeViewModel.listData.observe(viewLifecycleOwner, Observer {
            binding.progressBar.visibility = View.GONE
            adapter.submitList(it)
            //Thread(Runnable {
            //    it.forEach { entity ->
            //        AppDatabase.getDatabase().homeDao().insertData(entity)
            //    }
            //}).start()
        })

        FirebaseManager.instance.registerNotify(object: FirebaseManager.IFirebaseNotify{
            override fun onDataChanged() {
                if(binding == null) return
                Logger.d("Notify onDataChanged")
                binding.progressBar.visibility = View.VISIBLE
                homeViewModel.requestCurrentData()
            }
        })
    }

    private var messageDialog: MessageDialog? = null
    private fun showMessageDialog(itemData: HomeEntity) {
        removeMessageDialog()
        val testData = SimpleEntity()
        testData.id = 1
        testData.title = itemData.value
        testData.uuid = Util.getUUID()
        testData.timeStamp = Util.getTimestamp()
        val listData = arrayListOf<SimpleEntity>(testData)
        val btnData = arrayListOf<Pair<Int, String>>(
            Pair(0, getString(R.string.ok))
        )
        messageDialog = MessageDialog(
                getString(R.string.detail_info),
                listData,
                object: IDialogCallback{
                    override fun dialogItemClicked(position: Int, data: Any?) {}

                    override fun dialogBtnClicked(id: Int) {
                        removeMessageDialog()
                    }
                },
                btnData
        )
        messageDialog?.show(parentFragmentManager, "MessageDialog")
    }

    private fun removeMessageDialog() {
        messageDialog?.dismiss()
        messageDialog = null
    }

    override fun onResume() {
        super.onResume()
        if(!isAfterCreate) {
            binding.progressBar.visibility = View.VISIBLE
            homeViewModel.requestCurrentData()
        }
        isAfterCreate = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}