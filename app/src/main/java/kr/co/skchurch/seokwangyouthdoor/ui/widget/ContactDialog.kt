package kr.co.skchurch.seokwangyouthdoor.ui.widget

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.orhanobut.logger.Logger
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.skchurch.seokwangyouthdoor.R
import kr.co.skchurch.seokwangyouthdoor.adapter.ContactListAdapter
import kr.co.skchurch.seokwangyouthdoor.data.entities.MemberInfoEntity
import kr.co.skchurch.seokwangyouthdoor.databinding.FragmentContactDialogBinding
import kr.co.skchurch.seokwangyouthdoor.ui.more.MoreViewModel
import kr.co.skchurch.seokwangyouthdoor.utils.Util
import java.util.*

class ContactDialog(
        private val _title: String,
        private val _callback: IDialogCallback): DialogFragment() {
    companion object {
        private val TAG = ContactDialog::class.java.simpleName
    }

    private var _binding: FragmentContactDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ContactListAdapter
    private lateinit var callback: IDialogCallback
    private lateinit var moreViewModel: MoreViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentContactDialogBinding.inflate(inflater, container, false)

        callback = _callback

        moreViewModel =
            ViewModelProvider(this).get(MoreViewModel::class.java)

        setTitle(_title)
        initViews()
        bindViews()

        return binding.root
    }

    private fun initViews() = with(binding) {
        // 레이아웃 배경을 투명하게 해줌, 필수 아님
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        adapter = ContactListAdapter(onItemClicked = { position, itemData ->
            callback?.dialogItemClicked(position, itemData)
        })
        contactList.layoutManager = LinearLayoutManager(context)
        contactList.adapter = adapter
        if(Util.isNightMode()) searchItem.itemDeleteBtn.setImageResource(R.drawable.ic_delete_24)
        else searchItem.itemDeleteBtn.setImageResource(R.drawable.ic_delete_24_black)
    }

    private fun bindViews() = with(binding) {
        bottomBtn.setOnClickListener {
            callback?.dialogBtnClicked(0)
        }
        searchItem.itemDeleteBtn.setOnClickListener {
            searchItem.itemEditText.setText("")
            searchInputEvent()
        }
        searchItem.itemEditText.addTextChangedListener(
            afterTextChanged = {
                val timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        searchInputEvent()
                    }
                }, 1000)
            }
        )

        moreViewModel.listData.observe(viewLifecycleOwner, Observer {
            Logger.d("listdata observe : $it")
            adapter.submitList(it)
            contactList.scrollToPosition(0)
            if(it.size!!>1) {
                contactList.addItemDecoration(
                    DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
            }
        })
        searchInputEvent()
    }

    private fun searchInputEvent() {
        val inputText = binding.searchItem.itemEditText.text
        moreViewModel.requestContactData(inputText?.toString())
    }

    fun setTitle(title: String) {
        binding.dialogTitle.text = title
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}