package com.example.campussaathi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

class ServiceNameAdapter(private val list: List<String>, private val serviceIds: List<String> = emptyList()) :
    RecyclerView.Adapter<ServiceNameAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtServiceName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_name, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.txtName.text = list[position]
        
        holder.itemView.setOnClickListener {
            if (position < serviceIds.size) {
                val serviceId = serviceIds[position]
                val activity = holder.itemView.context as? AppCompatActivity
                val fragment = Student_ServiceDetailFragment().apply {
                    arguments = Bundle().apply {
                        putString("serviceId", serviceId)
                    }
                }
                activity?.supportFragmentManager?.beginTransaction()
                    ?.replace(android.R.id.content, fragment)
                    ?.addToBackStack(null)
                    ?.commit()
            }
        }
    }
}