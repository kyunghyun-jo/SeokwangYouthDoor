package kr.co.skchurch.seokwangyouthdoor.ui.timetable

import android.os.Bundle
import com.orhanobut.logger.Logger
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.skchurch.seokwangyouthdoor.adapter.TimetableListAdapter
import kr.co.skchurch.seokwangyouthdoor.data.AppDatabase
import kr.co.skchurch.seokwangyouthdoor.data.FirebaseManager
import kr.co.skchurch.seokwangyouthdoor.data.SharedPrefManager
import kr.co.skchurch.seokwangyouthdoor.databinding.FragmentTimetableBinding

class TimetableFragment : Fragment() {

    companion object {
        private val TAG = TimetableFragment::class.java.simpleName
    }

    private lateinit var timetableViewModel: TimetableViewModel
    private var _binding: FragmentTimetableBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var adapter: TimetableListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        timetableViewModel =
            ViewModelProvider(this).get(TimetableViewModel::class.java)

        _binding = FragmentTimetableBinding.inflate(inflater, container, false)

        initViews()

        return binding.root
    }

    private fun initViews() = with(binding) {
        adapter = TimetableListAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        binding.progressBar.visibility = View.VISIBLE

        timetableViewModel.listData.observe(viewLifecycleOwner, Observer {
            binding.progressBar.visibility = View.GONE
            adapter.submitList(it)
            /*
            Thread(Runnable {
                it.forEach { entity ->
                    AppDatabase.getDatabase().timetableDao().insertData(entity)
                }
            }).start()
             */
        })

        FirebaseManager.instance.registerNotify(object: FirebaseManager.IFirebaseNotify{
            override fun onDataChanged() {
                if(binding == null) return
                Logger.d("Notify onDataChanged")
                binding.progressBar.visibility = View.VISIBLE
                timetableViewModel.requestCurrentData()
                bannerTxt.text = SharedPrefManager.getInstance(requireContext()).getWorshipNotice()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        binding.progressBar.visibility = View.VISIBLE
        timetableViewModel.requestCurrentData()
        var noticeTxt = SharedPrefManager.getInstance(requireContext()).getWorshipNotice()
        if(noticeTxt.isNullOrBlank() || noticeTxt == "null") noticeTxt = ""
        binding.bannerTxt.text = noticeTxt
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}