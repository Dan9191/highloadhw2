package ru.dan.highloadhw2.controller

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import jakarta.validation.Valid
import kotlinx.coroutines.flow.Flow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import ru.dan.highloadhw2.model.PersonCreateDto
import ru.dan.highloadhw2.model.PersonUpdateDto
import ru.dan.highloadhw2.model.PersonViewDto
import ru.dan.highloadhw2.service.PersonService

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val personService: PersonService,
    private val meterRegistry: MeterRegistry,
) {
    private val logger = LoggerFactory.getLogger(UserController::class.java)

    private suspend fun <T> timed(operation: String, block: suspend () -> T): T {
        val sample = Timer.start(meterRegistry)
        return try {
            val result = block()
            sample.stop(
                meterRegistry.timer(
                    "http.requests",
                    "operation", operation,
                    "status", "success",
                    "uri", "/api/v1/users"
                )
            )
            result
        } catch (ex: Exception) {
            sample.stop(
                meterRegistry.timer(
                    "http.requests",
                    "operation", operation,
                    "status", "error",
                    "uri", "/api/v1/users",
                    "exception", ex.javaClass.simpleName
                )
            )
            throw ex
        }
    }

    @GetMapping
    suspend fun getAll(): Flow<PersonViewDto> = timed("users.list") {
        logger.info("GET /api/v1/users — fetching all users")
        personService.findAll()
    }

    @GetMapping("/{id}")
    suspend fun getById(@PathVariable id: Long): PersonViewDto = timed("users.get") {
        logger.info("GET /api/v1/users/{} — fetching user", id)
        personService.findById(id)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(@Valid @RequestBody dto: PersonCreateDto): PersonViewDto = timed("users.create") {
        logger.info("POST /api/v1/users — creating user: {}", dto.name)
        personService.create(dto)
    }

    @PutMapping("/{id}")
    suspend fun update(
        @PathVariable id: Long,
        @Valid @RequestBody dto: PersonUpdateDto
    ): PersonViewDto = timed("users.update") {
        logger.info("PUT /api/v1/users/{} — updating user", id)
        personService.update(id, dto)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun delete(@PathVariable id: Long) = timed("users.delete") {
        logger.info("DELETE /api/v1/users/{} — deleting user", id)
        personService.delete(id)
    }
}