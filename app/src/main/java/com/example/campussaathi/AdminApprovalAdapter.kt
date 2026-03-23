package com.example.campussaathi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campussaathi.databinding.AdminItemApprovalBinding

class AdminApprovalAdapter(
    private var items: MutableList<AdminApprovalModel>,
    private val onApprove: (AdminApprovalModel) -> Unit,
    private val onReject: (AdminApprovalModel) -> Unit,
    private val onItemClick: (AdminApprovalModel) -> Unit
) : RecyclerView.Adapter<AdminApprovalAdapter.ViewHolder>() {

    class ViewHolder(val binding: AdminItemApprovalBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AdminItemApprovalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvTitle.text = item.title
        holder.binding.tvSubtitle.text = item.subtitle

        holder.binding.btnApprove.setOnClickListener { onApprove(item) }
        holder.binding.btnReject.setOnClickListener { onReject(item) }
        holder.binding.root.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<AdminApprovalModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun removeItem(item: AdminApprovalModel) {
        val index = items.indexOfFirst { it.id == item.id }
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}