# EffectKit — 프로젝트 컨텍스트

> AI agent coding 시대에, **"생성된 코드를 어떻게 믿을 수 있게 만드느냐"** 를 코드 자체에서 보장하는 Kotlin 어노테이션 + KSP 라이브러리.

**이름 결정 배경**
- `Kontract` 후보였으나 Kotlin 표준 라이브러리의 `kotlin.contracts`와 혼동 가능성으로 제외
    - (참고: kotlin.contracts는 컴파일러에게 타입 추론 힌트를 주는 것, EffectKit은 side effect를 선언/강제하는 것으로 레이어가 다름)
- `Sidecar` 후보였으나 Kubernetes sidecar 패턴과 검색 네임스페이스 충돌 우려로 제외
- `EffectKit` — 직관적, 확장성 암시, 오픈소스 검색 노출에 유리

---

## 1. 왜 만드는가

### 배경
- AI가 코드를 생성하는 비중이 커지면서, 기존의 "사람이 쓰기 좋은" 언어 설계의 전제가 흔들리고 있다.
- 현재 업계는 **하네스(CLAUDE.md, .cursorrules, AGENTS.md 등)** — AI를 더 잘 부리는 방법에 집중하고 있다.
- 하지만 하네스가 아무리 정교해도 "AI가 생성한 코드가 실제로 믿을 수 있는가"는 별개의 문제다.

### 핵심 문제
```
현재:  AI → (하네스) → 코드 생성 → 사람이 검토
목표:  AI → 코드 생성 → (어노테이션 + KSP) → 컴파일러 1차 검증 → 사람이 검토
```

### 선행 연구 / 유사 프로젝트
- **Koka 언어** (2012~, Microsoft Research): Algebraic Effects로 side effect를 타입 시스템에 내장. 방향은 같지만 범용 언어를 목표로 해서 학술적으로 흘렀음.
- **Rust 소유권 시스템**: 단일 effect(메모리/동시성)를 타입 수준에서 강제하는 선례.
- **Haskell IO Monad**: 수학적으로 완벽하지만 진입장벽이 너무 높음.
- **현재 AI 코딩 도구들**: 자연어 제약(CLAUDE.md 등)으로 AI에게 "설명"하는 수준. 컴파일러 수준 강제 없음.

**우리 포지션**: 기존 Kotlin 생태계 위에 점진적으로 얹을 수 있는 실용적인 effect 어노테이션 시스템. 학술적 완벽함보다 백엔드 실무에서의 즉각적 유용성을 우선.

---

## 2. 핵심 아이디어

### Effect 어노테이션
함수의 **의도(Intent)** 와 **부작용(Side Effect)** 을 코드에 직접 표현:

```kotlin
@Intent("사용자 잔액 차감 후 트랜잭션 기록")
@IO(DB.WRITE, Network.WRITE)
@FailSafe(ROLLBACK_ALL)
suspend fun processPayment(user: User, amount: Money): Result<Receipt>
```

- `@Intent` — 사람 + AI가 읽는 의도 (KDoc과 보완 관계)
- `@IO` — 컴파일러가 강제하는 side effect 선언
- KSP가 컴파일 타임에 위반을 감지 → IDE에서 바로 빨간줄

### KDoc과의 차이
| | KDoc | 어노테이션 + KSP |
|---|---|---|
| AI 의도 전달 | ✅ 자연어라 풍부함 | △ 구조화되어 있지만 제한적 |
| 위반 강제 | ❌ 불가 | ✅ 컴파일 에러 |
| Effect 전파 추적 | ❌ 불가 | ✅ 자동 체크 |
| 코드베이스 분석 | ❌ 사람이 읽어야 함 | ✅ 기계적 집계 가능 |

→ **둘은 경쟁 관계가 아님. KDoc은 "왜", 어노테이션은 "무엇".**

---

## 3. 설계

### Effect 축 (직교 조합 방식)
각 축은 독립적으로 규칙을 가짐:

```kotlin
@Effects(
    io   = [DB.WRITE, Network.READ],  // IO 축
    conc = [ASYNC],                    // 동시성 축
    err  = [RESULT]                    // 실패 처리 축
)
fun processPayment(...): Result<Receipt>
```

