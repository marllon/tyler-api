package com.tylerproject.domain.event

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.Date

@Serializable
data class Event (
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val category: String? = null,
    val tags: List<String>? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
    val createdBy: String? = null,
    val date: String = "",
    val local: String = ""
    )