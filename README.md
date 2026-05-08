# effect-kit

> AI agent coding 시대에, **"생성된 코드를 어떻게 믿을 수 있게 만드느냐"** 를 코드 자체에서 보장하는 Kotlin 어노테이션 + KSP 라이브러리.

## 호환성

| 항목 | 최솟값 | 권장 |
|------|--------|------|
| **Kotlin** | 2.0.0 | 최신 2.x |
| **KSP** | 2.x | 최신 2.x |
| **Gradle** | 6.8.3 | 8.x |
| **JVM target** | 8 | — |
| **Java runtime** | 8 | — |

### 결정 근거
- **Kotlin 2.0** — KSP 2.x stable 기준. KSP 1.x는 Kotlin 2.2부터 미지원(deprecated).
- **JVM target 8** — 라이브러리 사용층 최대화. `effect-annotations`는 `@Retention(SOURCE)`로 런타임에 남지 않고, `effect-processor`는 빌드 타임에만 실행되므로 런타임 Java 버전 제약이 없음.
- **Gradle 6.8.3** — Kotlin 2.0 Gradle Plugin 공식 최솟값. 현업 기준 실질적 최솟값은 8.x.
