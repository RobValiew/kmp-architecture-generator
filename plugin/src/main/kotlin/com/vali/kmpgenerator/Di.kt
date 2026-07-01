package com.vali.kmpgenerator


enum class Di {
    NONE,
    KOIN;

    companion object {
        fun from(value: String): Di {
            return entries.firstOrNull { di ->
                di.name.equals(value, ignoreCase = true)
            } ?: error(
                "Unknown DI: $value. Available values: none, koin"
            )
        }
    }
}