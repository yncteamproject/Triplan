# 백엔드를 배포 서버(EC2)에 다시 올리는 스크립트 (docs/deploy.md "코드를 고친 뒤 다시 올리기")
#
# 하는 일: 최신 main인지 확인 → jar 빌드 → 서버로 전송 → 서버의 blue-green.sh가 무중단으로 전환 (#92)
#          blue-green.sh: 쉬는 칸에 새 버전 실행 → 켜졌는지 확인 → Nginx 전환 → 옛 칸 종료
#          새 버전이 켜지지 않으면 전환하지 않는다. 사이트는 계속 옛 버전으로 동작한다
#
# 쓰는 법 (저장소 맨 위 폴더의 PowerShell에서):
#   powershell -ExecutionPolicy Bypass -File scripts\deploy-backend.ps1             # 다시 올리기
#   powershell -ExecutionPolicy Bypass -File scripts\deploy-backend.ps1 -Rollback   # 바로 전 버전으로 되돌리기
#   powershell -ExecutionPolicy Bypass -File scripts\deploy-backend.ps1 -Status     # 지금 어느 칸이 서비스 중인지
#
# 준비: ~/.ssh/config에 서버를 "triplan"이라는 이름으로 등록해 둔다 (docs/deploy.md 2번)
# 서버 주소 · 키 경로는 이 파일에 적지 않는다 (공개 저장소)
param(
	[string]$Server = "triplan", # ~/.ssh/config의 Host 이름
	[switch]$Rollback, # 새로 올리지 않고 바로 전 버전(쉬는 칸에 남은 jar)으로 전환한다
	[switch]$Status, # 아무것도 바꾸지 않고 서버 상태만 본다
	[switch]$AllowNonMain # main이 아니거나 고친 파일이 있어도 올린다 (급할 때만. 리뷰 안 된 코드가 올라간다)
)

$BlueGreen = "/opt/triplan/blue-green.sh"
$UploadJar = "/tmp/triplan-new.jar"

function Step($message) {
	Write-Host ""
	Write-Host "==> $message" -ForegroundColor Cyan
}

function Fail($message) {
	Write-Host ""
	Write-Host "[실패] $message" -ForegroundColor Red
	Pop-Location
	exit 1
}

# 서버에서 명령을 실행하고 성공했는지 돌려준다. 서버가 출력한 내용은 화면에 그대로 보여준다
function Remote($command) {
	ssh $Server $command | Out-Host
	return ($LASTEXITCODE -eq 0)
}

# 저장소의 deploy/blue-green.sh를 서버에 올린다. 리뷰를 거친 스크립트로 전환하도록 매번 맞춘다
function SyncBlueGreen {
	scp -q deploy\blue-green.sh "${Server}:/tmp/blue-green.sh"
	if ($LASTEXITCODE -ne 0) {
		Fail "blue-green.sh를 서버로 보내지 못했습니다."
	}
	if (-not (Remote "install -m 755 /tmp/blue-green.sh $BlueGreen && rm -f /tmp/blue-green.sh")) {
		Fail "blue-green.sh를 서버에 설치하지 못했습니다."
	}
}

# 어느 폴더에서 실행해도 저장소 맨 위 폴더를 기준으로 동작한다
Push-Location (Split-Path $PSScriptRoot -Parent)

if (-not (Get-Command ssh -ErrorAction SilentlyContinue)) {
	Fail "ssh를 찾지 못했습니다. Windows 설정 → 선택적 기능에서 OpenSSH 클라이언트를 켜세요."
}

Step "서버($Server)에 접속합니다"
if (-not (Remote "true")) {
	Fail "서버에 접속하지 못했습니다. ~/.ssh/config의 '$Server' 설정과 보안 그룹의 SSH(내 IP)를 확인하세요."
}

if ($Status) {
	$null = Remote "$BlueGreen status"
	Pop-Location
	exit 0
}

if ($Rollback) {
	Step "바로 전 버전으로 되돌립니다"
	if (-not (Remote "$BlueGreen rollback")) {
		Fail "되돌리지 못했습니다. 위 기록을 확인하세요. 사이트는 지금 버전으로 계속 동작합니다."
	}
	Pop-Location
	exit 0
}

# 1. 리뷰를 거쳐 main에 들어간 코드만 올린다
Step "최신 main인지 확인합니다"
$branch = git rev-parse --abbrev-ref HEAD
git fetch -q origin main
$behind = git rev-list --count HEAD..origin/main
$changed = git status --porcelain -- backend deploy
Write-Host "브랜치: $branch / 받지 않은 main 커밋: $behind 개"
if (-not $AllowNonMain) {
	if ($branch -ne "main") {
		Fail "main 브랜치가 아닙니다. git switch main 후 git pull origin main을 하고 다시 실행하세요."
	}
	if ([int]$behind -gt 0) {
		Fail "최신 main이 아닙니다. git pull origin main을 하고 다시 실행하세요."
	}
	if ($changed) {
		Fail "backend나 deploy에 커밋하지 않은 변경이 있습니다. 그대로 서버에 올라가므로 정리하고 다시 실행하세요."
	}
}
Write-Host ("올릴 커밋: " + (git log -1 --format="%h %s"))

# 2. jar 만들기
Step "jar를 만듭니다"
Push-Location backend
.\gradlew.bat bootJar -q
$buildCode = $LASTEXITCODE
Pop-Location
if ($buildCode -ne 0) {
	Fail "jar 빌드에 실패했습니다. 위의 오류를 확인하세요."
}
$built = Get-ChildItem backend\build\libs\*.jar | Where-Object { $_.Name -notlike "*-plain.jar" } | Sort-Object LastWriteTime -Descending | Select-Object -First 1
if (-not $built) {
	Fail "backend\build\libs에서 jar를 찾지 못했습니다."
}
Write-Host ("만든 jar: " + $built.Name + " (" + [math]::Round($built.Length / 1MB, 1) + "MB)")

# 3. 서버로 보내기 (서비스 중인 버전은 건드리지 않는다)
Step "jar와 전환 스크립트를 서버로 보냅니다"
SyncBlueGreen
scp $built.FullName "${Server}:$UploadJar"
if ($LASTEXITCODE -ne 0) {
	Fail "jar를 보내지 못했습니다. 서버의 지금 버전은 그대로입니다."
}

# 4. 무중단 전환 (새 칸 실행 → 확인 → Nginx 전환 → 옛 칸 종료)
Step "새 버전으로 전환합니다 (켜지는 동안에도 사이트는 옛 버전으로 동작)"
if (-not (Remote "$BlueGreen deploy $UploadJar")) {
	Fail "새 버전이 켜지지 않아 전환하지 않았습니다. 사이트는 옛 버전으로 계속 동작합니다. 위 로그의 첫 ERROR 줄을 확인하세요."
}

Write-Host ""
Write-Host "배포 완료. 문제가 생기면 -Rollback으로 바로 전 버전으로 되돌릴 수 있습니다." -ForegroundColor Green
Pop-Location
