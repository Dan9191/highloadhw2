package ru.dan.highloadhw2.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TestRabbitConfig {

    companion object {
        const val EXCHANGE_NAME = "user.events.exchange"
        const val QUEUE_NAME = "user.events.queue"
        const val ROUTING_KEY = "user.event"
    }

    @Bean
    fun userEventsExchange() = DirectExchange(EXCHANGE_NAME, true, false)

    @Bean
    fun userEventsQueue() = Queue(QUEUE_NAME, true, false, false)

    @Bean
    fun binding(queue: Queue, exchange: DirectExchange): Binding =
        BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY)

    // Это то, из-за чего всё падало
    @Bean
    fun rabbitAdmin(connectionFactory: ConnectionFactory): RabbitAdmin =
        RabbitAdmin(connectionFactory).apply {
            // автоматически объявит exchange/queue при старте контекста
            isAutoStartup = true
        }
}