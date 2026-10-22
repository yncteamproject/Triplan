#!/usr/bin/env bash
# 배포 전용 키(GitHub Actions)로 접속했을 때 실행되는 유일한 명령 (#92)
#
# 서버의 ~/.ssh/authorized_keys에 배포 키를 이렇게 등록한다 (docs/deploy.md "자동 배포"):
#   restrict,command="/opt/triplan/ci-deploy.sh" ssh-ed25519 AAAA... triplan-ci-deploy
# - restrict: 셸 · 포트 포워딩 · 에이전트 전달 등을 모두 막는다
# - command: 접속하면 무엇을 요청하든 이 스크립트만 실행된다. 요청한 명령은 $SSH_ORIGINAL_COMMAND로 들어온다
# 그래서 배포 키가 새어도 할 수 있는 일은 아래 세 가지뿐이다. 서버에 들어가 다른 명령을 실행할 수 없다.
#
#   ssh -i 배포키 ubuntu@서버 deploy < triplan.jar    jar를 표준 입력으로 받아 무중단 배포
#   ssh -i 배포키 ubuntu@서버 rollback                 바로 전 버전으로 무중단 되돌리기
#   ssh -i 배포키 ubuntu@서버 status                   지금 어느 칸이 서비스 중인지
#
# 전환 스크립트(blue-green.sh)는 이 키로 바꿀 수 없다. 사람이 리뷰된 버전을 직접 올린다 (scripts/deploy-backend.ps1).
# 백엔드는 sudo 권한이 없는 triplan 사용자로 실행되므로(deploy/triplan@.service), 악성 jar가 배포되더라도 서버 관리 권한은 없다.
set -euo pipefail

BLUE_GREEN=/opt/triplan/blue-green.sh
UPLOAD="/tmp/triplan-ci-$$.jar"
# 받을 jar 크기 상한 (지금 jar는 약 66MB)
MAX_BYTES=$((200 * 1024 * 1024))

log() { echo "[ci-deploy $(date +%H:%M:%S)] $*" >&2; }

trap 'rm -f "$UPLOAD"' EXIT

case "${SSH_ORIGINAL_COMMAND:-}" in
	deploy)
		# 표준 입력으로 들어온 jar를 받는다. 상한보다 크면 거부
		head -c $((MAX_BYTES + 1)) > "$UPLOAD"
		size=$(stat -c %s "$UPLOAD")
		if [ "$size" -gt "$MAX_BYTES" ]; then
			log "jar가 너무 큽니다 (${size} bytes). 배포하지 않습니다."
			exit 1
		fi
		# jar(zip)는 PK로 시작한다. 아니면 전송이 잘못된 것
		if [ "$(head -c 2 "$UPLOAD")" != "PK" ]; then
			log "받은 파일이 jar가 아닙니다 (${size} bytes). 배포하지 않습니다."
			exit 1
		fi
		log "jar 받음 ($((size / 1024 / 1024))MB)"
		"$BLUE_GREEN" deploy "$UPLOAD"
		;;
	rollback)
		"$BLUE_GREEN" rollback
		;;
	status)
		"$BLUE_GREEN" status
		;;
	*)
		log "허용되지 않은 요청입니다: '${SSH_ORIGINAL_COMMAND:-(셸 접속)}'. deploy · rollback · status만 됩니다."
		exit 1
		;;
esac
