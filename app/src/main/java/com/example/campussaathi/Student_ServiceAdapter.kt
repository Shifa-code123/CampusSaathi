package com.example.campussaathi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campussaathi.databinding.ItemStudentServiceBinding

class Student_ServiceAdapter(
    private var list: List<Student_ServiceModel>,
    private val onItemClick: (Student_ServiceModel) -> Unit
) : RecyclerView.Adapter<Student_ServiceAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemStudentServiceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStudentServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.binding.tvServiceName.text = item.serviceName
        
        if (item.photos.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(item.photos[0])
                .into(holder.binding.ivServiceImage)
        } else {
            holder.binding.ivServiceImage.setImageResource(R.drawable.map_placeholder)
        }

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = list.size

    fun updateList(newList: List<Student_ServiceModel>) {
        list = newList
        notifyDataSetChanged()
    }
}