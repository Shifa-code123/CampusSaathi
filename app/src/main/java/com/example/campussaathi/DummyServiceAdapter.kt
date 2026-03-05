package com.example.campussaathi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DummyServiceAdapter : RecyclerView.Adapter<DummyServiceAdapter.ViewHolder>() {

    private val services = listOf(
        "Special Thali",
        "Monthly Tiffin",
        "Dinner Plan",
        "Breakfast Service"
    )

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text: TextView = itemView.findViewById(R.id.txtService)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = services.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.text.text = services[position]
    }
}