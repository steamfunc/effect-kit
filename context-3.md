# Context: #3 @IO 어노테이션 + IOEffect 타입 정의

## 목표
`effect-annotations` 모듈에 핵심 타입과 어노테이션을 정의한다.
`sample-app`에서 `@IO(STORAGE_WRITE)`를 붙일 수 있는 상태를 만든다.

## 설계 변경 노트
초기 계획은 `sealed class` 기반이었으나, 어노테이션 파라미터 타입 제약과 사용성 검토 후 **flat enum**으로 전환.

| 항목 | 변경 전 | 변경 후 | 이유 |
|---|---|---|---|
| 타입 구조 | `sealed class IOEffect` (중첩 계층) | `enum class IOEffect` (flat) | 어노테이션 파라미터로 enum 값 직접 사용 가능 |
| 어노테이션 파라미터 | `vararg KClass<out IOEffect>` | `vararg IOEffect` | `::class` 없이 `@IO(STORAGE_WRITE)` 로 깔끔하게 사용 |
| Effect 값 이름 | `DB.Read`, `Network.Write` 등 | `STORAGE_READ`, `NETWORK_WRITE` 등 | 추상화 수준 통일 (DB/File/Cache → STORAGE) |
| Lattice 표현 | 상속 구조로 표현 예정 | enum의 `implies` 프로퍼티 | KSP가 런타임 없이 읽을 수 있는 구조 |

## Scope
- ~~`IOEffect` sealed class 계층 정의 (IO 축만, PoC 범위)~~ → flat enum으로 전환
- `@IO` 어노테이션 정의
- `@Intent` 어노테이션 정의
- Lattice 구조 반영 방법 결정 (WRITE ≥ READ 관계를 코드에서 어떻게 표현할 것인가)
- `sample-app`에서 실제 사용 예시 작성

## 작업계획
- [x] ~~`IOEffect` sealed class 계층 작성~~ → flat enum으로 작성 (STORAGE_READ/WRITE, NETWORK_READ/WRITE, ENV_READ, PURE)
- [x] `@IO` 어노테이션 작성 (`@Target(FUNCTION)`, `@Retention(SOURCE)`)
- [x] `@Intent` 어노테이션 작성
- [x] Lattice 관계 표현 — enum의 `implies` 프로퍼티로 구현
- [x] `sample-app`에 사용 예시 코드 추가
- [x] `./gradlew build` 통과 확인

## 완료조건
- [ ] `effect-annotations` 모듈이 컴파일됨
- [ ] `sample-app`에서 `@IO`, `@Intent` 어노테이션 사용 가능
- [ ] Lattice 구조가 코드에 표현됨

## 이슈 / 논의사항
- Lattice 관계(WRITE ≥ READ)를 어떻게 표현할 것인가?
  - 옵션 A: 어노테이션 파라미터로만 선언, KSP가 Lattice 규칙을 별도 정의
  - 옵션 B: sealed class 상속 구조 자체에 반영 (WRITE가 READ를 extend)
  - 옵션 C: 별도 `lattice` 메타데이터 (companion object나 interface로 순서 정의)
  - **결정: enum의 `implies` 프로퍼티로 구현 (옵션 C 변형)**

- IOEffect 축 추가 후보 (완료 전 검토 필요, Codex 리뷰 기반):
  - `ENV_WRITE`: 환경변수/시스템 속성 변경 (현재 ENV_READ만 있어 비대칭)
  - `PROCESS_EXEC`: 서브프로세스 실행 — 중요한 보안 경계
  - `TIME_READ` 또는 `NONDETERMINISTIC_READ`: 시간/난수/UUID (순수성을 깨지만 IO는 아님)
  - `CONSOLE_READ` / `CONSOLE_WRITE`: CLI 코드의 stdin/stdout (STORAGE도 NETWORK도 아님)

- `PURE` 충돌 규칙: `@IO(PURE, NETWORK_READ)` 같은 모순 조합을 KSP에서 에러 처리해야 함
