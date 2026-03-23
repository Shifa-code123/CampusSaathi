package com.example.campussaathi

data class AdminVolunteerModel(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val type: String = "", // "volunteer" or "cityhelp"
    val status: String = "pending",
    val data: Map<String, Any?> = emptyMap()
)