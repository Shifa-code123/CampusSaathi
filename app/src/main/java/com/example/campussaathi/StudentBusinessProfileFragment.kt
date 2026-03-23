package com.example.campussaathi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StudentBusinessProfileFragment : Fragment() {

    private lateinit var profileImage: ImageView
    private lateinit var txtPostsCount: TextView
    private lateinit var txtFollowersCount: TextView
    private lateinit var txtOwnerName: TextView
    private lateinit var txtBio: TextView
    private lateinit var btnFollow: Button
    private lateinit var btnBack: ImageView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var ownerId: String? = null
    private var isFollowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ownerId = arguments?.getString("ownerId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_student_business_profile, container, false)
        initViews(view)
        setupTabs()
        loadProfileData()
        checkFollowStatus()

        btnFollow.setOnClickListener {
            toggleFollow()
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }

    private fun initViews(view: View) {
        profileImage = view.findViewById(R.id.profileImage)
        txtPostsCount = view.findViewById(R.id.txtPostsCount)
        txtFollowersCount = view.findViewById(R.id.txtFollowersCount)
        txtOwnerName = view.findViewById(R.id.txtOwnerName)
        txtBio = view.findViewById(R.id.txtBio)
        btnFollow = view.findViewById(R.id.btnFollow)
        btnBack = view.findViewById(R.id.btnBack)
        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)
    }

    private fun setupTabs() {
        val uid = ownerId ?: return
        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> PostsFragment.newInstance(uid)
                    1 -> ServicesFragment.newInstance(uid)
                    else -> PostsFragment.newInstance(uid)
                }
            }
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.setIcon(R.drawable.ic_grid)
                1 -> tab.setIcon(R.drawable.ic_services)
            }
        }.attach()
    }

    private fun loadProfileData() {
        val uid = ownerId ?: return
        
        db.collection("owner_verifications").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists() && isAdded) {
                    txtOwnerName.text = doc.getString("fullName") ?: "Owner"
                    txtBio.text = doc.getString("businessDescription") ?: "No bio available"
                }
            }

        db.collection("posts").whereEqualTo("ownerId", uid).limit(1).get()
            .addOnSuccessListener { docs ->
                if (!docs.isEmpty && isAdded) {
                    val url = docs.documents[0].getString("business_pic") ?: docs.documents[0].getString("img")
                    if (!url.isNullOrEmpty()) {
                        Glide.with(this).load(url).placeholder(R.drawable.ic_profile).circleCrop().into(profileImage)
                    }
                }
            }

        db.collection("posts").whereEqualTo("ownerId", uid).get()
            .addOnSuccessListener { if (isAdded) txtPostsCount.text = it.size().toString() }

        db.collection("followers").document(uid).collection("userFollowers").get()
            .addOnSuccessListener { if (isAdded) txtFollowersCount.text = it.size().toString() }
    }

    private fun checkFollowStatus() {
        val currentUserId = auth.currentUser?.uid ?: return
        val targetOwnerId = ownerId ?: return

        db.collection("followers").document(targetOwnerId)
            .collection("userFollowers").document(currentUserId)
            .addSnapshotListener { snapshot, _ ->
                if (isAdded) {
                    isFollowing = snapshot != null && snapshot.exists()
                    updateFollowButtonUI()
                }
            }
    }

    private fun updateFollowButtonUI() {
        if (!isAdded) return
        if (isFollowing) {
            btnFollow.text = "Following"
            btnFollow.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
            btnFollow.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        } else {
            btnFollow.text = "Follow"
            btnFollow.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.follow_button_blue))
            btnFollow.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        }
    }

    private fun toggleFollow() {
        val currentUserId = auth.currentUser?.uid ?: return
        val targetOwnerId = ownerId ?: return

        val followRef = db.collection("followers").document(targetOwnerId)
            .collection("userFollowers").document(currentUserId)

        if (isFollowing) {
            followRef.delete()
        } else {
            val data = hashMapOf("followedAt" to System.currentTimeMillis())
            followRef.set(data)
        }
    }

    companion object {
        fun newInstance(ownerId: String): StudentBusinessProfileFragment {
            val fragment = StudentBusinessProfileFragment()
            val args = Bundle()
            args.putString("ownerId", ownerId)
            fragment.arguments = args
            return fragment
        }
    }
}