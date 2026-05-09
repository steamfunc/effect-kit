#!/bin/bash
proj="/Users/oddpoet/SteamFunc/effect-kit"
path=$(jq -r '.tool_input.file_path // ""')
if [ -n "$path" ] && [[ "$path" != "$proj"* ]]; then
    echo "{\"continue\":false,\"stopReason\":\"프로젝트 외부 파일 변경 차단: $path\"}"
fi