### IO 축 (Lattice 구조)
```
         TOP
          |
         IO
        /    \
      DB      Network      FileIO
     /  \      /   \
  READ  WRITE READ  WRITE
          |
        PURE (bottom)
```

규칙: **선언한 effect ≥ 실제 호출한 effect** → OK, 그렇지 않으면 컴파일 에러.

```kotlin
// WRITE는 READ를 포함 → OK
@IO(DB.WRITE)
fun save() { db.findById(1); db.save(entity) }

// READ만 선언했는데 WRITE 호출 → 컴파일 에러
@IO(DB.READ)
fun getUser() { db.save(something) } // ❌
```

### 동시성 축
```kotlin
sealed class ConcEffect {
    object SYNC : ConcEffect()
    object ASYNC : ConcEffect()
    object BLOCKING : ConcEffect()
    sealed class ThreadSafety : ConcEffect() {
        object SAFE : ThreadSafety()
        object UNSAFE : ThreadSafety()
    }
}
```

축간 충돌 규칙:
```kotlin
conflictRules {
    BLOCKING conflicts ASYNC
    THREAD_UNSAFE warns_in ASYNC
}
```

### Effect 전파
핵심 메커니즘. Rust의 `async` 전파와 같은 원리:

```kotlin
// DB.WRITE를 호출하는 함수를 부르면 → 나도 DB.WRITE 선언해야 함
@IO(DB.READ)          // ← READ만 선언
fun process(id: Long) {
    saveLog()         // saveLog()가 DB.WRITE → 컴파일 에러!
}
```

### 어노테이션 누락 정책
점진적 도입을 위해 단계적으로:
- **1단계**: 누락 시 경고 (기존 레거시 코드 보호)
- **2단계**: 누락 시 에러 (팀 합의 후)

---

## 4. 프로젝트 구조

```
effectkit/
├── effect-annotations/     ← Effect 타입 + 어노테이션 정의
│   └── build.gradle.kts
├── effect-processor/       ← KSP 프로세서 (체크 로직)
│   └── build.gradle.kts
└── sample-app/             ← 백엔드 샘플 (Spring or Ktor)
    └── build.gradle.kts
```

모듈 분리 이유: KSP 프로세서가 어노테이션을 컴파일 타임에 읽으므로 순환 참조 방지.

---

## 5. 구현 로드맵

### 1단계: 어노테이션 정의
```kotlin
sealed class IOEffect {
    object PURE : IOEffect()
    sealed class DB : IOEffect() {
        object READ : DB()
        object WRITE : DB()
    }
    sealed class Network : IOEffect() {
        object READ : Network()
        object WRITE : Network()
    }
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class IO(vararg val effects: KClass<out IOEffect>)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Intent(val description: String)
```

### 2단계: KSP 프로세서 뼈대
```kotlin
class EffectProcessor(
    private val env: SymbolProcessorEnvironment
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // @IO 달린 함수 수집
        resolver.getSymbolsWithAnnotation("com.effectkit.IO")
            .filterIsInstance<KSFunctionDeclaration>()
            .forEach { checkEffects(it, resolver) }

        // 어노테이션 누락 경고
        resolver.getAllFiles()
            .flatMap { it.declarations }
            .filterIsInstance<KSFunctionDeclaration>()
            .filter { fn -> fn.annotations.none { it.shortName.asString() == "IO" } }
            .forEach { fn ->
                env.logger.warn("@IO 어노테이션이 없습니다", fn)
            }

        return emptyList()
    }
}
```

### 3단계: 충돌 체크
```kotlin
private fun checkEffects(fn: KSFunctionDeclaration, resolver: Resolver) {
    val declared = fn.getIOEffects()         // 선언된 effects
    val actual   = fn.body.collectEffects()  // 실제 호출된 effects

    val violations = actual - declared
    if (violations.isNotEmpty()) {
        env.logger.error(
            "선언되지 않은 effect가 호출됩니다: $violations",
            fn
        )
    }
}
```

### build.gradle.kts
```kotlin
// effect-processor
dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.0-1.0.13")
    implementation(project(":effect-annotations"))
}

// sample-app
plugins {
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
}
dependencies {
    implementation(project(":effect-annotations"))
    ksp(project(":effect-processor"))
}
```

