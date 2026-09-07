package com.gokturk.ai

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val role: String = "assistant",
    val content: String = "",
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
