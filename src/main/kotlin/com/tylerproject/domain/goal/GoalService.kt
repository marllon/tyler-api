package com.tylerproject.domain.goal

import com.tylerproject.service.ImageUploadService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.ceil

interface GoalService {
    fun listGoals(
        page: Int,
        pageSize: Int,
        status: GoalStatus?,
        activeOnly: Boolean,
        sortBy: String,
        sortDirection: String
    ): GoalPageResponse

    fun getById(id: String): GoalResponse?
    fun create(request: CreateGoalRequest, createdByUid: String?): GoalResponse
    fun update(id: String, request: UpdateGoalRequest): GoalResponse?
    fun addAmount(id: String, amount: Double): GoalResponse?
    fun delete(id: String): Boolean
    fun uploadImage(id: String, file: MultipartFile): String?
    fun deleteImage(id: String): Boolean
}

@Service
class GoalServiceImpl(
    private val goalRepository: GoalRepository,
    private val imageUploadService: ImageUploadService
) : GoalService {

    private val logger = LoggerFactory.getLogger(GoalServiceImpl::class.java)

    override fun listGoals(
        page: Int,
        pageSize: Int,
        status: GoalStatus?,
        activeOnly: Boolean,
        sortBy: String,
        sortDirection: String
    ): GoalPageResponse {
        val (goals, totalElements) = goalRepository.findAll(
            page = page,
            pageSize = pageSize,
            status = status,
            activeOnly = activeOnly,
            sortBy = sortBy,
            sortDirection = sortDirection
        )

        val totalPages = ceil(totalElements.toDouble() / pageSize).toInt()

        return GoalPageResponse(
            goals = goals.map { GoalResponse.fromEntity(it) },
            page = page,
            pageSize = pageSize,
            totalElements = totalElements,
            totalPages = totalPages,
            hasNext = page < totalPages - 1,
            hasPrevious = page > 0
        )
    }

    override fun getById(id: String): GoalResponse? {
        val goal = goalRepository.findById(id)
        return goal?.let { GoalResponse.fromEntity(it) }
    }

    override fun create(request: CreateGoalRequest, createdByUid: String?): GoalResponse {
        val now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)

        val goal = Goal(
            id = "",
            title = request.title,
            description = request.description,
            targetAmount = request.targetAmount,
            currentAmount = request.currentAmount,
            startDate = request.startDate ?: now,
            endDate = request.endDate,
            status = request.status,
            imageUrl = request.imageUrl,
            active = request.active,
            createdAt = now,
            updatedAt = now,
            createdBy = createdByUid
        )

        val savedGoal = goalRepository.save(goal)
        logger.info("Goal created: ${savedGoal.title} (${savedGoal.id})")
        return GoalResponse.fromEntity(savedGoal)
    }

    override fun update(id: String, request: UpdateGoalRequest): GoalResponse? {
        goalRepository.findById(id) ?: return null

        val now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)

        val updates = mutableMapOf<String, Any>("updatedAt" to now)

        request.title?.let { updates["title"] = it }
        request.description?.let { updates["description"] = it }
        request.targetAmount?.let { updates["targetAmount"] = it }
        request.currentAmount?.let { updates["currentAmount"] = it }
        request.startDate?.let { updates["startDate"] = it }
        request.endDate?.let { updates["endDate"] = it }
        request.status?.let { updates["status"] = it.name }
        request.imageUrl?.let { updates["imageUrl"] = it }
        request.active?.let { updates["active"] = it }

        val updatedGoal = goalRepository.update(id, updates)
        logger.info("Goal updated: ${updatedGoal?.title} ($id)")
        return updatedGoal?.let { GoalResponse.fromEntity(it) }
    }

    override fun addAmount(id: String, amount: Double): GoalResponse? {
        if (amount <= 0) {
            throw IllegalArgumentException("Valor deve ser maior que zero")
        }

        val existing = goalRepository.findById(id) ?: return null

        val newCurrentAmount = existing.currentAmount + amount
        val now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)

        val updates = mutableMapOf<String, Any>(
            "currentAmount" to newCurrentAmount,
            "updatedAt" to now
        )

        if (newCurrentAmount >= existing.targetAmount && existing.status == GoalStatus.ACTIVE) {
            updates["status"] = GoalStatus.COMPLETED.name
            logger.info("Goal $id completed! Target amount reached.")
        }

        val updatedGoal = goalRepository.update(id, updates)
        logger.info("Added $amount to goal $id. New amount: $newCurrentAmount")
        return updatedGoal?.let { GoalResponse.fromEntity(it) }
    }

    override fun delete(id: String): Boolean {
        val goal = goalRepository.findById(id) ?: return false
        
        goal.imageUrl?.let {
            try {
                deleteImageFromStorage(id)
            } catch (e: Exception) {
                logger.warn("Failed to delete image for goal $id: ${e.message}")
            }
        }

        goalRepository.softDelete(id)
        logger.info("Goal soft deleted: $id")
        return true
    }

    override fun uploadImage(id: String, file: MultipartFile): String? {
        logger.info("Uploading image for goal: $id")

        val goal = goalRepository.findById(id)
            ?: throw IllegalArgumentException("Goal not found: $id")

        // Se já existe uma imagem, deletar a antiga antes de fazer upload da nova
        if (goal.imageUrl != null) {
            try {
                logger.info("Deleting old image before uploading new one for goal: $id")
                imageUploadService.deleteGoalImageByUrl(goal.imageUrl)
                logger.info("Old image deleted successfully")
            } catch (e: Exception) {
                logger.warn("Failed to delete existing image: ${e.message}")
            }
        }

        // Upload da nova imagem na pasta goals/
        val goalImage = imageUploadService.uploadGoalImage(id, file)
        val imageUrl = goalImage.url

        val now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
        goalRepository.update(id, mapOf("imageUrl" to imageUrl, "updatedAt" to now))

        logger.info("Image uploaded successfully for goal: $id - URL: $imageUrl")
        return imageUrl
    }

    override fun deleteImage(id: String): Boolean {
        logger.info("Deleting image for goal: $id")

        val goal = goalRepository.findById(id)
            ?: throw IllegalArgumentException("Goal not found: $id")

        if (goal.imageUrl == null) {
            throw IllegalArgumentException("Goal has no image to delete")
        }

        deleteImageFromStorage(id)

        val now = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT)
        goalRepository.update(id, mapOf<String, Any?>("imageUrl" to null, "updatedAt" to now))

        logger.info("Image deleted successfully for goal: $id")
        return true
    }

    private fun deleteImageFromStorage(goalId: String) {
        val prefix = "goals/$goalId/"
        imageUploadService.deleteImagesByPrefix(prefix)
    }
}
