package kr.co.skchurch.seokwangyouthdoor.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kr.co.skchurch.seokwangyouthdoor.R
import kr.co.skchurch.seokwangyouthdoor.data.Constants
import kr.co.skchurch.seokwangyouthdoor.data.MemberType
import kr.co.skchurch.seokwangyouthdoor.data.entities.MemberInfoEntity
import kr.co.skchurch.seokwangyouthdoor.databinding.CardItemMemberInfoBinding
import kr.co.skchurch.seokwangyouthdoor.utils.Util

class MemberInfoListAdapter(val onItemClicked: (Int, MemberInfoEntity) -> Unit): ListAdapter<MemberInfoEntity, MemberInfoListAdapter.ViewHolder>(diffUtil) {

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<MemberInfoEntity>() {
            override fun areItemsTheSame(
                oldItem: MemberInfoEntity,
                newItem: MemberInfoEntity
            ): Boolean {
                return oldItem.uuid == newItem.uuid
            }

            override fun areContentsTheSame(
                oldItem: MemberInfoEntity,
                newItem: MemberInfoEntity
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
        return ViewHolder(CardItemMemberInfoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class ViewHolder(private val binding: CardItemMemberInfoBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(entity: MemberInfoEntity) {
            binding.cardTitle.text = entity.name
            //binding.cardLayout.setBackgroundColor(binding.root.context.getColor(COLOR_ARR.random()))
            var color = 0
            color = when(entity.type) {
                Constants.MEMBER_TYPE_PASTER -> COLOR_ARR[0]
                Constants.MEMBER_TYPE_CHIEF_TEACHER -> COLOR_ARR[1]
                Constants.MEMBER_TYPE_WORSHIP_TEAM_LEADER -> COLOR_ARR[2]
                Constants.MEMBER_TYPE_TEACHER -> COLOR_ARR[3]
                else -> android.R.color.white
            }
            binding.cardLayout.setBackgroundColor(binding.root.context.getColor(color))
            Glide.with(binding.cardImage)
                .load(entity.imageUrl)
                .error(getDefaultImgId(entity))
                .into(binding.cardImage)

            binding.root.setOnClickListener {
                onItemClicked(adapterPosition, entity)
            }
        }
    }

    private fun getDefaultImgId(entity: MemberInfoEntity): Int {
        return if(entity.gender == Constants.GENDER_WOMAN) R.mipmap.default_woman_black
            else R.mipmap.default_man_black
    }
}