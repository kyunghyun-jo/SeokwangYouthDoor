package kr.co.skchurch.seokwangyouthdoor.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.co.skchurch.seokwangyouthdoor.data.entities.TimetableEntity
import kr.co.skchurch.seokwangyouthdoor.databinding.ItemTimetableBinding
import kr.co.skchurch.seokwangyouthdoor.ui.timetable.TimetableViewModel

class TimetableListAdapter: ListAdapter<TimetableEntity, TimetableListAdapter.ViewHolder>(diffUtil) {

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<TimetableEntity>() {
            override fun areItemsTheSame(
                oldItem: TimetableEntity,
                newItem: TimetableEntity
            ): Boolean {
                return oldItem.uuid == newItem.uuid
            }

            override fun areContentsTheSame(
                oldItem: TimetableEntity,
                newItem: TimetableEntity
            ): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemTimetableBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class ViewHolder(private val binding: ItemTimetableBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(entity: TimetableEntity) {
            binding.itemTitleTxt.text = entity.title
            binding.itemValueEndTxt.text = entity.lastValue
            if(entity.middleValue!=null && entity.middleValue!!.isNotEmpty()) {
                binding.itemValueMiddleTxt.visibility = View.VISIBLE
                binding.itemValueMiddleTxt.text = entity.middleValue
            }
            else binding.itemValueMiddleTxt.visibility = View.GONE
        }
    }
}