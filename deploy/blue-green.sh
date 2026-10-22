#!/usr/bin/env bash
# 블루-그린 무중단 배포 (배포 서버에서 실행, #92)
#
# 백엔드 자리를 두 칸(8081 · 8082) 두고 번갈아 쓴다. Nginx에는 두 칸이 모두 등록되어 있고 설정은 고치지 않는다
# (deploy/nginx-triplan.conf). 꺼져 있는 칸으로 간 요청은 연결이 거부되고, Nginx가 바로 다른 칸으로 다시 보낸다.
#   ① 쉬는 칸에 새 버전 실행 → ② 켜졌는지 확인 → ③ 옛 칸 종료 (처리 중인 요청은 마치고 끝남, graceful shutdown)
# 새 칸이 켜지지 않으면 ③으로 가지 않는다. 옛 칸이 계속 켜져 있으므로 사용자는 끊김 없이 옛 버전을 쓴다.
#
# 처음에는 전환할 때 Nginx 설정을 바꾸고 reload했는데, reload 순간 쉬고 있던 연결(keep-alive)이 닫히면서
# 배포마다 요청 2건(0.48%)이 실패했다. 그래서 reload를 하지 않는 지금 방식으로 바꿨다.
#
# 쓰는 법 (서버 안에서, ubuntu 사용자):
#   /opt/triplan/blue-green.sh status                         지금 어느 칸이 서비스 중인지
#   /opt/triplan/blue-green.sh deploy /tmp/triplan-new.jar    새 jar를 쉬는 칸에 넣고 전환
#   /opt/triplan/blue-green.sh rollback                       바로 전 버전(쉬는 칸에 남아 있는 jar)으로 전환
#   /opt/triplan/blue-green.sh init                           처음 한 번: 예전 방식(triplan.service)에서 칸 방식으로 옮기기
#
# 내 컴퓨터에서는 scripts/deploy-backend.ps1이, GitHub Actions에서는 deploy 워크플로가 이 스크립트를 부른다.
set -euo pipefail

APP_DIR="${APP_DIR:-/opt/triplan}"
SLOTS_DIR="$APP_DIR/slots"
# 지금 서비스 중인 칸 번호를 적어 두는 파일 (Nginx는 두 칸을 다 보므로 여기서 기억한다)
STATE_FILE="${STATE_FILE:-$APP_DIR/active-slot}"
# 켜졌는지 확인할 주소 (조회수가 오르지 않는 목록 API. DB까지 다녀와서 "DB에도 연결됨"까지 확인된다)
HEALTH_PATH="/api/share-pages?size=1"
# 새 칸이 켜지기를 기다리는 최대 시간(초)
HEALTH_TIMEOUT="${HEALTH_TIMEOUT:-180}"

# 진행 기록은 표준 오류로 낸다. 함수 결과를 $( )로 받을 때 기록이 섞이지 않게
log() { echo "[$(date +%H:%M:%S)] $*" >&2; }
fail() { log "실패: $*"; exit 1; }

memory() {
	free -m | awk '/^Mem:/ { mem = $3 "/" $2 "MB" } /^Swap:/ { swap = $3 "MB" } END { print "메모리 " mem ", 스왑 " swap }'
}

active_port() {
	[ -f "$STATE_FILE" ] || fail "$STATE_FILE이 없습니다. 처음이면 init을 먼저 실행하세요."
	local port
	port=$(grep -oE '^(8081|8082)$' "$STATE_FILE" || true)
	[ -n "$port" ] || fail "$STATE_FILE에서 칸 번호(8081 · 8082)를 찾지 못했습니다."
	echo "$port"
}

other_port() {
	if [ "$1" = "8081" ]; then echo 8082; else echo 8081; fi
}

# 칸이 200으로 응답할 때까지 기다린다
# 돌려주는 값: 0 켜짐, 1 시간 초과, 2 켜지다가 죽음 (jar 오류 · 설정 오류 등)
wait_healthy() {
	local port="$1" started=$SECONDS code
	while (( SECONDS - started < HEALTH_TIMEOUT )); do
		code=$(curl -s -o /dev/null -m 5 -w '%{http_code}' "http://127.0.0.1:${port}${HEALTH_PATH}" || true)
		if [ "$code" = "200" ]; then
			return 0
		fi
		if ! systemctl is-active --quiet "triplan@${port}"; then
			return 2
		fi
		sleep 2
	done
	return 1
}

# 실패한 칸의 이번 실행 로그에서 원인이 될 만한 줄만 보여준다
# 이 출력은 GitHub Actions 로그(공개 저장소라 누구나 볼 수 있음)에도 남으므로 DB 접속 주소는 가린다
show_failure_log() {
	local port="$1" since="$2"
	sudo journalctl -u "triplan@${port}" --since "@${since}" --no-pager -o cat 2> /dev/null \
		| grep -E "ERROR|Caused by|Exception|Error:|APPLICATION FAILED|Description:|Reason:" \
		| grep -vE "^[[:space:]]+at " \
		| sed -E 's#(jdbc:)?postgresql://[^ ]*#(DB 주소 가림)#g; s#(password|secret|api-key|apiKey)[=:][^ ,]*#\1=(가림)#gI' \
		| cut -c1-300 | tail -15 >&2 || true
}

