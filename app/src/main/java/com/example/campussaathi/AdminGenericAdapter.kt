package com.example.campussaathi
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campussaathi.databinding.AdminItemManageCardBinding

data class AdminManageModel(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: String
)

class AdminGenericAdapter(
    private var items: List<AdminManageModel>,
    private val onItemClick: (AdminManageModel) -> Unit
) : RecyclerView.Adapter<AdminGenericAdapter.ViewHolder>() {

    class ViewHolder(val binding: AdminItemManageCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AdminItemManageCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvManageTitle.text = item.title
        holder.binding.tvManageSubtitle.text = item.subtitle
        holder.binding.root.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<AdminManageModel>) {
        items = newItems
        notifyDataSetChanged()
    }
}