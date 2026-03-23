package com.example.campussaathi

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campussaathi.adapter.CommentAdapter
import com.example.campussaathi.utils.ProfileImageLoader
import com.google.android.material.bottomsheet.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CommentBottomSheet(private val serviceId: String) : BottomSheetDialogFragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var editComment: EditText
    private lateinit var btnSend: ImageView
    private lateinit var txtNoComments: TextView
    private lateinit var profileImage: ImageView

    private lateinit var adapter: CommentAdapter
    private val commentList = ArrayList<CommentModel>()

    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onStart() {
        super.onStart()

        val dialog = dialog as BottomSheetDialog
        val bottomSheet =
            dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.bottom_comments, container, false)

        recycler = view.findViewById(R.id.rvComments)
        editComment = view.findViewById(R.id.etComment)
        txtNoComments = view.findViewById(R.id.tvNoComments)
        btnSend = view.findViewById(R.id.btnSend)
        profileImage = view.findViewById(R.id.profileimage)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        adapter = CommentAdapter(commentList) { comment ->
            showCommentOptions(comment)
        }

        recycler.adapter = adapter

        // 🔥 INPUT BOX PROFILE IMAGE
        ProfileImageLoader.loadProfile(profileImage)

        loadComments()

        // 🔥 SEND COMMENT
        btnSend.setOnClickListener {

            val text = editComment.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            val uid = currentUserId
            if (uid.isEmpty()) return@setOnClickListener

            // 🔥 FETCH USER DATA
            db.collection("users").document(uid).get()
                .addOnSuccessListener { userDoc ->

                    val name = userDoc.getString("fullName") ?: "Student"
                    val image = userDoc.getString("profileImageBase64") ?: ""

                    val comment = hashMapOf(
                        "serviceId" to serviceId,
                        "userId" to uid,
                        "userName" to name,
                        "userImage" to image,
                        "commentText" to text,
                        "timestamp" to System.currentTimeMillis()
                    )

                    db.collection("comments")
                        .add(comment)
                        .addOnSuccessListener {
                            editComment.setText("")
                        }
                }
        }

        return view
    }

    // 🔥 LOAD COMMENTS REAL-TIME
    private fun loadComments() {

        db.collection("comments")
            .whereEqualTo("serviceId", serviceId)
            .addSnapshotListener { value, _ ->

                commentList.clear()

                if (value != null) {

                    if (value.isEmpty) {

                        txtNoComments.visibility = View.VISIBLE
                        recycler.visibility = View.GONE

                    } else {

                        txtNoComments.visibility = View.GONE
                        recycler.visibility = View.VISIBLE

                        for (doc in value.documents) {

                            val comment = CommentModel(
                                commentId = doc.id,
                                userId = doc.getString("userId") ?: "",
                                userName = doc.getString("userName") ?: "",
                                userImage = doc.getString("userImage") ?: "",
                                commentText = doc.getString("commentText") ?: "",
                                timestamp = doc.getLong("timestamp") ?: 0L
                            )

                            commentList.add(comment)
                        }

                        commentList.sortBy { it.timestamp }

                        adapter.notifyDataSetChanged()
                        recycler.scrollToPosition(commentList.size - 1)
                    }
                }
            }
    }

    // 🔥 LONG PRESS OPTIONS
    private fun showCommentOptions(comment: CommentModel) {

        if (comment.userId == currentUserId) {

            val options = arrayOf("Report", "Delete")

            AlertDialog.Builder(requireContext())
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> reportComment(comment)
                        1 -> deleteComment(comment)
                    }
                }
                .show()

        } else {

            val options = arrayOf("Report")

            AlertDialog.Builder(requireContext())
                .setItems(options) { _, which ->
                    if (which == 0) reportComment(comment)
                }
                .show()
        }
    }

    private fun reportComment(comment: CommentModel) {

        val report = hashMapOf(
            "commentId" to comment.commentId,
            "text" to comment.commentText,
            "ownerId" to comment.userId,
            "reportedBy" to currentUserId,
            "time" to System.currentTimeMillis()
        )

        db.collection("comment_reports")
            .add(report)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Comment reported", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteComment(comment: CommentModel) {

        db.collection("comments")
            .document(comment.commentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Comment deleted", Toast.LENGTH_SHORT).show()
            }
    }
}