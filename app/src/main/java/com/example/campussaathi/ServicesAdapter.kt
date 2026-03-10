package com.example.campussaathi

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class ServicesAdapter(
    private val services: List<Service>
) : RecyclerView.Adapter<ServicesAdapter.ServiceViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    class ServiceViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val profile: ImageView = view.findViewById(R.id.imgProfile)
        val name: TextView = view.findViewById(R.id.txtOwnerName)
        val pager: ViewPager2 = view.findViewById(R.id.photosPager)
        val counter: TextView = view.findViewById(R.id.txtPhotoCounter)
        val followBtn: Button = view.findViewById(R.id.btnFollow)
        val btnViewDetails: Button = view.findViewById(R.id.btnViewDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_card, parent, false)

        return ServiceViewHolder(view)
    }

    override fun getItemCount(): Int {
        return services.size
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {

        val service = services[position]

        // OWNER NAME
        db.collection("owner_verifications")
            .document(service.ownerId)
            .get()
            .addOnSuccessListener { doc ->
                holder.name.text = doc.getString("fullName") ?: "Owner"
            }

        // PROFILE PIC
        db.collection("posts")
            .whereEqualTo("ownerId", service.ownerId)
            .get()
            .addOnSuccessListener { docs ->

                for (doc in docs) {

                    val url = doc.getString("business_pic")

                    if (!url.isNullOrEmpty()) {

                        Glide.with(holder.itemView.context)
                            .load(url)
                            .circleCrop()
                            .into(holder.profile)

                        break
                    }
                }
            }

        // VIEWPAGER IMAGES
        holder.pager.adapter = PhotosAdapter(service.photos)

        val total = service.photos.size
        holder.counter.text = "1/$total"

        holder.pager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                holder.counter.text = "${position + 1}/$total"
            }
        })

        // FOLLOW BUTTON
        holder.followBtn.setOnClickListener {

            holder.followBtn.text = "Following"
            holder.followBtn.isEnabled = false
        }

        // VIEW DETAILS BUTTON
        holder.btnViewDetails.setOnClickListener {

            val context = holder.itemView.context

            val intent = Intent(context, DetailsActivity::class.java)

            intent.putStringArrayListExtra("PHOTOS", ArrayList(service.photos))
            intent.putExtra("OWNER_ID", service.ownerId)
            intent.putExtra("SERVICE_NAME", service.serviceName)

            context.startActivity(intent)
        }

        //passing details
        holder.btnViewDetails.setOnClickListener {

            val context = holder.itemView.context

            val intent = Intent(context, DetailsActivity::class.java)

            intent.putStringArrayListExtra("PHOTOS", ArrayList(service.photos))
            intent.putExtra("OWNER_ID", service.ownerId)
            intent.putExtra("SERVICE_NAME", service.serviceName)

            intent.putExtra("LAT", service.latitude)
            intent.putExtra("LNG", service.longitude)
            intent.putExtra("PHONE", service.phone)
            intent.putExtra("DESCRIPTION", service.description)

            context.startActivity(intent)
        }
    }
}