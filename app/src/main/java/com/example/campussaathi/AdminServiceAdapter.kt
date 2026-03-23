package com.example.campussaathi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campussaathi.databinding.AdminItemServiceBinding

class AdminServiceAdapter(
    private var items: List<AdminServiceModel>,
    private val onItemClick: (AdminServiceModel) -> Unit
) : RecyclerView.Adapter<AdminServiceAdapter.ViewHolder>() {

    class ViewHolder(val binding: AdminItemServiceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AdminItemServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvAdminServiceName.text = item.serviceName
        holder.binding.tvAdminServiceCategory.text = item.category
        holder.binding.tvAdminServiceStatus.text = item.status
        
        if (item.photos.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(item.photos[0])
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.binding.ivAdminServiceImage)
        } else {
            holder.binding.ivAdminServiceImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.binding.root.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<AdminServiceModel>) {
        items = newItems
        notifyDataSetChanged()
    }
}