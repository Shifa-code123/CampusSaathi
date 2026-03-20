package com.example.campussaathi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campussaathi.databinding.ItemServiceBinding

class CategoryAdapter(
    private val list: List<Service>
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemServiceBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemServiceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val service = list[position]

        // 🔹 Name
        holder.binding.tvServiceName.text = service.serviceName

        // 🔹 Description
        holder.binding.tvDescription.text = service.description

        // 🔹 Image (Cloudinary)
        if (service.photos.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(service.photos[0])
                .into(holder.binding.ivService)
        }

        // 🔹 Button click (optional)
        holder.binding.btnViewDetails.setOnClickListener {
            // TODO: open details page
        }
    }

    override fun getItemCount(): Int = list.size
}