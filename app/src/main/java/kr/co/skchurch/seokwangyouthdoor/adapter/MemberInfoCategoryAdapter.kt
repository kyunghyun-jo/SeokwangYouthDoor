package kr.co.skchurch.seokwangyouthdoor.adapter

import android.graphics.Color
import android.view.*
import com.orhanobut.logger.Logger
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import kr.co.skchurch.seokwangyouthdoor.R
import kr.co.skchurch.seokwangyouthdoor.SeokwangYouthApplication
import kr.co.skchurch.seokwangyouthdoor.data.entities.SimpleEntity
import kr.co.skchurch.seokwangyouthdoor.databinding.ItemMemberInfoCategoryBinding
import kr.co.skchurch.seokwangyouthdoor.utils.Util

class MemberInfoCategoryAdapter(val onItemClicked: (Int, SimpleEntity) -> Unit): ListAdapter<SimpleEntity, MemberInfoCategoryAdapter.ViewHolder>(diffUtil) {

    companion object {
        private val TAG = MemberInfoCategoryAdapter::class.java.simpleName
        val diffUtil = object : DiffUtil.ItemCallback<SimpleEntity>() {
            override fun areItemsTheSame(
                oldItem: SimpleEntity,
                newItem: SimpleEntity
            ): Boolean {
                return oldItem.uuid == newItem.uuid
            }

            override fun areContentsTheSame(
                oldItem: SimpleEntity,
                newItem: SimpleEntity
            ): Boolean {
                return oldItem == newItem
            }
        }

        val COLOR_ARR = arrayOf(
            R.color.bg_yellow,
            R.color.bg_green,
            R.color.bg_teal,
            R.color.bg_purple
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemMemberInfoCategoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
    }

    var itemHeight: Int = -1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(itemHeight>0) {
            val layoutParams = holder.itemView.layoutParams
            layoutParams.height = itemHeight
        }
        holder.bind(currentList[position])
    }

    inner class ViewHolder(private val binding: ItemMemberInfoCategoryBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(entity: SimpleEntity) {
            binding.itemTitleTxt.text = entity.title
            //binding.itemTitleTxt.setTextColor(Color.BLACK)
            val bgColor = binding.root.context.getColor(COLOR_ARR.random())
            binding.root.setBackgroundColor(bgColor)
            binding.root.tag = bgColor
            //binding.root.setBackgroundResource(R.drawable.card_item_selector)
            Logger.d("bind MemberCategory entity.title : ${entity.title} / paster : ${binding.root.context.getString(R.string.paster)}")
            if(Util.isOnline(binding.root.context) &&
                entity.title == binding.root.context.getString(R.string.paster)) {
                Glide.with(binding.itemThumbnail)
                    .load(entity.imageUrl)
                    .signature(ObjectKey(System.currentTimeMillis()))
                    .into(binding.itemThumbnail)
            }
            else {
                Glide.with(binding.itemThumbnail)
                    .load(entity.imageUrl)
                    .into(binding.itemThumbnail)
            }
            binding.root.setOnClickListener{
                onItemClicked(adapterPosition, entity)
            }
            binding.root.setOnTouchListener { v, event ->
                when(event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        v.setBackgroundColor(v.context.getColor(R.color.card_focus_color))
                    }
                    else -> {
                        if(v.tag!=null) v.setBackgroundColor(v.tag.toString().toInt())
                    }
                }
                false
            }
        }
    }
}