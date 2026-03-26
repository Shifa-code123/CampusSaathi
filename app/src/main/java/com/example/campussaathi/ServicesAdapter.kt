package com.example.campussaathi

import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import android.view.MotionEvent
import android.view.GestureDetector
import android.graphics.Color
import android.annotation.SuppressLint
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity

class ServicesAdapter(
    private val services: List<Service>
) : RecyclerView.Adapter<ServicesAdapter.ServiceViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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
        
        val btnLike: ImageView = view.findViewById(R.id.btnLike)
        val txtLikeCount: TextView = view.findViewById(R.id.txtLikeCount)
        val imgHeartAnim: ImageView = view.findViewById(R.id.imgHeartAnim)
        val photoContainer: View = view.findViewById(R.id.photoContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_service_card, parent, false)
        return ServiceViewHolder(view)
    }

    override fun getItemCount(): Int = services.size

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]
        val context = holder.itemView.context
        val userId = auth.currentUser?.uid ?: return

        // Use addSnapshotListener for real-time profile image and name updates
        if (service.ownerId.isNotBlank()) {
            db.collection("users").document(service.ownerId).addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists() && holder.adapterPosition == position) {
                    holder.name.text = doc.getString("fullName") ?: "Owner"
                    
                    val base64 = doc.getString("profileImageBase64")
                    if (!base64.isNullOrEmpty()) {
                        try {
                            val bytes = Base64.decode(base64, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            holder.profile.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            holder.profile.setImageResource(R.drawable.ic_profile)
                        }
                    } else {
                        holder.profile.setImageResource(R.drawable.ic_profile)
                    }
                }
            }
        }

        val likeRef = db.collection("likes").document(service.serviceId)
        val userLikeRef = likeRef.collection("userLikes").document(userId)

        userLikeRef.addSnapshotListener { doc, _ ->
            if (doc != null && doc.exists()) {
                holder.btnLike.setImageResource(R.drawable.ic_heart)
                holder.btnLike.setColorFilter(Color.RED)
            } else {
                holder.btnLike.setImageResource(R.drawable.ic_heart)
                holder.btnLike.setColorFilter(Color.DKGRAY)
            }
        }

        likeRef.collection("userLikes").addSnapshotListener { snapshot, _ ->
            holder.txtLikeCount.text = (snapshot?.size() ?: 0).toString()
        }

        fun toggleLike() {
            userLikeRef.get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    userLikeRef.delete()
                } else {
                    userLikeRef.set(hashMapOf("timestamp" to System.currentTimeMillis()))
                }
            }
        }

        holder.btnLike.setOnClickListener { toggleLike() }

        val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                showHeartAnimation(holder)
                userLikeRef.get().addOnSuccessListener { doc ->
                    if (!doc.exists()) toggleLike()
                }
                return true
            }

            override fun onDown(e: MotionEvent): Boolean = true
        })

        val child = holder.pager.getChildAt(0)
        child.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 🔥 Prevent parent ViewPager2 from intercepting horizontal swipes
                    v.parent.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // 🔥 Allow parent to intercept again after touch ends
                    v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            gestureDetector.onTouchEvent(event)
            false
        }

        val saveRef = db.collection("saved").document(userId).collection("services").document(service.serviceId)
        saveRef.addSnapshotListener { doc, _ ->
            if (doc != null && doc.exists()) holder.btnSave.setImageResource(R.drawable.ic_saved)
            else holder.btnSave.setImageResource(R.drawable.ic_save_outline)
        }

        holder.btnSave.setOnClickListener {
            saveRef.get().addOnSuccessListener { doc ->
                if (doc.exists()) saveRef.delete()
                else saveRef.set(hashMapOf("serviceId" to service.serviceId, "serviceName" to service.serviceName, "ownerId" to service.ownerId, "photos" to service.photos, "savedAt" to System.currentTimeMillis()))
            }
        }

        holder.serviceName.text = service.serviceName

        val navigateToProfile = View.OnClickListener {
            if (service.ownerId.isNotBlank()) {
                val activity = context as? AppCompatActivity
                val fragment = StudentBusinessProfileFragment.newInstance(service.ownerId)
                activity?.supportFragmentManager?.beginTransaction()?.replace(android.R.id.content, fragment)?.addToBackStack(null)?.commit()
            }
        }
        holder.profile.setOnClickListener(navigateToProfile)
        holder.name.setOnClickListener(navigateToProfile)

        val photos = service.photos.filterIsInstance<String>()
        holder.pager.adapter = PhotosAdapter(photos)
        setupDots(holder, photos.size)
        holder.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) { updateDots(holder, position) }
        })

        if (service.ownerId.isNotBlank()) {
            db.collection("followers").document(service.ownerId).collection("userFollowers").document(userId).addSnapshotListener { snapshot, _ ->
                if (holder.adapterPosition == position) {
                    if (snapshot != null && snapshot.exists()) { holder.followBtn.text = "Following"; holder.followBtn.alpha = 0.5f }
                    else { holder.followBtn.text = "Follow"; holder.followBtn.alpha = 1.0f }
                }
            }
        }

        holder.followBtn.setOnClickListener {
            if (service.ownerId.isNotBlank()) {
                val followRef = db.collection("followers").document(service.ownerId).collection("userFollowers").document(userId)
                followRef.get().addOnSuccessListener { doc -> if (doc.exists()) followRef.delete() else followRef.set(hashMapOf("followedAt" to System.currentTimeMillis())) }
            }
        }

        holder.commentIcon.setOnClickListener {
            val activity = holder.itemView.context as androidx.fragment.app.FragmentActivity
            CommentBottomSheet(service.serviceId).show(activity.supportFragmentManager, "comments")
        }

        holder.btnViewDetails.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailsActivity::class.java).apply {
                putStringArrayListExtra("PHOTOS", ArrayList(service.photos))
                putExtra("OWNER_ID", service.ownerId)
                putExtra("SERVICE_NAME", service.serviceName)
                putExtra("LAT", service.latitude)
                putExtra("LNG", service.longitude)
                putExtra("PHONE", service.phone)
                putExtra("DESCRIPTION", service.description)
                putExtra("SERVICE_ID", service.serviceId)
            }
            context.startActivity(intent)
        }

        db.collection("ratings").whereEqualTo("serviceId", service.serviceId).addSnapshotListener { docs, _ ->
            if (docs != null) {
                var total = 0f
                var count = 0
                for (doc in docs) { doc.getDouble("rating")?.let { total += it.toFloat(); count++ } }
                holder.ratingText.text = String.format("%.1f", if (count > 0) total / count else 0f)
            }
        }

        db.collection("comments").whereEqualTo("serviceId", service.serviceId).addSnapshotListener { value, _ ->
            if (value != null) holder.commentText.text = value.size().toString()
        }
    }

    private fun showHeartAnimation(holder: ServiceViewHolder) {
        holder.imgHeartAnim.visibility = View.VISIBLE
        holder.imgHeartAnim.alpha = 1f
        holder.imgHeartAnim.scaleX = 0f
        holder.imgHeartAnim.scaleY = 0f
        holder.imgHeartAnim.animate().scaleX(1.2f).scaleY(1.2f).setDuration(300).withEndAction {
            holder.imgHeartAnim.animate().scaleX(1f).scaleY(1f).alpha(0f).setDuration(300).withEndAction {
                holder.imgHeartAnim.visibility = View.GONE
            }.start()
        }.start()
    }

    private fun setupDots(holder: ServiceViewHolder, count: Int) {
        holder.dotsLayout.removeAllViews()
        for (i in 0 until count) {
            val dot = View(holder.itemView.context)
            dot.layoutParams = LinearLayout.LayoutParams(16, 16).apply { setMargins(6, 0, 6, 0) }
            dot.setBackgroundResource(if (i == 0) R.drawable.dot_active else R.drawable.dot_inactive)
            holder.dotsLayout.addView(dot)
        }
    }

    private fun updateDots(holder: ServiceViewHolder, position: Int) {
        for (i in 0 until holder.dotsLayout.childCount) {
            holder.dotsLayout.getChildAt(i).setBackgroundResource(if (i == position) R.drawable.dot_active else R.drawable.dot_inactive)
        }
    }

    override fun onViewRecycled(holder: ServiceViewHolder) {
        super.onViewRecycled(holder)
        holder.pager.adapter = null
    }
}
