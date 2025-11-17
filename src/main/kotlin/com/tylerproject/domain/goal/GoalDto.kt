package com.tylerproject.domain.goal

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.*

data class CreateGoalRequest
@JsonCreator
constructor(
    @JsonProperty("title")
    @field:NotBlank(message = "Título é obrigatório")
    @field:Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
    val title: String,

    @JsonProperty("description")
    @field:NotBlank(message = "Descrição é obrigatória")
    val description: String,

    @JsonProperty("targetAmount")
    @field:NotNull(message = "Valor alvo é obrigatório")
    @field:DecimalMin(value = "0.01", message = "Valor alvo deve ser maior que zero")
    val targetAmount: Double,

    @JsonProperty("currentAmount")
    @field:DecimalMin(value = "0.00", message = "Valor arrecadado não pode ser negativo")
    val currentAmount: Double = 0.0,

    @JsonProperty("startDate")
    val startDate: String? = null,

    @JsonProperty("endDate")
    val endDate: String? = null,

    @JsonProperty("status")
    @field:NotNull(message = "Status é obrigatório")
    val status: GoalStatus = GoalStatus.ACTIVE,

    @JsonProperty("imageUrl")
    val imageUrl: String? = null,

    @JsonProperty("active")
    val active: Boolean = true
)

data class UpdateGoalRequest
@JsonCreator
constructor(
    @JsonProperty("title")
    @field:Size(max = 200, message = "Título deve ter no máximo 200 caracteres")
    val title: String? = null,

    @JsonProperty("description")
    val description: String? = null,

    @JsonProperty("targetAmount")
    @field:DecimalMin(value = "0.01", message = "Valor alvo deve ser maior que zero")
    val targetAmount: Double? = null,

    @JsonProperty("currentAmount")
    @field:DecimalMin(value = "0.00", message = "Valor arrecadado não pode ser negativo")
    val currentAmount: Double? = null,

    @JsonProperty("startDate")
    val startDate: String? = null,

    @JsonProperty("endDate")
    val endDate: String? = null,

    @JsonProperty("status")
    val status: GoalStatus? = null,

    @JsonProperty("imageUrl")
    val imageUrl: String? = null,

    @JsonProperty("active")
    val active: Boolean? = null
)

data class GoalResponse(
    val id: String,
    val title: String,
    val description: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val progress: Int,
    val startDate: String,
    val endDate: String? = null,
    val status: GoalStatus,
    val imageUrl: String? = null,
    val active: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val createdBy: String? = null
) {
    companion object {
        fun fromEntity(goal: Goal): GoalResponse {
            return GoalResponse(
                id = goal.id,
                title = goal.title,
                description = goal.description,
                targetAmount = goal.targetAmount,
                currentAmount = goal.currentAmount,
                progress = goal.progress,
                startDate = goal.startDate,
                endDate = goal.endDate,
                status = goal.status,
                imageUrl = goal.imageUrl,
                active = goal.active,
                createdAt = goal.createdAt,
                updatedAt = goal.updatedAt,
                createdBy = goal.createdBy
            )
        }
    }
}

data class GoalPageResponse(
    val goals: List<GoalResponse>,
    val page: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

data class ImageUploadResponse(
    val imageUrl: String
)

data class AddAmountRequest
@JsonCreator
constructor(
    @JsonProperty("amount")
    @field:NotNull(message = "Valor é obrigatório")
    @field:DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    val amount: Double
)
