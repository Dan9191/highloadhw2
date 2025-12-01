package ru.dan.highloadhw2.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitConfig {

    companion object {
        const val EXCHANGE_NAME = "user.events.exchange"
        const val QUEUE_NAME = "user.events.queue"
        const val ROUTING_KEY = "user.event"
    }

    @Bean
    fun userEventsExchange(): DirectExchange = DirectExchange(EXCHANGE_NAME)

    @Bean
    fun userEventsQueue(): Queue = Queue(QUEUE_NAME, true)

    @Bean
    fun binding(queue: Queue, exchange: DirectExchange): Binding =
        BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY)
}