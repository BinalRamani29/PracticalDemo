package com.example.demoproject.utils.nullSafeExtensions

inline fun <reified T> T?.orDefault(): T {
    return this ?: when (T::class) {
        String::class -> "" as T
        Int::class -> 0 as T
        Double::class -> 0.0 as T
        Float::class -> 0f as T
        Long::class -> 0L as T
        Boolean::class -> false as T
        List::class -> emptyList<T>() as T
        ArrayList::class -> arrayListOf<T>() as T
        MutableList::class -> mutableListOf<T>() as T
        Set::class -> emptySet<T>() as T
        Map::class -> emptyMap<T, T>() as T
        else -> this ?: throw IllegalArgumentException("No default for type ${T::class}")
    }
}

inline fun <reified T> T?.orDefault(fallback: T): T {
    return this ?: fallback
}