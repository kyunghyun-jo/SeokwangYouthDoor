package kr.co.skchurch.seokwangyouthdoor.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.co.skchurch.seokwangyouthdoor.data.entities.SimpleEntity
import kr.co.skchurch.seokwangyouthdoor.databinding.ItemNormalTextBinding
import kr.co.skchurch.seokwangyouthdoor.utils.Util.getRealTxt

class NormalListAdapter(val onItemClicked: (Int, SimpleEntity) -> Unit): ListAdapter<SimpleEntity, NormalListAdapter.ViewHolder>(diffUtil) {

    companion object {
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemNormalTextBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class ViewHolder(private val binding: ItemNormalTextBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(entity: SimpleEntity) {
            binding.itemTxt.text = getRealTxt(entity.title.orEmpty())
            if(entity.value?.isNotEmpty() == true) {
                binding.itemTxt.text = entity.title + " : " + entity.value
            }
            binding.root.setOnClickListener {
                onItemClicked(adapterPosition, currentList[adapterPosition])
            }
        }
    }
}