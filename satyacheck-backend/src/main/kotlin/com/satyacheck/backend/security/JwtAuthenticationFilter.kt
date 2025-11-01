package com.satyacheck.backend.security

import com.satyacheck.backend.service.impl.JwtService
import com.satyacheck.backend.service.impl.UserDetailsServiceImpl
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.logging.Logger

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userDetailsService: UserDetailsServiceImpl
) : OncePerRequestFilter() {
    private val logger = Logger.getLogger(JwtAuthenticationFilter::class.java.name)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val jwt = parseJwt(request)
            if (jwt != null && jwtService.validateToken(jwt)) {
                val username = jwtService.getUsernameFromToken(jwt)
                val userDetails = userDetailsService.loadUserByUsername(username)
                
                val authentication = UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.authorities
                )
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: Exception) {
            logger.warning("Cannot set user authentication: ${e.message}")
        }
        
        filterChain.doFilter(request, response)
    }
    
    private fun parseJwt(request: HttpServletRequest): String? {
        val headerAuth = request.getHeader("Authorization")
        
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7)
        }
        
        return null
    }
}