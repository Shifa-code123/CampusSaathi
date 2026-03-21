package com.example.campussaathi

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EditServiceListAdapter(private val services: List<Student_ServiceModel>) :
    RecyclerView.Adapter<EditServiceListAdapter.EditViewHolder>() {

    class EditViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtServiceName: TextView = view.findViewById(R.id.txtServiceName)
        val txtCategory: TextView = view.findViewById(R.id.txtCategory)
        val txtStatus: TextView = view.findViewById(R.id.txtStatus)
        val btnEdit: Button = view.findViewById(R.id.btnEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_edit_service, parent, false)
        return EditViewHolder(view)
    }

    override fun onBindViewHolder(holder: EditViewHolder, position: Int) {
        val service = services[position]
        holder.txtServiceName.text = service.serviceName
        holder.txtCategory.text = service.category
        holder.txtStatus.text = "Status: ${service.status}"

        holder.btnEdit.setOnClickListener {
            val intent = Intent(holder.itemView.context, EditServiceFormActivity::class.java)
            intent.putExtra("SERVICE_ID", service.id)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount() = services.size
}