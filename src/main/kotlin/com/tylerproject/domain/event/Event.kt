package com.tylerproject.domain.event


import kotlinx.serialization.Serializable


@Serializable
data class EventImage(
    val id: String = "",
    val url: String = "",
    val filename: String = "",
    val contentType: String = "",
    val size: Long = 0L,
    val isPrimary: Boolean = false,
    val uploadedAt: String = ""
)

@Serializable
data class Event (
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val images: List<EventImage> = emptyList(),
    val category: String? = null,
    val tags: List<String>? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
    val createdBy: String? = null,
    val date: String = "",
    val local: String = ""
){
    val primaryImageUrl: String?
        get() = images.find { it.isPrimary }?.url ?: images.firstOrNull()?.url
}