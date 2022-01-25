package kr.co.skchurch.seokwangyouthdoor.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kr.co.skchurch.seokwangyouthdoor.R
import kr.co.skchurch.seokwangyouthdoor.data.Constants
import kr.co.skchurch.seokwangyouthdoor.data.entities.MemberInfoEntity
import kr.co.skchurch.seokwangyouthdoor.databinding.ItemContactBinding

class ContactListAdapter(val onItemClicked: (Int, MemberInfoEntity) -> Unit): ListAdapter<MemberInfoEntity, ContactListAdapter.ViewHolder>(diffUtil) {

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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemContactBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class ViewHolder(private val binding: ItemContactBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(entity: MemberInfoEntity) {
            var job = ""
            when(entity.type) {
                Constants.MEMBER_TYPE_PASTER ->
                    job = binding.root.context.getString(R.string.paster)
                Constants.MEMBER_TYPE_CHIEF_TEACHER ->
                    job = binding.root.context.getString(R.string.chief_teacher)
                Constants.MEMBER_TYPE_WORSHIP_TEAM_LEADER ->
                    job = binding.root.context.getString(R.string.worship_team_leader)
                Constants.MEMBER_TYPE_TEACHER ->
                    job = binding.root.context.getString(R.string.teachers)
                else ->
                    job = binding.root.context.getString(R.string.students)
            }
            binding.itemName.text = entity.name + " " + job
            binding.itemPhoneNumber.text = entity.phoneNumber
            Glide.with(binding.itemProfileImg)
                .load(entity.imageUrl)
                .error(getDefaultImgId(entity))
                .into(binding.itemProfileImg)

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