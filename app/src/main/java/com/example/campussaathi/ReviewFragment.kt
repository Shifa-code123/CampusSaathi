package com.example.campussaathi

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campussaathi.databinding.FragmentReviewBinding
import com.google.firebase.firestore.FirebaseFirestore

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
        fetchReviews()
    }

    private fun setupRecyclerView() {
        reviewAdapter = ReviewAdapter(reviewList)
        binding.rvReviews.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reviewAdapter
        }
    }

    private fun fetchReviews() {
        binding.loader.visibility = View.VISIBLE

        db.collection("ratings")
            .get() // ❌ orderBy हटाया (timestamp nahi tha)
            .addOnSuccessListener { documents ->
                binding.loader.visibility = View.GONE
                reviewList.clear()

                Log.d("REVIEW_DEBUG", "Docs size: ${documents.size()}")

                var totalRatingSum = 0f
                val countMap = mutableMapOf(1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0)

                for (document in documents) {

                    // ✅ DIRECT FIELD FETCH (NO toObject bug)
                    val rating = document.getLong("rating")?.toInt() ?: 0

                    Log.d("REVIEW_DEBUG", "Rating: $rating")

                    val review = ReviewModel(
                        rating = rating,
                        studentName = "Student",
                        reviewText = "No review available",
                        timestamp = 0L
                    )

                    reviewList.add(review)

                    if (rating in 1..5) {
                        countMap[rating] = (countMap[rating] ?: 0) + 1
                        totalRatingSum += rating
                    }
                }

                updateUI(reviewList.size, totalRatingSum, countMap)
                reviewAdapter.updateData(reviewList)
            }
            .addOnFailureListener { e ->
                binding.loader.visibility = View.GONE
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUI(totalReviews: Int, totalRatingSum: Float, countMap: Map<Int, Int>) {

        if (totalReviews == 0) {
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

        val average = totalRatingSum / totalReviews

        binding.tvAverageRating.text = String.format("%.1f", average)
        binding.rbAverage.rating = average
        binding.tvTotalReviews.text = "$totalReviews Reviews"

        fun getPercent(count: Int): Int {
            return ((count.toFloat() / totalReviews) * 100).toInt()
        }

        binding.pb5Star.progress = getPercent(countMap[5] ?: 0)
        binding.pb4Star.progress = getPercent(countMap[4] ?: 0)
        binding.pb3Star.progress = getPercent(countMap[3] ?: 0)
        binding.pb2Star.progress = getPercent(countMap[2] ?: 0)
        binding.pb1Star.progress = getPercent(countMap[1] ?: 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}