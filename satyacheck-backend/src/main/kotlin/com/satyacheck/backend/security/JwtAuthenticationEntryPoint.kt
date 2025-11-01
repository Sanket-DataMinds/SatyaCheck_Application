package com.satyacheck.backend.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.satyacheck.backend.model.dto.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.util.logging.Logger

@Component
class JwtAuthenticationEntryPoint : AuthenticationEntryPoint {
    private val logger = Logger.getLogger(JwtAuthenticationEntryPoint::class.java.name)
    private val objectMapper = ObjectMapper()
    
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        logger.warning("Unauthorized error: ${authException.message}")
        
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        
        val body = ApiResponse.error<Any>("Unauthorized - ${authException.message}")
        
        objectMapper.writeValue(response.outputStream, body)
    }
}