package com.satyacheck.backend.service.impl

import com.satyacheck.backend.model.entity.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.security.Key
import java.util.Date
import java.util.logging.Logger

@Service
class JwtService {
    private val logger = Logger.getLogger(JwtService::class.java.name)

    @Value("\${app.jwt.secret}")
    private lateinit var jwtSecret: String

    @Value("\${app.jwt.expiration-ms}")
    private var jwtExpirationMs: Int = 0

    @Value("\${app.jwt.refresh-expiration-ms}")
    private var jwtRefreshExpirationMs: Int = 0

    /**
     * Generate a JWT access token for a user
     */
    fun generateAccessToken(authentication: Authentication): String {
        val userPrincipal = authentication.principal as UserDetails
        return generateAccessToken(userPrincipal.username)
    }

    /**
     * Generate a JWT access token for a username
     */
    fun generateAccessToken(username: String): String {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(Date())
            .setExpiration(Date(Date().time + jwtExpirationMs))
            .signWith(key())
            .compact()
    }

    /**
     * Generate a JWT refresh token for a user
     */
    fun generateRefreshToken(user: User): String {
        return Jwts.builder()
            .setSubject(user.username)
            .setIssuedAt(Date())
            .setExpiration(Date(Date().time + jwtRefreshExpirationMs))
            .signWith(key())
            .compact()
    }

    /**
     * Extract username from JWT token
     */
    fun getUsernameFromToken(token: String): String {
        val claims = Jwts.parserBuilder()
            .setSigningKey(key())
            .build()
            .parseClaimsJws(token)
            .body
        return claims.subject
    }

    /**
     * Validate a JWT token
     */
    fun validateToken(token: String): Boolean {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
            return true
        } catch (e: SignatureException) {
            logger.info("Invalid JWT signature: ${e.message}")
        } catch (e: MalformedJwtException) {
            logger.info("Invalid JWT token: ${e.message}")
        } catch (e: ExpiredJwtException) {
            logger.info("JWT token is expired: ${e.message}")
        } catch (e: UnsupportedJwtException) {
            logger.info("JWT token is unsupported: ${e.message}")
        } catch (e: IllegalArgumentException) {
            logger.info("JWT claims string is empty: ${e.message}")
        }
        return false
    }

    /**
     * Get JWT token expiration date
     */
    fun getExpirationFromToken(token: String): Date {
        val claims = getAllClaimsFromToken(token)
        return claims.expiration
    }

    /**
     * Get all claims from a token
     */
    fun getAllClaimsFromToken(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(key())
            .build()
            .parseClaimsJws(token)
            .body
    }

    /**
     * Check if token is expired
     */
    fun isTokenExpired(token: String): Boolean {
        val expiration = getExpirationFromToken(token)
        return expiration.before(Date())
    }

    /**
     * Get token expiration in milliseconds
     */
    fun getAccessTokenExpirationMs(): Long {
        return jwtExpirationMs.toLong()
    }

    /**
     * Creates signing key from the secret
     */
    private fun key(): Key {
        return Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }
}