package kr.co.skchurch.seokwangyouthdoor.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.co.skchurch.seokwangyouthdoor.data.entities.BoardEntity
import kr.co.skchurch.seokwangyouthdoor.databinding.ItemBoardBinding

class BoardListAdapter(val onItemClicked: (Int, BoardEntity) -> Unit): ListAdapter<BoardEntity, BoardListAdapter.ViewHolder>(diffUtil) {

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<BoardEntity>() {
            override fun areItemsTheSame(
                oldItem: BoardEntity,
                newItem: BoardEntity
            ): Boolean {
                return oldItem.uuid == newItem.uuid
            }

            override fun areContentsTheSame(
                oldItem: BoardEntity,
                newItem: BoardEntity
            ): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemBoardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class ViewHolder(private val binding: ItemBoardBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(entity: BoardEntity) = with(binding) {
            itemTitleTxt.text = entity.title
            if(entity.author != null) {
                itemAuthorTxt.text = entity.author
                itemAuthorTxt.visibility = View.VISIBLE
            }
            else {
                itemAuthorTxt.visibility = View.GONE
            }
            root.setOnClickListener {
                onItemClicked(adapterPosition, entity)
            }
        }
    }
}