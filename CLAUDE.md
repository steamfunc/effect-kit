# effect-kit

## 브랜치 정책

### 브랜치 구조
- `main` — 릴리즈. 태그 기준.
- `develop` — 통합 브랜치. feature들이 여기로 merge됨.
- `feature/issue-{번호}` — 이슈 단위 작업.
- `release/{version}` — 릴리즈 준비.
- `hotfix/{version}` — 긴급 수정.

### Feature 브랜치 워크플로우
1. `develop`에서 `feature/issue-{번호}` 브랜치 생성
2. 프로젝트 루트에 `context-{번호}.md` 생성
3. **context 파일 작성 완료 전까지 코딩 시작하지 않는다**
4. 작업 완료 후: context 파일 삭제 커밋 → rebase-squash → `develop`에 fast-forward merge

### Release / Hotfix
git flow 표준 방식 (squash 없이 merge commit 유지).

### Context 파일 규칙
- 위치: 프로젝트 루트 `context-{이슈번호}.md`
- 섹션: 목표 / Scope / 작업계획(체크박스) / 완료조건 / 이슈
- 인간과 AI가 함께 작성. 초안은 GitHub issue 기반.
- feature 브랜치 완료 시 삭제 — squash 직전 마지막 커밋으로 제거.

---

## 하네스: 코딩 가이드라인

**목표:** 모든 코딩 작업에 Karpathy 가이드라인을 적용하고, 설계·계획 검토 시 grill-me로 검증한다.

**트리거:**
- 코드 작성·수정·리뷰·디버깅 등 **모든 코딩 작업** → `karpathy-guidelines` 스킬을 반드시 적용한다. 예외 없음.
- 설계·계획·아키텍처 결정 검토 요청 시 → `grill-me` 스킬을 사용한다.

**변경 이력:**
| 날짜 | 변경 내용 | 대상 | 사유 |
|------|----------|------|------|
| 2026-05-08 | 초기 구성 | 전체 | - |
| 2026-05-08 | 브랜치 정책 추가, context 파일 자동 로딩 추가 | CLAUDE.md | - |

---

## Active Feature Context

!`if ls context-*.md >/dev/null 2>&1; then cat context-*.md; fi`
