# Context: #4 KSP 프로세서 뼈대 — @IO 달린 함수 감지

## 목표
KSP 프로세서가 `@IO` 어노테이션이 달린 함수를 찾아 컴파일 로그에 출력한다.
"컴파일 타임에 무언가가 돌아간다"는 것을 확인하는 단계.

## Scope
- `EffectProcessor`에서 `@IO` 달린 `KSFunctionDeclaration` 수집
- 함수 이름 + 선언된 `IOEffect` 값 목록을 컴파일 경고(warn) 로그로 출력
- `SymbolProcessorProvider` 등록 확인 (이미 뼈대 존재)
- `sample-app`에서 `./gradlew build` 시 실제 로그 출력 확인

## 작업계획
- [x] `EffectProcessor.process()` 구현 — `@IO` 달린 함수 수집 및 로그 출력
- [x] `./gradlew :sample-app:build` 로 동작 확인

## 완료조건
- [x] `./gradlew :sample-app:build` 시 `@IO` 달린 함수 목록이 컴파일 로그에 출력됨
- [x] `UserService.kt`의 5개 함수가 모두 감지됨

## 이슈 / 논의사항
- KSP 로그 레벨: `warn` vs `info` — 기본 빌드 출력에 보이려면 `warn` 이상 필요 → `warn`으로 결정
- `@IO` 어노테이션의 `effects` 파라미터를 KSP에서 읽는 방법: `KSAnnotation.arguments`에서 `ArrayList<KSType>` 추출, 각 `KSType.declaration.simpleName`으로 enum 이름 획득

---

## 기타 작업 (이슈 외)

### 하네스 설정 (2026-05-10)

`.claude/settings.json`에 프로젝트 훅 추가:

| 훅 | 동작 |
|---|---|
| `PreToolUse` (Write\|Edit) | `file_path`가 프로젝트 루트 밖이면 차단 (`check-path.sh`) |
| `Stop` | 매 턴 종료 시 `./gradlew detekt test` 자동 실행 |

detekt `1.23.7` 프로젝트 전체 적용 (`libs.versions.toml` + 루트 `build.gradle.kts`).
기존 `WildcardImport` / `UnusedParameter` 위반 수정 완료.
