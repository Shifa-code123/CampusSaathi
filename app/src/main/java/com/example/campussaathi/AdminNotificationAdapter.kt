package com.example.campussaathi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campussaathi.databinding.AdminItemNotificationBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AdminNotificationModel(
    val title: String = "",
    val description: String = "",
    val timestamp: Long = 0L
)

class AdminNotificationAdapter(private var notifications: List<AdminNotificationModel>) :
    RecyclerView.Adapter<AdminNotificationAdapter.ViewHolder>() {

    class ViewHolder(val binding: AdminItemNotificationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AdminItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]
        holder.binding.tvAdminNotificationTitle.text = notification.title
        holder.binding.tvAdminNotificationDesc.text = notification.description
        
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        holder.binding.tvAdminNotificationTime.text = sdf.format(Date(notification.timestamp))
    }

    override fun getItemCount(): Int = notifications.size

    fun updateData(newList: List<AdminNotificationModel>) {
        notifications = newList
        notifyDataSetChanged()
    }
}