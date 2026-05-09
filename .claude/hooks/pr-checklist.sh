#!/bin/bash
command=$(jq -r '.tool_input.command // ""')
if echo "$command" | grep -q 'gh pr create'; then
    echo '{
      "hookSpecificOutput": {
        "hookEventName": "PostToolUse",
        "additionalContext": "PR 생성 완료. 아직 완료하지 않은 항목을 즉시 처리하라:\n- [ ] GitHub Project 상태 → Review\n- [ ] GitHub issue에 작업 결과 요약 코멘트 작성\n이 두 항목을 완료하기 전까지 다른 작업으로 넘어가지 않는다."
      }
    }'
fi
