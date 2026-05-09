package io.github.steamfunc.effectkit.annotations

/**
 * Declares the IO side effects a function may produce.
 *
 * The KSP processor compares declared effects against actual calls in the body
 * and emits a compile error when undeclared effects are found.
 *
 * ```kotlin
 * @IO(STORAGE_WRITE)
 * fun save(user: User) { ... }
 *
 * @IO(NETWORK_READ, STORAGE_WRITE)
 * fun fetchAndSave(id: Long): User { ... }
 * ```
 *
 * [IOEffect.PURE] asserts no external interaction and must not be combined
 * with other effects — the processor treats such combinations as errors.
 *
 * Retention is [AnnotationRetention.SOURCE] because the constraint is enforced
 * entirely at compile time; no runtime representation is needed.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class IO(vararg val effects: IOEffect)

/**
 * Documents the intent of a function for human reviewers and AI code generators.
 *
 * Complements KDoc ("why") and [IO] ("what side effects") with a single-line
 * description of what the function is supposed to accomplish. AI rule files
 * distributed with EffectKit instruct code generators to write and respect this
 * annotation so intent is preserved across edits.
 *
 * ```kotlin
 * @Intent("사용자 잔액 차감 후 트랜잭션 기록")
 * @IO(STORAGE_WRITE)
 * fun processPayment(user: User, amount: Money): Result<Receipt>
 * ```
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Intent(val description: String)
