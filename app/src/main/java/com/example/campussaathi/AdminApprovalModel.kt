package com.example.campussaathi
data class AdminApprovalModel(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val status: String = "pending",
    val type: String = "", // "owner" or "service"
    val data: Map<String, Any?> = emptyMap()
)