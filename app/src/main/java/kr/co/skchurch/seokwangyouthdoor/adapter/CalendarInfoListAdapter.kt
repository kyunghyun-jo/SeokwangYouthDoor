package kr.co.skchurch.seokwangyouthdoor.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.co.skchurch.seokwangyouthdoor.R
import kr.co.skchurch.seokwangyouthdoor.data.Constants
import kr.co.skchurch.seokwangyouthdoor.data.entities.CalendarEntity
import kr.co.skchurch.seokwangyouthdoor.databinding.ItemCalendarBinding
import kr.co.skchurch.seokwangyouthdoor.utils.Util

class CalendarInfoListAdapter: ListAdapter<CalendarEntity, CalendarInfoListAdapter.ViewHolder>(diffUtil) {

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<CalendarEntity>() {
            override fun areItemsTheSame(
                oldItem: CalendarEntity,
                newItem: CalendarEntity
            ): Boolean {
                return oldItem.uuid == newItem.uuid
            }

            override fun areContentsTheSame(
                oldItem: CalendarEntity,
                newItem: CalendarEntity
            ): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemCalendarBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class ViewHolder(private val binding: ItemCalendarBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(entity: CalendarEntity) {
            binding.itemTitleTxt.text = entity.title
            if(entity.detailInfo != null) {
                binding.itemDetailTxt.text = entity.detailInfo
                binding.itemDetailTxt.visibility = View.VISIBLE
            }
            else {
                binding.itemDetailTxt.visibility = View.GONE
            }
            binding.itemIcon.setImageDrawable(
                if(entity.scheduleType == Constants.SCHEDULE_TYPE_BIRTHDAY)
                    binding.itemIcon.context.getDrawable(
                        if(Util.isNightMode()) R.drawable.ic_cake_24
                        else R.drawable.ic_cake_24_black
                    )
                else {
                    binding.itemIcon.context.getDrawable(
                        if(Util.isNightMode()) R.drawable.ic_event_24
                        else R.drawable.ic_event_24_black
                    )
                }
            )
        }
    }
}