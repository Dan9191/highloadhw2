package ru.dan.highloadhw2.service

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Component
import ru.dan.highloadhw2.entity.Person
import ru.dan.highloadhw2.model.PersonCreateDto
import ru.dan.highloadhw2.model.PersonUpdateDto
import ru.dan.highloadhw2.model.PersonViewDto

@Component
class PersonMapper {

    fun toEntity(dto: PersonCreateDto): Person =
        Person(name = dto.name, mail = dto.mail, password = dto.password)

    fun toEntity(dto: PersonUpdateDto, existing: Person): Person =
        existing.copy(
            name = dto.name ?: existing.name,
            mail = dto.mail ?: existing.mail,
            password = dto.password ?: existing.password
        )

    fun toViewDto(entity: Person): PersonViewDto =
        PersonViewDto(
            id = entity.id!!,
            name = entity.name,
            mail = entity.mail
        )

    fun toViewDtoFlow(flow: Flow<Person>): Flow<PersonViewDto> =
        flow.map { toViewDto(it) }
}