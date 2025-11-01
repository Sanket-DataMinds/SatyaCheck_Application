package com.satyacheck.backend.model.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "articles")
data class Article(
    @Id
    val slug: String,
    val title: String,
    val description: String,
    val image: String,
    val imageHint: String = "",
    val category: String,
    val content: String,
    val language: String = "en"
)