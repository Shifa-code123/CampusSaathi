package com.example.campussaathi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MyServicesAdapter(private val services: List<Student_ServiceModel>) :
    RecyclerView.Adapter<MyServicesAdapter.ServiceViewHolder>() {

    class ServiceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgService: ImageView = view.findViewById(R.id.imgService)
        val txtStatusBadge: TextView = view.findViewById(R.id.txtStatusBadge)
        val txtServiceName: TextView = view.findViewById(R.id.txtServiceName)
        val txtCategory: TextView = view.findViewById(R.id.txtCategory)
        val txtDescription: TextView = view.findViewById(R.id.txtDescription)
        val txtContact: TextView = view.findViewById(R.id.txtContact)
        val txtDistance: TextView = view.findViewById(R.id.txtDistance)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]
        
        holder.txtServiceName.text = service.serviceName
        holder.txtCategory.text = service.category
        holder.txtDescription.text = service.description
        holder.txtContact.text = service.contact
        holder.txtDistance.text = service.distance

        // Status Badge
        holder.txtStatusBadge.text = service.status.replaceFirstChar { it.uppercase() }
        when (service.status.lowercase()) {
            "approved", "accepted" -> holder.txtStatusBadge.setBackgroundResource(R.drawable.status_accepted)
            "rejected" -> holder.txtStatusBadge.setBackgroundResource(R.drawable.status_rejected)
            else -> holder.txtStatusBadge.setBackgroundResource(R.drawable.status_pending)
        }

        // Load Image
        if (service.photos.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(service.photos[0])
                .placeholder(R.drawable.campus_logo)
                .error(R.drawable.campus_logo)
                .into(holder.imgService)
        } else {
            holder.imgService.setImageResource(R.drawable.campus_logo)
        }
    }

    override fun getItemCount() = services.size
}