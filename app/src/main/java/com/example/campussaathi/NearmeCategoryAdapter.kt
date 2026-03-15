package com.example.campussaathi

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.campussaathi.databinding.ItemNearmeCategoryBinding

class NearmeCategoryAdapter(
    private val categoryList: List<String>
) : RecyclerView.Adapter<NearmeCategoryAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemNearmeCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = ItemNearmeCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val category = categoryList[position]

        holder.binding.categoryName.text = category

        // ICON SETUP
        when (category) {

            "Hospitals" ->
                holder.binding.categoryIcon.setImageResource(R.drawable.ic_ambulance)

            "Police" ->
                holder.binding.categoryIcon.setImageResource(R.drawable.ic_police)

            "Medical" ->
                holder.binding.categoryIcon.setImageResource(R.drawable.ic_medical)

            "Fire Station" ->
                holder.binding.categoryIcon.setImageResource(R.drawable.ic_fire)

            "Bus Station" ->
                holder.binding.categoryIcon.setImageResource(R.drawable.ic_bus)
            "Railway Station" ->
                holder.binding.categoryIcon.setImageResource(R.drawable.ic_train)
        }

        holder.itemView.setOnClickListener {

            val intent = Intent(
                holder.itemView.context,
                CategoryListActivity::class.java
            )

            intent.putExtra("category", category)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = categoryList.size
}