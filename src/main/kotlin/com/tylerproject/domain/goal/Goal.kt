package com.tylerproject.domain.goal

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.math.BigDecimal
import java.math.RoundingMode

@Serializable
data class Goal(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val targetAmount: Double = 0.0,
    val currentAmount: Double = 0.0,
    val startDate: String = "",
    val endDate: String? = null,
    val status: GoalStatus = GoalStatus.ACTIVE,
    val imageUrl: String? = null,
    val active: Boolean = true,
    val createdAt: String = "",
    val updatedAt: String = "",
    val createdBy: String? = null
) {
    @Transient
    val progress: Int
        get() {
            if (targetAmount <= 0.0) return 0
            val percentage = (currentAmount / targetAmount) * 100.0
            return percentage.toInt().coerceIn(0, 100)
        }
}
