package kr.co.skchurch.seokwangyouthdoor.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.co.skchurch.seokwangyouthdoor.R
import kr.co.skchurch.seokwangyouthdoor.data.entities.FreeBoardEntity
import kr.co.skchurch.seokwangyouthdoor.databinding.ItemFreeBoardBinding

class FreeBoardListAdapter(
    val onItemClicked: (Int, FreeBoardEntity) -> Unit,
    val onItemLongClicked: (Int, FreeBoardEntity) -> Unit,
    val author: String? = null
): ListAdapter<FreeBoardEntity, FreeBoardListAdapter.ViewHolder>(diffUtil) {

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<FreeBoardEntity>() {
            override fun areItemsTheSame(
                oldItem: FreeBoardEntity,
                newItem: FreeBoardEntity
            ): Boolean {
                return oldItem.uuid == newItem.uuid
            }

            override fun areContentsTheSame(
                oldItem: FreeBoardEntity,
                newItem: FreeBoardEntity
            ): Boolean {
                return oldItem == newItem
            }

        }

        val BG_ARR_INCOMMING = arrayOf(
            R.drawable.bg_incomming_bubble_green,
            R.drawable.bg_incomming_bubble_teal,
            R.drawable.bg_incomming_bubble_yellow,
            R.drawable.bg_incomming_bubble_white
        )
        val BG_ARR_OUTGOING = arrayOf(
            R.drawable.bg_outgoing_bubble_green,
            R.drawable.bg_outgoing_bubble_teal,
            R.drawable.bg_outgoing_bubble_yellow,
            R.drawable.bg_outgoing_bubble_white
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemFreeBoardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class ViewHolder(private val binding: ItemFreeBoardBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(entity: FreeBoardEntity) = with(binding) {
            itemTitleTxt.text = entity.title
            itemValueTxt.text = entity.description
            if(author!=null && entity.author == author) {
                itemTitleTxt.setTextColor(binding.root.resources.getColor(R.color.white))
                itemValueTxt.setTextColor(binding.root.resources.getColor(R.color.white))
                binding.root.background = binding.root.context.getDrawable(R.drawable.bg_incomming_bubble_purple)
            }
            else
                binding.root.background = binding.root.context.getDrawable(BG_ARR_INCOMMING.random())
            root.setOnClickListener {
                onItemClicked(adapterPosition, entity)
            }
            root.setOnLongClickListener {
                onItemLongClicked(adapterPosition, entity)
                true
            }
        }
    }
}