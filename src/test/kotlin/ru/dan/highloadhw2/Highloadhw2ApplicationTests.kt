package ru.dan.highloadhw2

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import ru.dan.highloadhw2.config.TestRabbitConfig

@Testcontainers
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestRabbitConfig::class)
class Highloadhw2ApplicationTests {

	companion object {
		@Container
		val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
			withDatabaseName("testdb")
			withUsername("test")
			withPassword("test")
			withInitScript("init.sql") // опционально
		}

		@Container
		val rabbitmq = RabbitMQContainer("rabbitmq:3-management-alpine").apply {
			withExposedPorts(5672, 15672)
		}

		@JvmStatic
		@DynamicPropertySource
		fun properties(registry: DynamicPropertyRegistry) {
			registry.add("spring.r2dbc.url") {
				"r2dbc:postgresql://${postgres.host}:${postgres.getMappedPort(5432)}/testdb"
			}
			registry.add("spring.r2dbc.username") { postgres.username }
			registry.add("spring.r2dbc.password") { postgres.password }

			registry.add("spring.rabbitmq.host") { rabbitmq.host }
			registry.add("spring.rabbitmq.port") { rabbitmq.getMappedPort(5672) }
			registry.add("spring.rabbitmq.username") { "guest" }
			registry.add("spring.rabbitmq.password") { "guest" }
		}
	}

	@Autowired
	lateinit var webTestClient: WebTestClient

	@Autowired
	lateinit var objectMapper: ObjectMapper

	@Autowired
	lateinit var rabbitAdmin: RabbitAdmin

	private val exchange = "user.events.exchange"
	private val routingKey = "user.event"
	private val queue = "user.events.queue"

	@BeforeEach
	fun setUp() {
		rabbitAdmin.purgeQueue(queue, false)
	}
}