package kr.co.skchurch.seokwangyouthdoor.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import kr.co.skchurch.seokwangyouthdoor.R
import kr.co.skchurch.seokwangyouthdoor.SeokwangYouthApplication
import kr.co.skchurch.seokwangyouthdoor.data.Constants
import kr.co.skchurch.seokwangyouthdoor.data.entities.HomeEntity
import kr.co.skchurch.seokwangyouthdoor.databinding.ItemHomeFooterBinding
import kr.co.skchurch.seokwangyouthdoor.databinding.ItemHomeHeaderBinding
import kr.co.skchurch.seokwangyouthdoor.utils.Util

class HomeListAdapter(val onItemClicked: (Int, HomeEntity) -> Unit): ListAdapter<HomeEntity, HomeListAdapter.ViewHolder>(diffUtil) {

    companion object {
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        when(viewType) {
            Constants.ITEM_TYPE_HEADER -> return ViewHolder(ItemHomeHeaderBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
            else -> return ViewHolder(ItemHomeFooterBinding.inflate(
                LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    override fun getItemViewType(position: Int): Int {
        return currentList[position].type
    }

    private val ICON_ARR = arrayOf(
        SeokwangYouthApplication.context!!.resources.getDrawable(R.drawable.ic_cake_24, null),
        SeokwangYouthApplication.context!!.resources.getDrawable(R.drawable.ic_event_24, null)
    )
    private val ICON_ARR_BLACK = arrayOf(
        SeokwangYouthApplication.context!!.resources.getDrawable(R.drawable.ic_cake_24_black, null),
        SeokwangYouthApplication.context!!.resources.getDrawable(R.drawable.ic_event_24_black, null)
    )
    inner class ViewHolder(private val binding: ViewBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(entity: HomeEntity) {
            when(binding) {
                is ItemHomeHeaderBinding -> {
                    binding.itemTitleTxt.text = entity.title
                }
                is ItemHomeFooterBinding -> {
                    binding.itemThumbnail.visibility = View.GONE
                    val titleArr = entity.title?.split("^")
                    if(titleArr == null) binding.itemTitleTxt.text = ""
                    else {
                        if(titleArr.size > 1) {
                            val type = titleArr[0].toInt()
                            val titleTxt = titleArr[1]
                            val weekStartPos = titleArr[2].indexOf(".")
                            val date = titleArr[2].substring(weekStartPos+1)
                            var iconImg = if(type == Constants.SCHEDULE_TYPE_BIRTHDAY) ICON_ARR_BLACK[0] else ICON_ARR_BLACK[1]
                            if(Util.isNightMode()) iconImg = if(type == Constants.SCHEDULE_TYPE_BIRTHDAY) ICON_ARR[0] else ICON_ARR[1]
                            binding.itemThumbnail.setImageDrawable(iconImg)
                            binding.itemThumbnail.visibility = View.VISIBLE
                            binding.itemTitleTxt.text = "$date $titleTxt"
                        }
                        else {
                            val tempArr = entity.title!!.split("_")
                            if(tempArr.size == 1) binding.itemTitleTxt.text = entity.title
                            else binding.itemTitleTxt.text = tempArr[1]
                        }
                    }
                    binding.itemNew.visibility = if(entity.flagNew == 1) View.VISIBLE else View.GONE
                }
                else -> {}
            }
            binding.root.setOnClickListener {
                onItemClicked(adapterPosition, entity)
            }
        }
    }
}