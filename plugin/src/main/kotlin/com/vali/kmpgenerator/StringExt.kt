package com.vali.kmpgenerator

fun String.toPascalCase(): String {
    return split("-", "_", " ")
        .filter { part -> part.isNotBlank() }
        .joinToString(separator = "") { part ->
            part.replaceFirstChar { char ->
                char.uppercase()
            }
        }
}

fun String.toKebabCase(): String {
    return replace(Regex("([a-z])([A-Z])"), "$1-$2")
        .replace("_", "-")
        .replace(" ", "-")
        .lowercase()
}

fun String.toPackageSegment(): String {
    return toKebabCase()
        .replace("-", "")
        .replace("_", "")
        .lowercase()
}