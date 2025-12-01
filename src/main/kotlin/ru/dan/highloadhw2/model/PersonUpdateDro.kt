package ru.dan.highloadhw2.model

data class PersonUpdateDto(
    val name: String? = null,
    val mail: String? = null,
    val password: String? = null
)