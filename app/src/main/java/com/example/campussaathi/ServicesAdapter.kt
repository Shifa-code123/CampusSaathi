package com.example.campussaathi

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.LinearLayout
import android.view.View
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
        val serviceName: TextView = view.findViewById(R.id.txtServiceName)
        val pager: ViewPager2 = view.findViewById(R.id.photosPager)
        val followBtn: Button = view.findViewById(R.id.btnFollow)
        val btnViewDetails: Button = view.findViewById(R.id.btnViewDetails)
        val dotsLayout: LinearLayout = view.findViewById(R.id.dotsLayout)

        val ratingText: TextView = view.findViewById(R.id.txtRating)

        val commentText: TextView = view.findViewById(R.id.txtCommentCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_card, parent, false)

        return ServiceViewHolder(view)
    }

    override fun getItemCount(): Int = services.size

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {

        val service = services[position]
        val context = holder.itemView.context

        // RESET recycled views
        Glide.with(holder.itemView)
            .load(R.drawable.ic_profile)
            .circleCrop()
            .into(holder.profile)

        holder.serviceName.text = service.serviceName ?: "Service"
        holder.name.text = "Owner"

        // ---------------- OWNER NAME ----------------

        if (service.ownerId.isNotBlank()) {

            holder.name.text = "Loading..."

            db.collection("owner_verifications")
                .document(service.ownerId)
                .get()
                .addOnSuccessListener { doc ->
                    if (holder.adapterPosition == position) {
                        holder.name.text = doc.getString("fullName") ?: "Owner"
                    }
                }
                .addOnFailureListener {
                    holder.name.text = "Owner"
                }
        }

        // ---------------- OWNER PROFILE IMAGE ----------------

        if (service.ownerId.isNotBlank()) {

            val currentPosition = holder.adapterPosition

            db.collection("posts")
                .whereEqualTo("ownerId", service.ownerId)
                .get()
                .addOnSuccessListener { docs ->

                    if (holder.adapterPosition != currentPosition) return@addOnSuccessListener

                    if (!docs.isEmpty) {

                        var url = docs.documents[0].getString("business_pic")

                        if (url.isNullOrEmpty()) {
                            url = docs.documents[0].getString("img")
                        }

                        if (!url.isNullOrEmpty()) {

                            Glide.with(holder.itemView.context)
                                .load(url)
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.ic_profile)
                                .circleCrop()
                                .into(holder.profile)
                        }
                    }
                }
        }

        // ---------------- PHOTOS ----------------

        val photos = service.photos.filterIsInstance<String>()
        holder.pager.adapter = PhotosAdapter(photos)

        setupDots(holder, photos.size)

        holder.pager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                updateDots(holder, position)
            }
        })

        // Prevent parent ViewPager swipe
        val parentViewPager =
            (holder.itemView.context as androidx.fragment.app.FragmentActivity)
                .findViewById<ViewPager2>(R.id.viewPager)

        holder.pager.getChildAt(0).setOnTouchListener { _, event ->

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    parentViewPager.isUserInputEnabled = false
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    parentViewPager.isUserInputEnabled = true
                }
            }

            false
        }

        // ---------------- FOLLOW BUTTON ----------------

        holder.followBtn.text = "Follow"
        holder.followBtn.isEnabled = true

        holder.followBtn.setOnClickListener {
            holder.followBtn.text = "Following"
            holder.followBtn.isEnabled = false
        }

        // ---------------- DETAILS ----------------

        holder.btnViewDetails.setOnClickListener {

            val intent = Intent(context, DetailsActivity::class.java)

            intent.putStringArrayListExtra("PHOTOS", ArrayList(photos))
            intent.putExtra("OWNER_ID", service.ownerId)
            intent.putExtra("SERVICE_NAME", service.serviceName)
            intent.putExtra("LAT", service.latitude)
            intent.putExtra("LNG", service.longitude)
            intent.putExtra("PHONE", service.phone)
            intent.putExtra("DESCRIPTION", service.description)
            intent.putExtra("SERVICE_ID", service.serviceId)

            context.startActivity(intent)
        }

        // ---------------- OPEN OWNER PROFILE ----------------

        holder.name.setOnClickListener {

            val intent = Intent(
                holder.itemView.context,
                StudentOwnerProfileActivity::class.java
            )

            intent.putExtra("ownerId", service.ownerId)

            holder.itemView.context.startActivity(intent)
        }

        holder.profile.setOnClickListener {

            val intent = Intent(
                holder.itemView.context,
                StudentOwnerProfileActivity::class.java
            )

            intent.putExtra("ownerId", service.ownerId)

            holder.itemView.context.startActivity(intent)
        }

        //Rating
        db.collection("ratings")
            .whereEqualTo("ownerId", service.ownerId)
            .get()
            .addOnSuccessListener { docs ->

                var total = 0f
                var count = 0

                for (doc in docs) {

                    val rating = doc.getDouble("rating") ?: 0.0
                    total += rating.toFloat()
                    count++
                }

                if (count > 0) {

                    val avg = total / count

                    holder.ratingText.text = String.format("%.1f", avg)
                    holder.commentText.text = count.toString()

                } else {

                    holder.ratingText.text = "0.0"
                    holder.commentText.text = "0"
                }
            }
    }

    // ---------------- DOTS ----------------

    private fun setupDots(holder: ServiceViewHolder, count: Int) {

        holder.dotsLayout.removeAllViews()

        for (i in 0 until count) {

            val dot = View(holder.itemView.context)

            val params = LinearLayout.LayoutParams(16, 16)
            params.setMargins(6, 0, 6, 0)

            dot.layoutParams = params

            dot.setBackgroundResource(
                if (i == 0) R.drawable.dot_active
                else R.drawable.dot_inactive
            )

            holder.dotsLayout.addView(dot)
        }
    }

    private fun updateDots(holder: ServiceViewHolder, position: Int) {

        for (i in 0 until holder.dotsLayout.childCount) {

            val dot = holder.dotsLayout.getChildAt(i)

            dot.setBackgroundResource(
                if (i == position) R.drawable.dot_active
                else R.drawable.dot_inactive
            )
        }
    }

    override fun onViewRecycled(holder: ServiceViewHolder) {
        super.onViewRecycled(holder)
        holder.pager.adapter = null
    }
}