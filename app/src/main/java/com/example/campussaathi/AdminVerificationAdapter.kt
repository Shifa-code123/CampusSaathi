package com.example.campussaathi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminVerificationAdapter(
    private val list: List<AdminVerificationModel>,
    private val onApprove: (AdminVerificationModel) -> Unit,
    private val onReject: (AdminVerificationModel) -> Unit
) : RecyclerView.Adapter<AdminVerificationAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtOwnerName)
        val txtType: TextView = itemView.findViewById(R.id.txtOwnerType)
        val btnApprove: Button = itemView.findViewById(R.id.btnApprove)
        val btnReject: Button = itemView.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_verification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = list[position]

        holder.txtName.text = model.fullName
        holder.txtType.text = model.ownerType

        holder.btnApprove.setOnClickListener {
            onApprove(model)
        }

        holder.btnReject.setOnClickListener {
            onReject(model)
        }
    }

    override fun getItemCount(): Int = list.size
}
