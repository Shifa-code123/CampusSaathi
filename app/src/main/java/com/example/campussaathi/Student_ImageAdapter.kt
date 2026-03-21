package com.example.campussaathi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campussaathi.databinding.ItemSliderImageBinding

class Student_ImageAdapter(private val images: List<String>) : RecyclerView.Adapter<Student_ImageAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSliderImageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSliderImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(holder.itemView.context).load(images[position]).into(holder.binding.ivSliderImage)
    }

    override fun getItemCount(): Int = images.size
}