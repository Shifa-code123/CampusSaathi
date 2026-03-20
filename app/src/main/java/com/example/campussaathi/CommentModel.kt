package com.example.campussaathi

data class CommentModel(
    var commentId: String = "",
    var userId: String = "",
    var userName: String = "",
    var userImage: String = "",
    var commentText: String = "",
    var timestamp: Long = 0L
)