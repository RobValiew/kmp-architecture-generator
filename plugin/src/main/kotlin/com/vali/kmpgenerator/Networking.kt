package com.vali.kmpgenerator


enum class Networking {
    NONE,
    KTOR;

    companion object {
        fun from(value: String): Networking {
            return entries.firstOrNull { networking ->
                networking.name.equals(value, ignoreCase = true)
            } ?: error(
                "Unknown networking: $value. Available values: none, ktor"
            )
        }
    }
}