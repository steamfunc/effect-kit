package io.github.steamfunc.effectkit.annotations

/**
 * Side effect axes for IO operations.
 *
 * Designed as a flat enum so values can be used directly as annotation parameters
 * without `::class` syntax (e.g. `@IO(STORAGE_WRITE)`).
 *
 * ## Lattice
 * `WRITE` subsumes `READ` on the same axis — declaring `STORAGE_WRITE` covers
 * `STORAGE_READ` as well. The [implies] property encodes this relationship for
 * KSP to resolve at compile time.
 *
 * ## Axis groupings
 * - **STORAGE**: any persistent store — DB, file, cache, etc.
 * - **NETWORK**: any outbound network call — HTTP, gRPC, MQ, etc.
 * - **ENV**: non-deterministic system sources — clock, random, environment variables.
 * - **PURE**: no external interaction; safe to call multiple times with the same result.
 */
enum class IOEffect {
    PURE,
    STORAGE_READ,
    STORAGE_WRITE,
    NETWORK_READ,
    NETWORK_WRITE,
    ENV_READ;

    /**
     * Effects that are implicitly covered when this effect is declared.
     *
     * Used by the KSP processor to resolve lattice subsumption:
     * a function declaring [STORAGE_WRITE] is also allowed to perform [STORAGE_READ].
     */
    val implies: Set<IOEffect> get() = when (this) {
        STORAGE_WRITE -> setOf(STORAGE_READ)
        NETWORK_WRITE -> setOf(NETWORK_READ)
        else          -> emptySet()
    }
}
