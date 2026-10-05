package com.jarvis.v5

data class AgentContext(
    val userMessage: String,
    val recentConversation: List<String> = emptyList(),
    val rememberedFacts: List<String> = emptyList()
)
