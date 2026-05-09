package io.github.steamfunc.effectkit.annotations

enum class IOEffect {
    PURE,
    STORAGE_READ,
    STORAGE_WRITE,
    NETWORK_READ,
    NETWORK_WRITE,
    ENV_READ;

    val implies: Set<IOEffect> get() = when (this) {
        STORAGE_WRITE -> setOf(STORAGE_READ)
        NETWORK_WRITE -> setOf(NETWORK_READ)
        else          -> emptySet()
    }
}
