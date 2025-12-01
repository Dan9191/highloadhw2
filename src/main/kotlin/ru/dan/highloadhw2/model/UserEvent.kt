package ru.dan.highloadhw2.model

import java.io.Serializable

data class UserEvent(
    val type: UserEventType,
    val userId: Long,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
