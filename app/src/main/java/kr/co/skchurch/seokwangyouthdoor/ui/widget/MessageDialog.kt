package kr.co.skchurch.seokwangyouthdoor.ui.widget

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.orhanobut.logger.Logger
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.skchurch.seokwangyouthdoor.adapter.NormalListAdapter
import kr.co.skchurch.seokwangyouthdoor.data.entities.SimpleEntity
import kr.co.skchurch.seokwangyouthdoor.databinding.FragmentMessageDialogBinding
import kr.co.skchurch.seokwangyouthdoor.utils.Util

class MessageDialog(
        private val _title: String,
        private val _listData: List<SimpleEntity>,
        private val _callback: IDialogCallback,
        private val _btnData: List<Pair<Int, String>>? = null
        ): DialogFragment() {

    companion object {
        private val TAG = MessageDialog::class.java.simpleName
    }

    private var _binding: FragmentMessageDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NormalListAdapter
    private lateinit var callback: IDialogCallback
    private var btnData: List<Pair<Int, String>>? = null
    private var listData: List<SimpleEntity>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMessageDialogBinding.inflate(inflater, container, false)

        listData = _listData
        btnData = _btnData
        callback = _callback

        setTitle(_title)
        initViews()
        bindViews()

        return binding.root
    }

    private fun initViews() = with(binding) {
        // 레이아웃 배경을 투명하게 해줌, 필수 아님
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        resetContainerHeight()
        adapter = NormalListAdapter(onItemClicked = {position, itemData ->
            callback?.dialogItemClicked(position, itemData)
        })
        messageList.layoutManager = LinearLayoutManager(context)
        messageList.adapter = adapter
        if(listData?.size!!>1) {
            messageList.addItemDecoration(
                    DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
        setBottomButton()
        refresh()
    }

    private fun setBottomButton() = with(binding) {
        if(btnData == null) bottomBtnLayout.visibility = View.INVISIBLE
        else {
            bottomBtnLayout.visibility = View.VISIBLE
            btn1.setText(btnData!![0].second)
            btn1.visibility = View.VISIBLE
            btn1.setOnClickListener {
                callback?.dialogBtnClicked(btnData!![0].first)
            }
            if(btnData!!.size>1) {
                btn2.setText(btnData!![1].second)
                btn2.visibility = View.VISIBLE
                btn2.setOnClickListener {
                    callback?.dialogBtnClicked(btnData!![1].first)
                }
            }
        }
    }

    private fun bindViews() = with(binding) {}

    fun setListData(_listData: List<SimpleEntity>) {
        listData = _listData
        resetContainerHeight()
        refresh()
    }

    fun setTitle(title: String) {
        binding.dialogTitle.text = title
    }

    private fun refresh() {
        adapter.submitList(listData)
    }

    private fun resetContainerHeight() = with(binding) {
        val displayHeight = Util.displayMetrics.second
        var containerLayoutParam = popupContainer.layoutParams
        var dataTextLeng = 0
        listData?.forEach {
            dataTextLeng += it.title!!.length
        }
        dataTextLeng += ((listData!!.size-1)*50)
        Logger.d("resetContainerHeight dataTextLeng : $dataTextLeng")
        val gapHeight = when {
            dataTextLeng < 100 -> 450f
            dataTextLeng < 200 -> 350f
            dataTextLeng > 300 -> 100f
            else -> 200f
        }
        containerLayoutParam.height = displayHeight - Util.dpToPx(requireContext(), gapHeight).toInt()
        popupContainer.layoutParams = containerLayoutParam
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}