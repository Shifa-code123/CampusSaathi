package com.example.campussaathi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campussaathi.databinding.FragmentReviewBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class ReviewFragment : Fragment() {

    private var _binding: FragmentReviewBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: FirebaseFirestore
    private lateinit var reviewAdapter: ReviewAdapter
    private val reviewList = mutableListOf<ReviewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        setupRecyclerView()
        setupRefreshLayout()
        fetchData()
    }

    private fun setupRecyclerView() {
        reviewAdapter = ReviewAdapter(reviewList)
        binding.rvReviews.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reviewAdapter
        }
    }

    private fun setupRefreshLayout() {
        binding.swipeRefresh.setOnRefreshListener {
            fetchData()
        }
    }

    private fun fetchData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        binding.loader.visibility = View.VISIBLE

        db.collection("services")
            .whereEqualTo("ownerId", uid)
            .get()
            .addOnSuccessListener { serviceDocs ->
                val serviceIds = serviceDocs.map { it.id }
                if (serviceIds.isEmpty()) {
                    onDataLoaded(emptyList(), 0, 0f, emptyMap())
                    return@addOnSuccessListener
                }
                
                fetchReviewsForServices(serviceIds)
            }
            .addOnFailureListener { e ->
                binding.loader.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchReviewsForServices(serviceIds: List<String>) {
        val targetIds = serviceIds.take(10)

        val ratingsTask = db.collection("ratings")
            .whereIn("serviceId", targetIds)
            .get()

        val commentsTask = db.collection("comments")
            .whereIn("serviceId", targetIds)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()

        Tasks.whenAllSuccess<QuerySnapshot>(ratingsTask, commentsTask)
            .addOnSuccessListener { results ->
                val ratingDocs = results[0]
                val commentDocs = results[1]

                val ratingLookup = mutableMapOf<String, Int>() // "serviceId_studentId" -> rating
                var totalSum = 0f
                var totalCount = 0
                val starCounts = mutableMapOf(1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0)

                for (doc in ratingDocs) {
                    val r = doc.getLong("rating")?.toInt() ?: 0
                    val sId = doc.getString("serviceId") ?: ""
                    val uId = doc.getString("studentId") ?: ""
                    if (r in 1..5) {
                        ratingLookup["${sId}_${uId}"] = r
                        totalSum += r
                        totalCount++
                        starCounts[r] = (starCounts[r] ?: 0) + 1
                    }
                }

                val tempReviewList = mutableListOf<ReviewModel>()
                val userIdsToFetch = mutableSetOf<String>()

                for (doc in commentDocs) {
                    val uId = doc.getString("userId") ?: ""
                    val sId = doc.getString("serviceId") ?: ""
                    val commentText = doc.getString("commentText") ?: ""
                    val userName = doc.getString("userName") ?: "Student"
                    val timestamp = doc.getLong("timestamp") ?: 0L
                    
                    val rating = ratingLookup["${sId}_${uId}"] ?: 0
                    
                    tempReviewList.add(ReviewModel(
                        userName = userName,
                        comment = commentText,
                        rating = rating,
                        userId = uId,
                        timestamp = timestamp
                    ))
                    if (uId.isNotEmpty()) userIdsToFetch.add(uId)
                }

                fetchUserProfilesAndFinish(tempReviewList, userIdsToFetch.toList(), totalCount, totalSum, starCounts)
            }
            .addOnFailureListener { e ->
                binding.loader.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(requireContext(), "Error fetching reviews: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchUserProfilesAndFinish(
        reviews: List<ReviewModel>,
        userIds: List<String>,
        totalCount: Int,
        totalSum: Float,
        starCounts: Map<Int, Int>
    ) {
        if (userIds.isEmpty()) {
            onDataLoaded(reviews, totalCount, totalSum, starCounts)
            return
        }

        val userTasks = userIds.chunked(10).map { batch ->
            db.collection("users").whereIn(FieldPath.documentId(), batch).get()
        }

        Tasks.whenAllSuccess<QuerySnapshot>(userTasks)
            .addOnSuccessListener { results ->
                val profileMap = mutableMapOf<String, String>()
                for (snapshot in results) {
                    for (doc in snapshot) {
                        profileMap[doc.id] = doc.getString("profileImageBase64") ?: ""
                    }
                }

                val finalReviews = reviews.map { 
                    it.copy(profileImageBase64 = profileMap[it.userId] ?: "")
                }
                onDataLoaded(finalReviews, totalCount, totalSum, starCounts)
            }
            .addOnFailureListener {
                onDataLoaded(reviews, totalCount, totalSum, starCounts)
            }
    }

    private fun onDataLoaded(reviews: List<ReviewModel>, totalCount: Int, totalSum: Float, starCounts: Map<Int, Int>) {
        if (context == null) return
        binding.loader.visibility = View.GONE
        binding.swipeRefresh.isRefreshing = false
        reviewList.clear()
        reviewList.addAll(reviews)
        reviewAdapter.updateData(reviewList)
        updateSummary(totalCount, totalSum, starCounts)
    }

    private fun updateSummary(totalCount: Int, totalSum: Float, starCounts: Map<Int, Int>) {
        if (totalCount == 0) {
            binding.tvAverageRating.text = "0.0"
            binding.rbAverage.rating = 0f
            binding.tvTotalReviews.text = "0 Reviews"
            binding.pb5Star.progress = 0
            binding.pb4Star.progress = 0
            binding.pb3Star.progress = 0
            binding.pb2Star.progress = 0
            binding.pb1Star.progress = 0
            return
        }

        val avg = totalSum / totalCount
        binding.tvAverageRating.text = String.format("%.1f", avg)
        binding.rbAverage.rating = avg
        binding.tvTotalReviews.text = "$totalCount Reviews"

        fun getPercent(count: Int): Int = ((count.toFloat() / totalCount) * 100).toInt()

        binding.pb5Star.progress = getPercent(starCounts[5] ?: 0)
        binding.pb4Star.progress = getPercent(starCounts[4] ?: 0)
        binding.pb3Star.progress = getPercent(starCounts[3] ?: 0)
        binding.pb2Star.progress = getPercent(starCounts[2] ?: 0)
        binding.pb1Star.progress = getPercent(starCounts[1] ?: 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
