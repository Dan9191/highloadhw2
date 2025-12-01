package ru.dan.highloadhw2.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.dan.highloadhw2.model.PersonCreateDto
import ru.dan.highloadhw2.model.PersonUpdateDto
import ru.dan.highloadhw2.model.PersonViewDto
import ru.dan.highloadhw2.model.UserEvent
import ru.dan.highloadhw2.model.UserEventType
import ru.dan.highloadhw2.repository.PersonRepository

@Service
class PersonService(
    private val personRepository: PersonRepository,
    private val mapper: PersonMapper,
    private val rabbitTemplate: RabbitTemplate
) {

    private val logger = LoggerFactory.getLogger(PersonService::class.java)
    private val exchangeName = "user.events.exchange"
    private val routingKey = "user.event"

    suspend fun findAll(): Flow<PersonViewDto> =
        personRepository.findAll().map { mapper.toViewDto(it) }

    suspend fun findById(id: Long): PersonViewDto =
        personRepository.findById(id)
            ?.let(mapper::toViewDto)
            ?: throw NoSuchElementException("User with id $id not found")

    @Transactional
    suspend fun create(dto: PersonCreateDto): PersonViewDto {
        if (dto.name.isBlank() || dto.mail.isBlank() || dto.password.isBlank()) {
            throw IllegalArgumentException("Name, mail and password must not be blank")
        }

        val entity = mapper.toEntity(dto)
        val saved = personRepository.save(entity.copy(id = null))
        logger.info("User created with id: {}", saved.id)

        sendUserEvent(UserEventType.CREATED, saved.id!!)
        return mapper.toViewDto(saved)
    }

    @Transactional
    suspend fun update(id: Long, dto: PersonUpdateDto): PersonViewDto {
        val existing = personRepository.findById(id)
            ?: throw NoSuchElementException("User with id $id not found")

        val updatedEntity = mapper.toEntity(dto, existing)
        val saved = personRepository.save(updatedEntity)

        logger.info("User $id updated")
        sendUserEvent(UserEventType.UPDATED, id)
        return mapper.toViewDto(saved)
    }

    @Transactional
    suspend fun delete(id: Long) {
        if (!personRepository.existsById(id)) {
            throw NoSuchElementException("User with id $id not found")
        }
        personRepository.deleteById(id)
        logger.info("User $id deleted")
        sendUserEvent(UserEventType.DELETED, id)
    }

    private fun sendUserEvent(type: UserEventType, userId: Long) {
        val event = UserEvent(type, userId)
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                rabbitTemplate.convertAndSend(exchangeName, routingKey, event)
                logger.debug("Event sent: {}", event)
            } catch (ex: Exception) {
                logger.error("Failed to send event to RabbitMQ", ex)
            }
        }
    }
}