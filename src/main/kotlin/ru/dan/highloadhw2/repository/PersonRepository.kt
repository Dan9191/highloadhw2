package ru.dan.highloadhw2.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.dan.highloadhw2.entity.Person

interface PersonRepository : CoroutineCrudRepository<Person, Long>