# from 칸이 서비스 중인 상태에서 to 칸을 띄우고 넘긴다. 새 칸이 켜지지 않으면 넘기지 않고 1을 돌려준다
start_and_switch() {
	local from="$1" to="$2" started since result=0
	log "새 칸 ${to} 실행 (지금 서비스 중: ${from}) · $(memory)"
	since=$(date +%s)
	# 바로 죽어도 여기서 멈추지 않고, 아래 확인에서 실패로 처리한다
	sudo systemctl restart "triplan@${to}" || true
	started=$SECONDS
	wait_healthy "$to" || result=$?
	if [ "$result" -ne 0 ]; then
		if [ "$result" -eq 2 ]; then
			log "새 칸 ${to}이 켜지다가 종료됐습니다 ($((SECONDS - started))초). 원인:"
		else
			log "새 칸 ${to}이 ${HEALTH_TIMEOUT}초 안에 켜지지 않았습니다. 마지막 로그:"
		fi
		show_failure_log "$to" "$since"
		sudo systemctl stop "triplan@${to}" || true
		log "전환하지 않았습니다. 옛 칸 ${from}이 계속 서비스합니다 (사용자 영향 없음)"
		return 1
	fi
	log "새 칸 ${to} 켜짐 ($((SECONDS - started))초). 잠시 두 칸이 함께 요청을 받습니다 · $(memory)"

	echo "$to" > "$STATE_FILE"
	# 옛 칸은 새 요청을 거부하고(Nginx가 새 칸으로 다시 보냄) 처리 중인 요청만 마친 뒤 끝난다
	sudo systemctl stop "triplan@${from}"
	# 서버가 재부팅되면 지금 서비스 중인 칸만 켜지게
	sudo systemctl disable --quiet "triplan@${from}" 2> /dev/null || true
	sudo systemctl enable --quiet "triplan@${to}"
	log "옛 칸 ${from} 종료. 이제 ${to}만 서비스합니다 · $(memory)"
}

command="${1:-status}"

case "$command" in
	status)
		active=$(active_port)
		idle=$(other_port "$active")
		echo "서비스 중: ${active} ($(systemctl is-active "triplan@${active}" || true))"
		echo "쉬는 칸:   ${idle} ($(systemctl is-active "triplan@${idle}" || true)), jar $( [ -f "$SLOTS_DIR/$idle/triplan.jar" ] && echo 있음 || echo 없음)"
		memory
		;;

	deploy)
		jar="${2:-}"
		[ -n "$jar" ] && [ -f "$jar" ] || fail "새 jar 경로를 넣어주세요. 예: $0 deploy /tmp/triplan-new.jar"
		active=$(active_port)
		idle=$(other_port "$active")
		mkdir -p "$SLOTS_DIR/$idle"
		# 쉬는 칸의 jar는 "그 전 버전"이다. 새 버전이 실패하면 되살려서 rollback을 계속 쓸 수 있게 한다
		[ -f "$SLOTS_DIR/$idle/triplan.jar" ] && mv "$SLOTS_DIR/$idle/triplan.jar" "$SLOTS_DIR/$idle/triplan.jar.prev"
		cp "$jar" "$SLOTS_DIR/$idle/triplan.jar"
		rm -f "$jar"
		if ! start_and_switch "$active" "$idle"; then
			if [ -f "$SLOTS_DIR/$idle/triplan.jar.prev" ]; then
				mv "$SLOTS_DIR/$idle/triplan.jar.prev" "$SLOTS_DIR/$idle/triplan.jar"
			else
				rm -f "$SLOTS_DIR/$idle/triplan.jar"
			fi
			exit 1
		fi
		rm -f "$SLOTS_DIR/$idle/triplan.jar.prev"
		log "배포 완료. 서비스 중: ${idle} (바로 전 버전은 ${active} 칸에 남아 있어 rollback으로 되돌릴 수 있음)"
		;;

	rollback)
		active=$(active_port)
		idle=$(other_port "$active")
		[ -f "$SLOTS_DIR/$idle/triplan.jar" ] || fail "쉬는 칸(${idle})에 jar가 없어서 되돌릴 버전이 없습니다."
		start_and_switch "$active" "$idle" || exit 1
		log "되돌리기 완료. 서비스 중: ${idle}"
		;;

	init)
		# 예전 방식(triplan.service가 /opt/triplan/triplan.jar를 8080으로 실행)에서 칸 방식으로 옮길 준비
		# 예전 서비스는 끄지 않는다. Nginx로 바꾼 뒤에 끈다 (docs/deploy.md 맨 아래 "기록")
		[ -f "$STATE_FILE" ] && fail "이미 칸 방식입니다 ($(active_port) 서비스 중)."
		[ -f "$APP_DIR/triplan.jar" ] || fail "$APP_DIR/triplan.jar가 없습니다."
		mkdir -p "$SLOTS_DIR/8081" "$SLOTS_DIR/8082"
		cp "$APP_DIR/triplan.jar" "$SLOTS_DIR/8081/triplan.jar"
		sudo systemctl daemon-reload
		log "8081 칸 실행 (예전 8080도 아직 켜져 있음) · $(memory)"
		sudo systemctl start triplan@8081
		started=$SECONDS
		wait_healthy 8081 || fail "8081 칸이 켜지지 않았습니다. sudo journalctl -u triplan@8081 -n 50 로 확인하세요."
		sudo systemctl enable --quiet triplan@8081
		echo 8081 > "$STATE_FILE"
		log "8081 칸 켜짐 ($((SECONDS - started))초) · $(memory)"
		;;

	*)
		fail "알 수 없는 명령: ${command} (status · deploy · rollback · init)"
		;;
esac
