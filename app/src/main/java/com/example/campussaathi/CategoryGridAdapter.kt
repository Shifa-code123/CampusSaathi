package com.example.campussaathi

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campussaathi.databinding.ItemCategoryBinding

class CategoryGridAdapter(
    private var list: List<CategoryModel>
) : RecyclerView.Adapter<CategoryGridAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemCategoryBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val category = list[position]

        holder.binding.tvCategoryName.text = category.name
        holder.binding.ivCategory.setImageResource(category.imageRes)

        // 🔥 CLICK → OPEN SERVICES PAGE
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, CategoryServicesActivity::class.java)
            intent.putExtra("categoryName", category.name)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<CategoryModel>) {
        list = newList
        notifyDataSetChanged()
    }
}