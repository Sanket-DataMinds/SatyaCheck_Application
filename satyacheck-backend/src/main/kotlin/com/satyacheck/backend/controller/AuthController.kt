package com.satyacheck.backend.controller

import com.satyacheck.backend.model.dto.ApiResponse
import com.satyacheck.backend.model.dto.AuthResponse
import com.satyacheck.backend.model.dto.LoginRequest
import com.satyacheck.backend.model.dto.RefreshTokenRequest
import com.satyacheck.backend.model.dto.RegisterRequest
import com.satyacheck.backend.model.entity.User
import com.satyacheck.backend.repository.UserRepository
import com.satyacheck.backend.service.impl.JwtService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.util.UUID
import java.util.logging.Logger

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val authenticationManager: AuthenticationManager
) {
    private val logger = Logger.getLogger(AuthController::class.java.name)

    @PostMapping("/register")
    fun register(@Valid @RequestBody registerRequest: RegisterRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        // Check if username exists
        if (userRepository.existsByUsername(registerRequest.username)) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("Error: Username is already taken!"))
        }

        // Check if email exists
        if (userRepository.existsByEmail(registerRequest.email)) {
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("Error: Email is already in use!"))
        }

        // Create new user
        val user = User(
            id = UUID.randomUUID().toString(),
            username = registerRequest.username,
            password = passwordEncoder.encode(registerRequest.password),
            email = registerRequest.email,
            name = registerRequest.name
        )

        userRepository.save(user)
        logger.info("User registered successfully: ${user.username}")

        // Authenticate the user
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(registerRequest.username, registerRequest.password)
        )
        SecurityContextHolder.getContext().authentication = authentication

        // Generate JWT tokens
        val accessToken = jwtService.generateAccessToken(authentication)
        val refreshToken = jwtService.generateRefreshToken(user)

        val authResponse = AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresIn = jwtService.getAccessTokenExpirationMs(),
            username = user.username,
            roles = user.roles
        )

        return ResponseEntity.ok(ApiResponse.success(authResponse, "User registered successfully"))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password)
            )
            SecurityContextHolder.getContext().authentication = authentication

            val user = userRepository.findByUsername(loginRequest.username)
                .orElseThrow { RuntimeException("User not found") }

            // Update last login time
            userRepository.save(user.copy(lastLoginAt = LocalDateTime.now()))

            val accessToken = jwtService.generateAccessToken(authentication)
            val refreshToken = jwtService.generateRefreshToken(user)

            val authResponse = AuthResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresIn = jwtService.getAccessTokenExpirationMs(),
                username = user.username,
                roles = user.roles
            )

            logger.info("User logged in successfully: ${user.username}")
            return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"))
        } catch (e: Exception) {
            logger.warning("Login failed: ${e.message}")
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("Error: Invalid username or password"))
        }
    }

    @PostMapping("/refresh")
    fun refreshToken(@Valid @RequestBody refreshTokenRequest: RefreshTokenRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        try {
            // Validate refresh token
            if (!jwtService.validateToken(refreshTokenRequest.refreshToken)) {
                return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Error: Invalid refresh token"))
            }

            // Get username from refresh token
            val username = jwtService.getUsernameFromToken(refreshTokenRequest.refreshToken)

            // Get user from database
            val user = userRepository.findByUsername(username)
                .orElseThrow { RuntimeException("User not found") }

            // Generate new tokens
            val accessToken = jwtService.generateAccessToken(username)
            val refreshToken = jwtService.generateRefreshToken(user)

            val authResponse = AuthResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresIn = jwtService.getAccessTokenExpirationMs(),
                username = user.username,
                roles = user.roles
            )

            logger.info("Token refreshed successfully for user: ${user.username}")
            return ResponseEntity.ok(ApiResponse.success(authResponse, "Token refreshed successfully"))
        } catch (e: Exception) {
            logger.warning("Token refresh failed: ${e.message}")
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("Error: Failed to refresh token - ${e.message}"))
        }
    }
}