---

## 6. 작업 순서 (추천)

```
Week 1:  effect-annotations 모듈
         IO sealed class + @IO, @Intent 어노테이션

Week 2:  KSP 프로세서 뼈대
         @IO 달린 함수 찾아서 이름 출력만 해도 성공

Week 3:  누락 경고
         @IO 없는 함수에 경고 메시지

Week 4:  충돌 체크
         선언 vs 실제 호출 비교 (가장 까다로운 부분)

Week 5+: 동시성 축 추가 (ASYNC/SYNC)
         축간 충돌 매트릭스
         문서 자동 생성
```

---

## 7. AI Rules 세트 (라이브러리와 함께 배포)

EffectKit은 라이브러리만 배포하는 게 아니라, **AI가 어노테이션을 자동으로 작성하도록 하는 rule 파일을 함께 배포**한다. 사용자가 rule 파일을 자기 프로젝트에 복사하면 셋업 완료.

### Rule vs Skill
- **Rule** → 코드 생성할 때마다 자동 적용되는 습관. EffectKit의 핵심.
- **Skill** → 명시적으로 요청할 때 실행. 레거시 코드 일괄 마이그레이션용 보조 도구.

### 지원 도구
- **Claude Code**: `.claude/rules/` 디렉토리 지원 (v2.0.64+). path frontmatter로 `.kt` 파일에만 scoped 적용 가능.
- **Cursor**: `.cursor/rules/*.mdc`

### Rule 파일 내용 (예시)
```markdown
---
paths:
  - "**/*.kt"
---
# EffectKit 어노테이션 규칙

## 항상 적용
- 새로 작성하는 모든 함수에 @IO 어노테이션 추가
- DB 접근이 있으면 DB.READ 또는 DB.WRITE 명시
- suspend 함수면 conc = [ASYNC] 추가
- @Intent는 함수 의도를 동사형 한 줄로 작성

## 전파 규칙
- 호출하는 함수의 effect를 내 @IO에 포함시킬 것
- DB.WRITE 호출 → 나도 DB.WRITE 선언

## 예시
@Intent("유저 조회")
@IO(DB.READ)
suspend fun getUser(id: Long): User?
```

### 최종 배포 구조
```
effectkit/
├── effect-annotations/         ← 라이브러리
├── effect-processor/           ← KSP 프로세서
├── sample-app/                 ← 예제
└── rules/
    ├── effectkit-claude.md     ← Claude Code용 (.claude/rules/ 에 복사)
    └── effectkit-cursor.mdc    ← Cursor용 (.cursor/rules/ 에 복사)
```

---

## 8. 핵심 결정사항 (논의 중 정해진 것들)

| 결정 | 이유 |
|---|---|
| 범용 언어가 아닌 Kotlin 어노테이션으로 시작 | 생태계를 처음부터 가져올 수 있음 |
| KSP (kapt 아님) | Kotlin 네이티브, 최대 2배 빠름, deprecated 방향 아님 |
| Lattice 구조로 effect 계층 설계 | 수학적으로 탄탄, 새 effect 추가 시 위치만 지정 |
| 직교 축 방식 (IO / 동시성 / 실패) | 축마다 독립적 규칙, 조합 가능 |
| 프로젝트 특화 → 범용 순서로 확장 | 현실적인 진입장벽 |
| 1단계는 문서 생성만 (검증 없음) | 점진적 도입, 레거시 코드 보호 |
| Rule 파일을 라이브러리와 함께 배포 | 도입 즉시 AI도 어노테이션 자동 작성, 진입장벽 제거 |

---

## 9. 레퍼런스

- [Koka Language](https://koka-lang.github.io/koka/doc/book.html) — Algebraic Effects 설계 영감
- [KSP 공식 문서](https://kotlinlang.org/docs/ksp-overview.html)
- [KSP GitHub](https://github.com/google/ksp)
- Rust 소유권 시스템 — 단일 effect 강제의 선례
- Java checked exception — 좋은 반면교사 (계층 없이 flat하게 만들어 실패)