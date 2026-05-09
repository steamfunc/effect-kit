package io.github.steamfunc.effectkit.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType

/**
 * KSP processor that detects functions annotated with `@IO` and logs
 * their declared effects at compile time.
 *
 * This is the PoC phase: no violations are reported yet, only detection.
 * The output appears as `w: [EffectKit] @IO detected: ...` in the build log.
 *
 * ## Enum value extraction
 * KSP represents `vararg` annotation parameters as `ArrayList<*>` internally.
 * Each element for an enum parameter is a [KSType] whose declaration holds
 * the enum entry name — so `STORAGE_WRITE` comes back as
 * `(value as KSType).declaration.simpleName.asString()`.
 */
class EffectProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation("io.github.steamfunc.effectkit.annotations.IO")
            .filterIsInstance<KSFunctionDeclaration>()
            .forEach { fn ->
                val effects = fn.annotations
                    .first { it.shortName.asString() == "IO" }
                    .arguments
                    .flatMap { arg ->
                        @Suppress("UNCHECKED_CAST")
                        when (val v = arg.value) {
                            is ArrayList<*> -> v.toList()
                            else -> listOf(v)
                        }
                    }
                    .map { v -> (v as? KSType)?.declaration?.simpleName?.asString() ?: v.toString() }

                env.logger.warn("[EffectKit] @IO detected: ${fn.qualifiedName?.asString()} $effects")
            }

        return emptyList()
    }
}

/**
 * Registers [EffectProcessor] with the KSP runtime.
 * Declared in `META-INF/services/com.google.devtools.ksp.processing.SymbolProcessorProvider`.
 */
class EffectProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        EffectProcessor(environment)
}
