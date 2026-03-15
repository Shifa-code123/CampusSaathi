package com.example.campussaathi

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campussaathi.databinding.ItemCityhelpBinding

class CityHelpAdapter(
    private val list: List<CityHelpModel>
) : RecyclerView.Adapter<CityHelpAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemCityhelpBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemCityhelpBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]

        holder.binding.serviceName.text = item.serviceName
        holder.binding.serviceDesc.text = item.description
        holder.binding.serviceDistance.text =
            "${item.distanceFromCampus} km away"


        if (item.photos.isNotEmpty()) {

            Glide.with(holder.itemView.context)
                .load(item.photos[0])
                .into(holder.binding.serviceImage)
        }

        holder.itemView.setOnClickListener {

            val intent = Intent(
                holder.itemView.context,
                ServiceDetailActivity::class.java
            )

            intent.putExtra("serviceName", item.serviceName)
            intent.putExtra("description", item.description)
            intent.putExtra("contact", item.contact)

            intent.putExtra("latitude", item.latitude)
            intent.putExtra("longitude", item.longitude)

            intent.putStringArrayListExtra(
                "photos",
                ArrayList(item.photos)
            )


            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int = list.size
}