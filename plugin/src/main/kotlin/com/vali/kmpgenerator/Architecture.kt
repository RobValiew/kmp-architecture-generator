package com.vali.kmpgenerator

enum class Architecture {
    MVI,
    MVVM;

    companion object {
        fun from(value: String): Architecture {
            return entries.firstOrNull { architecture ->
                architecture.name.equals(value, ignoreCase = true)
            } ?: error(
                "Unknown architecture: $value. Available values: mvi, mvvm"
            )
        }
    }
}