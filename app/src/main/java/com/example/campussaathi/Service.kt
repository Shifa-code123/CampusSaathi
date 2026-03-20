package com.example.campussaathi

data class Service(
    var serviceId: String = "",
    var ownerId: String = "",
    var photos: List<String> = emptyList(),
    var serviceName: String = "",   // ✅ change yaha
    var latitude: Double? = null,
    var longitude: Double? = null,
    var phone: String = "",
    var description: String = "",
    var category: String = ""
)