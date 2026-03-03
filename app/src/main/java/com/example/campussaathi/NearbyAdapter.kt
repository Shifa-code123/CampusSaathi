package com.example.campussaathi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class NearbyAdapter(
    private val list: List<Pair<String, NearbyPlace>>, // docId + data
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<NearbyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.placeName)
        val distance: TextView = view.findViewById(R.id.placeDistance)
        val image: ImageView = view.findViewById(R.id.placeImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nearby_place, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (docId, place) = list[position]

        holder.name.text = place.name
        holder.distance.text = place.distance

//        if (place.photos.isNotEmpty()) {
//            Glide.with(holder.image.context)
//                .load(place.photos[0])
//                .into(holder.image)
//        }

        holder.itemView.setOnClickListener {
            onClick(docId)
        }
    }
}