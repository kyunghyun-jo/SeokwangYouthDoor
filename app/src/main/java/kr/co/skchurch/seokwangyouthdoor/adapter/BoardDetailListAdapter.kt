package kr.co.skchurch.seokwangyouthdoor.adapter

import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.orhanobut.logger.Logger
import kr.co.skchurch.seokwangyouthdoor.R
import kr.co.skchurch.seokwangyouthdoor.data.Constants
import kr.co.skchurch.seokwangyouthdoor.data.entities.HomeEntity
import kr.co.skchurch.seokwangyouthdoor.databinding.*

class BoardDetailListAdapter(
    val onBtnClick: (Int) -> Unit,
    val onItemClick: ((Int, Any) -> Unit)? = null
):
    ListAdapter<HomeEntity, BoardDetailListAdapter.ViewHolder>(diffUtil) {

    companion object {
        val TAG = BoardDetailListAdapter::class.java.simpleName
        val diffUtil = object : DiffUtil.ItemCallback<HomeEntity>() {
            override fun areItemsTheSame(
                oldItem: HomeEntity,
                newItem: HomeEntity
            ): Boolean {
                return oldItem.uuid == newItem.uuid
            }

            override fun areContentsTheSame(
                oldItem: HomeEntity,
                newItem: HomeEntity
            ): Boolean {
                return oldItem == newItem
            }
        }
        const val BTN_ID_ATTACH = 0
        const val BTN_ID_DEATTACH = 1
    }

    private var currentBoardMode = Constants.BOARD_MODE_VIEW

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when(viewType) {
            Constants.ITEM_TYPE_HEADER -> ViewHolder(ItemHomeHeaderBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
            Constants.ITEM_TYPE_EDIT_SINGLE -> ViewHolder(ItemEditSingleBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
            Constants.ITEM_TYPE_EDIT_MULTI -> ViewHolder(ItemEditMultiBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
            Constants.ITEM_TYPE_ATTACH_IMAGE -> ViewHolder(ItemAttachImageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
            Constants.ITEM_TYPE_IMAGE -> ViewHolder(ItemAttachImageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
            Constants.ITEM_TYPE_PROFILE_IMAGE -> ViewHolder(ItemAttachImageBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
            else -> ViewHolder(ItemBoardTextBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    override fun getItemViewType(position: Int): Int {
        return currentList[position].type
    }

    fun setBoardMode(boardMode: Int) {
        currentBoardMode = boardMode
    }

    fun getBoardMode(): Int = currentBoardMode

    inner class ViewHolder(private val binding: ViewBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(entity: HomeEntity) {
            when(binding) {
                is ItemHomeHeaderBinding -> {
                    binding.itemTitleTxt.text = entity.title
                    binding.root.setOnClickListener {
                        if(onItemClick!=null) onItemClick!!(adapterPosition, currentList[adapterPosition])
                    }
                }
                is ItemEditSingleBinding -> {
                    binding.itemEditText.setText(entity.title)
                    binding.itemEditText.addTextChangedListener(object: TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {}

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {}

                        override fun afterTextChanged(s: Editable?) {
                            entity.title = s.toString()
                            //onEditListener(entity, s.toString())
                        }

                    })
                }
                is ItemEditMultiBinding -> {
                    binding.itemEditText.setText(entity.title)
                    binding.itemEditText.addTextChangedListener(object: TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {}

                        override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {}

                        override fun afterTextChanged(s: Editable?) {
                            entity.title = s.toString()
                            //onEditListener(entity, s.toString())
                        }

                    })
                }
                is ItemAttachImageBinding -> {
                    Logger.d("ItemAttachImageBinding imageUrl : ${entity.imageUrl}")
                    Glide.with(binding.itemImage.context)
                        .load(
                            if(entity.imageUrl == null) null
                            else {
                                if(entity.imageUrl!!.startsWith("http")) entity.imageUrl
                                else Uri.parse(entity.imageUrl)
                            })
                        //.load(if(entity.imageUrl == null) null else entity.imageUrl)
                        .apply(RequestOptions.centerCropTransform())
                        .into(binding.itemImage)

                    when(entity.type) {
                        Constants.ITEM_TYPE_ATTACH_IMAGE -> {
                            binding.itemAttachBtnLayout.visibility = View.VISIBLE
                            binding.itemAttachBtn.setOnClickListener {
                                Logger.d("click itemAttachBtn")
                                onBtnClick(BTN_ID_ATTACH)
                            }
                            binding.itemDeattachBtn.setOnClickListener {
                                Logger.d("click itemDeattachBtn")
                                onBtnClick(BTN_ID_DEATTACH)
                            }
                        }
                        Constants.ITEM_TYPE_PROFILE_IMAGE -> {
                            binding.itemAttachBtnLayout.visibility = View.VISIBLE
                            binding.itemAttachBtn.textSize = 11f
                            //binding.itemDeattachBtn.textSize = 12f
                            binding.itemAttachBtn.setText(R.string.loading_image)
                            binding.itemAttachBtn.setOnClickListener {
                                Logger.d("click itemAttachBtn")
                                onBtnClick(BTN_ID_ATTACH)
                            }
                            binding.itemDeattachBtn.setText(R.string.apply)
                            binding.itemDeattachBtn.setOnClickListener {
                                Logger.d("click itemDeattachBtn")
                                onBtnClick(BTN_ID_DEATTACH)
                            }
                        }
                        else -> {
                            binding.itemAttachBtnLayout.visibility = View.GONE
                        }
                    }
                }
                is ItemBoardTextBinding -> {
                    binding.itemTxt.text = entity.title
                    if(entity.value!=null && entity.value.equals(Constants.OPTION_ALIGN_CENTER)) {
                        var layoutParams = binding.itemTxt.layoutParams
                        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                        binding.itemTxt.layoutParams = layoutParams
                        binding.itemTxt.gravity = Gravity.CENTER
                    }
                    binding.root.setOnClickListener {
                        if(onItemClick!=null) onItemClick!!(adapterPosition, currentList[adapterPosition])
                    }
                }
                else -> {}
            }
        }
    }
}