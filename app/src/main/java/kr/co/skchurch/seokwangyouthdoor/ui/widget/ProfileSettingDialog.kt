package kr.co.skchurch.seokwangyouthdoor.ui.widget

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.orhanobut.logger.Logger
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kr.co.skchurch.seokwangyouthdoor.SeokwangYouthApplication
import kr.co.skchurch.seokwangyouthdoor.adapter.BoardDetailListAdapter
import kr.co.skchurch.seokwangyouthdoor.data.Constants
import kr.co.skchurch.seokwangyouthdoor.data.FirebaseManager
import kr.co.skchurch.seokwangyouthdoor.data.entities.HomeEntity
import kr.co.skchurch.seokwangyouthdoor.databinding.FragmentMessageDialogBinding
import kr.co.skchurch.seokwangyouthdoor.ui.board.detail.BoardDetailActivity
import kr.co.skchurch.seokwangyouthdoor.utils.Util

class ProfileSettingDialog(
        private val _title: String,
        private val _listData: List<HomeEntity>,
        private val _callback: IDialogCallback,
        private val _btnData: List<Pair<Int, String>>? = null
        ): DialogFragment() {

    companion object {
        private val TAG = ProfileSettingDialog::class.java.simpleName
    }

    private var _binding: FragmentMessageDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: BoardDetailListAdapter
    private lateinit var callback: IDialogCallback
    private var btnData: List<Pair<Int, String>>? = null
    private var listData: MutableList<HomeEntity> = mutableListOf()
    private var selectedImageUri: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMessageDialogBinding.inflate(inflater, container, false)

        listData.addAll(_listData)
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
        adapter = BoardDetailListAdapter(onBtnClick = {
                when(it) {
                    BoardDetailListAdapter.BTN_ID_ATTACH -> {
                        if(Util.isOnline(requireContext())) pickFromGallery()
                    }
                    BoardDetailListAdapter.BTN_ID_DEATTACH -> {
                        if(Util.isOnline(requireContext())) saveProfileImage(selectedImageUri)
                    }
                    else -> {}
                }
            },
            onItemClick = {position, itemData ->
                Logger.d("selected Item : $position / $itemData")
                callback?.dialogItemClicked(position, itemData)
            }
        )
        
        messageList.layoutManager = LinearLayoutManager(context)
        messageList.adapter = adapter
        if(listData?.size!!>1) {
            messageList.addItemDecoration(
                    DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
        setBottomButton()
        resetContainerHeight()
        refresh()
    }

    private fun resetContainerHeight() = with(binding) {
        var containerLayoutParam = popupContainer.layoutParams
        containerLayoutParam.height = Util.dpToPx(requireContext(), 450f).toInt()
        popupContainer.layoutParams = containerLayoutParam
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

    private val photoRequestLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK) {
                Logger.d("photoRequest result : ${result.data}")
                val imageItem: HomeEntity? = listData.find { entity ->
                    entity.type == Constants.ITEM_TYPE_PROFILE_IMAGE
                }
                imageItem?.imageUrl = result.data?.data.toString()
                selectedImageUri = imageItem?.imageUrl
                //adapter.submitList(mutableList.toList())
                adapter.notifyDataSetChanged()

                //saveProfileImage(imageItem?.imageUrl!!)
            }
        }

    private fun saveProfileImage(imageUrl: String?) {
        SeokwangYouthApplication.getMyProfile {
            if(imageUrl == null || it?.imageUrl == imageUrl) return@getMyProfile
            Handler(Looper.getMainLooper()).post(Runnable {
                binding.progressBar.visibility = View.VISIBLE
            })
            val selectedImgUri = Uri.parse(imageUrl)
            val storageKey = "profile/${it!!.name}/image"
            uploadPhoto(it.imageUrl, selectedImgUri, storageKey,
                successHandler = { storageUri ->
                    Logger.d("successHandler : $storageUri")
                    binding.progressBar.visibility = View.GONE
                    FirebaseManager.instance.updateProfileImage(it, storageUri)
                },
                errorHandler = {
                    Logger.w("errorHandler : $it")
                    binding.progressBar.visibility = View.GONE
                    //Toast.makeText(this@BoardDetailActivity, "사진 업로드에 실패했습니다. ", Toast.LENGTH_SHORT).show()
                })
        }
    }

    private fun pickFromGallery() {
        val mimeTypes = arrayOf("image/jpeg", "image/png")
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        }
        //startActivityForResult(intent, PICK_IMAGE)
        photoRequestLauncher.launch(intent)
    }

    private val storage: FirebaseStorage by lazy {
        Firebase.storage
    }
    private fun uploadPhoto(currentImg:String?, uri: Uri, storageKey: String, successHandler: (String) -> Unit, errorHandler: () -> Unit) {
        val fileName = "${System.currentTimeMillis()}.png"
        Logger.d("uploadPhoto storageKey : $storageKey / fileName : $fileName")
        if(currentImg!=null && currentImg.startsWith("http")) {
            val tempArr = currentImg!!.split("/")
            val tempArr2 = tempArr[tempArr.lastIndex].split("%2F")
            val endPos = tempArr2[tempArr2.lastIndex].indexOf("?")
            if(endPos >= 0) {
                val targetImg = tempArr2[tempArr2.lastIndex].substring(0, endPos)
                Logger.d("uploadPhoto current Profile delete : $targetImg")
                storage.reference.child(storageKey).child(targetImg).delete().addOnCompleteListener {
                    if(it.isSuccessful) {
                        Logger.d("uploadPhoto current Profile delete complete!")
                    }
                }
            }
        }
        storage.reference.child(storageKey).child(fileName)
            .putFile(uri)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    storage.reference.child(storageKey).child(fileName)
                        .downloadUrl
                        .addOnSuccessListener { uri ->
                            successHandler(uri.toString())
                        }
                        .addOnFailureListener {
                            errorHandler()
                        }
                }
                else {
                    errorHandler()
                }
            }
    }

    private fun bindViews() = with(binding) {}

    fun setListData(_listData: List<HomeEntity>) {
        listData.clear()
        listData.addAll(_listData)
        refresh()
    }

    fun setTitle(title: String) {
        binding.dialogTitle.text = title
    }

    private fun refresh() {
        adapter.submitList(listData.toList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}