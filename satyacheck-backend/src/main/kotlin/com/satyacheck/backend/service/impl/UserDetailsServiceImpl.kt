package com.satyacheck.backend.service.impl

import com.satyacheck.backend.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(
    private val userRepository: UserRepository
) : UserDetailsService {
    
    override fun loadUserByUsername(username: String): UserDetails {
        return userRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("User not found with username: $username") }
    }
}