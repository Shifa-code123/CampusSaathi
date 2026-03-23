package com.example.campussaathi
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campussaathi.databinding.AdminItemCategoryBinding

class AdminCategoryAdapter(
    private val categories: List<Category>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<AdminCategoryAdapter.ViewHolder>() {

    data class Category(val name: String, val imageRes: Int)

    class ViewHolder(val binding: AdminItemCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AdminItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.binding.tvCategoryName.text = category.name
        holder.binding.ivCategoryBg.setImageResource(category.imageRes)
        holder.binding.root.setOnClickListener { onItemClick(category.name) }
    }

    override fun getItemCount(): Int = categories.size
}