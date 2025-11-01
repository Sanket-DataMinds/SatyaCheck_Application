package com.satyacheck.backend.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime

@Document(collection = "users")
data class User(
    @Id
    val id: String? = null,
    
    @Indexed(unique = true)
    private val username: String,
    
    private val password: String,
    
    @Indexed(unique = true)
    val email: String,
    
    val name: String,
    
    val roles: MutableSet<String> = mutableSetOf("ROLE_USER"),
    
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    
    val isActive: Boolean = true,
    
    val lastLoginAt: LocalDateTime? = null
) : UserDetails {
    
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return roles.map { role -> SimpleGrantedAuthority(role) }
    }
    
    override fun getPassword(): String {
        return password
    }
    
    override fun getUsername(): String {
        return username
    }
    
    override fun isAccountNonExpired(): Boolean {
        return isActive
    }
    
    override fun isAccountNonLocked(): Boolean {
        return isActive
    }
    
    override fun isCredentialsNonExpired(): Boolean {
        return true
    }
    
    override fun isEnabled(): Boolean {
        return isActive
    }
}