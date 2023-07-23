package kr.co.skchurch.seokwangyouthdoor.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.co.skchurch.seokwangyouthdoor.data.entities.SimpleEntity
import kr.co.skchurch.seokwangyouthdoor.data.entities.TimetableEntity
import kr.co.skchurch.seokwangyouthdoor.databinding.ItemTimetableBinding
import kr.co.skchurch.seokwangyouthdoor.ui.timetable.TimetableViewModel

class TimetableListAdapter(val onItemClicked: (Int, String) -> Unit): ListAdapter<TimetableEntity, TimetableListAdapter.ViewHolder>(diffUtil) {

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

    private var mOriginTextColor: Int? = null
    inner class ViewHolder(private val binding: ItemTimetableBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(entity: TimetableEntity) {
            if(mOriginTextColor == null) mOriginTextColor = binding.itemTitleTxt.currentTextColor
            val titleValue = convertValue(entity.title)
            val lastValue = convertValue(entity.lastValue)
            binding.itemTitleTxt.text = titleValue.first
            setItemClickable(binding.itemTitleTxt, titleValue)
            binding.itemValueEndTxt.text = lastValue.first
            setItemClickable(binding.itemValueEndTxt, lastValue)
            if(entity.middleValue!=null && entity.middleValue!!.isNotEmpty()) {
                binding.itemValueMiddleTxt.visibility = View.VISIBLE
                val middleValue = convertValue(entity.middleValue!!)
                binding.itemValueMiddleTxt.text = middleValue.first
                setItemClickable(binding.itemValueMiddleTxt, middleValue)
            }
            else binding.itemValueMiddleTxt.visibility = View.GONE
        }
    }

    private fun convertValue(originValue: String): Pair<String, String?> {
        val tempArr = originValue.split("_")
        var result = Pair<String, String?>(tempArr[0], null)
        if(tempArr.size>1) {
            result = Pair(tempArr[0], tempArr[1])
        }
        return result
    }

    private fun hasOptionValue(pairValue: Pair<String, String?>): Boolean = !pairValue.second.isNullOrEmpty()

    private fun setItemClickable(itemView: TextView, itemValue: Pair<String, String?>) {
        if(hasOptionValue(itemValue)) {
            itemView.tag = itemValue.second
            itemView.setTextColor(Color.BLUE)
            itemView.setOnClickListener {
                onItemClicked(0, it.tag.toString())
            }
        }
        else {
            itemView.tag = null
            if(mOriginTextColor!=null) itemView.setTextColor(mOriginTextColor!!)
            itemView.setOnClickListener(null)
        }
    }
}