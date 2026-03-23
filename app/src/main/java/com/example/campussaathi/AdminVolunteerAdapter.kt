package com.example.campussaathi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campussaathi.databinding.AdminItemVolunteerApprovalBinding

class AdminVolunteerAdapter(
    private var items: MutableList<AdminVolunteerModel>,
    private val onApprove: (AdminVolunteerModel) -> Unit,
    private val onReject: (AdminVolunteerModel) -> Unit,
    private val onItemClick: (AdminVolunteerModel) -> Unit
) : RecyclerView.Adapter<AdminVolunteerAdapter.ViewHolder>() {

    class ViewHolder(val binding: AdminItemVolunteerApprovalBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AdminItemVolunteerApprovalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.adminTvVolunteerTitle.text = item.title
        holder.binding.adminTvVolunteerSubtitle.text = item.subtitle

        holder.binding.adminBtnApproveVolunteer.setOnClickListener { onApprove(item) }
        holder.binding.adminBtnRejectVolunteer.setOnClickListener { onReject(item) }
        holder.binding.root.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<AdminVolunteerModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun removeItem(item: AdminVolunteerModel) {
        val index = items.indexOfFirst { it.id == item.id }
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}