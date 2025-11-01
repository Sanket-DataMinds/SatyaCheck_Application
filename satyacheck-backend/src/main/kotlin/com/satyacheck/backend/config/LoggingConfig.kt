package com.satyacheck.backend.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

/**
 * Configuration for request logging and tracing
 */
@Configuration
class LoggingConfig {

    /**
     * Filter to add request ID to every request for tracing
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun requestIdFilter() = object : OncePerRequestFilter() {
        override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain
        ) {
            try {
                // Check if request already has an ID header
                val requestId = request.getHeader("X-Request-ID") ?: UUID.randomUUID().toString()
                
                // Add the request ID to the MDC context
                MDC.put("requestId", requestId)
                
                // Add user ID to MDC if authenticated
                request.userPrincipal?.let {
                    MDC.put("userId", it.name)
                }
                
                // Add request ID to response headers
                response.addHeader("X-Request-ID", requestId)
                
                // Continue with the filter chain
                filterChain.doFilter(request, response)
            } finally {
                // Clean up the MDC context
                MDC.remove("requestId")
                MDC.remove("userId")
            }
        }
    }
}