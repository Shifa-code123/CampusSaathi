package com.example.campussaathi

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.example.campussaathi.CommentBottomSheet
import android.view.MotionEvent
import android.view.animation.AnimationUtils

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
        val commentIcon: ImageView = view.findViewById(R.id.iconComment)

        val btnSave: ImageView = view.findViewById(R.id.btnSave)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_card, parent, false)

        return ServiceViewHolder(view)
    }

    override fun getItemCount(): Int = services.size

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {

        val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.item_anim)
        holder.itemView.startAnimation(animation)

        val service = services[position]
        val context = holder.itemView.context

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val saveRef = db.collection("saved")
            .document(userId)
            .collection("services")
            .document(service.serviceId)

        // Save icon sync
        saveRef.addSnapshotListener { doc, _ ->
            if (doc != null && doc.exists()) {
                holder.btnSave.setImageResource(R.drawable.ic_saved)
            } else {
                holder.btnSave.setImageResource(R.drawable.ic_save_outline)
            }
        }

        holder.btnSave.setOnClickListener {
            saveRef.get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    saveRef.delete()
                } else {
                    val data = hashMapOf(
                        "serviceId" to service.serviceId,
                        "serviceName" to service.serviceName,
                        "ownerId" to service.ownerId,
                        "photos" to service.photos,
                        "savedAt" to System.currentTimeMillis()
                    )
                    saveRef.set(data)
                }
            }
        }

        // ---------------- UI ----------------

        Glide.with(holder.itemView)
            .load(R.drawable.ic_profile)
            .circleCrop()
            .into(holder.profile)

        holder.serviceName.text = service.serviceName
        holder.name.text = "Owner"

        // Owner name
        if (service.ownerId.isNotBlank()) {
            db.collection("owner_verifications")
                .document(service.ownerId)
                .get()
                .addOnSuccessListener { doc ->
                    if (holder.adapterPosition == position) {
                        holder.name.text = doc.getString("fullName") ?: "Owner"
                    }
                }
        }

        // Owner image
        if (service.ownerId.isNotBlank()) {
            db.collection("posts")
                .whereEqualTo("ownerId", service.ownerId)
                .limit(1)
                .get()
                .addOnSuccessListener { docs ->

                    if (!docs.isEmpty) {

                        var url = docs.documents[0].getString("business_pic")

                        if (url.isNullOrEmpty()) {
                            url = docs.documents[0].getString("img")
                        }

                        if (!url.isNullOrEmpty()) {
                            Glide.with(holder.itemView.context)
                                .load(url)
                                .placeholder(R.drawable.ic_profile)
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

        // 🔥🔥🔥 MAIN FIX HERE 🔥🔥🔥
        holder.pager.isUserInputEnabled = true
        holder.pager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        var startX = 0f
        var startY = 0f

        holder.pager.getChildAt(0).setOnTouchListener { v, event ->

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    v.parent.requestDisallowInterceptTouchEvent(true)
                }

                MotionEvent.ACTION_MOVE -> {

                    val dx = Math.abs(event.x - startX)
                    val dy = Math.abs(event.y - startY)

                    if (dy > dx) {
                        // 🔥 vertical swipe → parent handle karega
                        v.parent.requestDisallowInterceptTouchEvent(false)
                    } else {
                        // horizontal swipe → pager handle karega
                        v.parent.requestDisallowInterceptTouchEvent(true)
                    }
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }

            false
        }

        // ---------------- FOLLOW ----------------

        holder.followBtn.setOnClickListener {
            holder.followBtn.text = "Following"
            holder.followBtn.isEnabled = false
        }

        // ---------------- COMMENT ----------------

        holder.commentIcon.setOnClickListener {
            val activity = holder.itemView.context as androidx.fragment.app.FragmentActivity
            val sheet = CommentBottomSheet(service.serviceId)
            sheet.show(activity.supportFragmentManager, "comments")
        }

        // ---------------- DETAILS ----------------

        holder.btnViewDetails.setOnClickListener {

            val intent = Intent(holder.itemView.context, DetailsActivity::class.java)

            intent.putStringArrayListExtra("PHOTOS", ArrayList(service.photos))
            intent.putExtra("OWNER_ID", service.ownerId)
            intent.putExtra("SERVICE_NAME", service.serviceName)
            intent.putExtra("LAT", service.latitude)
            intent.putExtra("LNG", service.longitude)
            intent.putExtra("PHONE", service.phone)
            intent.putExtra("DESCRIPTION", service.description)
            intent.putExtra("SERVICE_ID", service.serviceId)

            val ctx = holder.itemView.context
            ctx.startActivity(intent)

            if (ctx is android.app.Activity) {
                ctx.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }

        // ---------------- ⭐ RATING ----------------

        db.collection("ratings")
            .whereEqualTo("serviceId", service.serviceId)
            .addSnapshotListener { docs, _ ->

                if (docs != null) {

                    var total = 0f
                    var count = 0

                    for (doc in docs) {

                        val rating = doc.getDouble("rating")

                        if (rating != null) {
                            total += rating.toFloat()
                            count++
                        }
                    }

                    val avg = if (count > 0) total / count else 0f

                    holder.ratingText.text = String.format("%.1f", avg)
                }
            }

        // ---------------- COMMENTS COUNT ----------------

        db.collection("comments")
            .whereEqualTo("serviceId", service.serviceId)
            .addSnapshotListener { value, _ ->
                if (value != null) {
                    holder.commentText.text = value.size().toString()
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