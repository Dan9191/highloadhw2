package ru.dan.highloadhw2.config

import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class RateLimitFilter(
    rateLimiterRegistry: RateLimiterRegistry
) : WebFilter {

    private val rateLimiter = rateLimiterRegistry.rateLimiter("global-api-limit")

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        // Применяем лимитер ко всем запросам на /api/v1/users/**
        if (exchange.request.path.value().startsWith("/api/v1/users")) {
            val permitted = rateLimiter.acquirePermission()
            if (!permitted) {
                exchange.response.statusCode = HttpStatus.TOO_MANY_REQUESTS
                exchange.response.headers.set("Content-Type", "application/json")
                val body = """{"error":"Too Many Requests","message":"Limit 1000 RPS"}"""
                return exchange.response.writeWith(Mono.just(exchange.response.bufferFactory().wrap(body.toByteArray())))
            }
        }
        return chain.filter(exchange)
    }
}