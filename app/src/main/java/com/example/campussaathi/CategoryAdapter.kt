package com.example.campussaathi

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campussaathi.databinding.ItemCategoryBinding

class CategoryAdapter(
    private var categoryList: List<CategoryModel>
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(val binding: ItemCategoryBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryList[position]
        holder.binding.tvCategoryName.text = category.name
        holder.binding.ivCategory.setImageResource(category.imageRes)
    }

    override fun getItemCount(): Int = categoryList.size

    fun updateList(newList: List<CategoryModel>) {
        categoryList = newList
        notifyDataSetChanged()
    }
}