# 백엔드를 배포 서버(EC2)에 다시 올리는 스크립트 (docs/deploy.md "코드를 고친 뒤 다시 올리기")
#
# 하는 일: 최신 main인지 확인 → jar 빌드 → 서버의 지금 jar 백업 → 새 jar 전송 → 재시작 → 켜졌는지 확인
#          2분 안에 켜지지 않으면 로그를 보여주고, 백업해 둔 jar로 자동으로 되돌린다
#
# 쓰는 법 (저장소 맨 위 폴더의 PowerShell에서):
#   powershell -ExecutionPolicy Bypass -File scripts\deploy-backend.ps1             # 다시 올리기
#   powershell -ExecutionPolicy Bypass -File scripts\deploy-backend.ps1 -Rollback   # 바로 전 버전으로 되돌리기
#
# 준비: ~/.ssh/config에 서버를 "triplan"이라는 이름으로 등록해 둔다 (docs/deploy.md 2번)
# 서버 주소 · 키 경로는 이 파일에 적지 않는다 (공개 저장소)
param(
	[string]$Server = "triplan", # ~/.ssh/config의 Host 이름
	[switch]$Rollback, # 새로 올리지 않고 백업해 둔 jar로 되돌린다
	[switch]$AllowNonMain # main이 아니거나 고친 파일이 있어도 올린다 (급할 때만. 리뷰 안 된 코드가 올라간다)
)

$AppDir = "/opt/triplan"
$Jar = "$AppDir/triplan.jar"
$Backup = "$AppDir/triplan.jar.bak"
# 서버 안에서 3초마다 API를 불러 200이 나오면 성공 (최대 2분). $를 PowerShell이 바꾸지 않게 작은따옴표로 둔다
$HealthCheck = 'for i in $(seq 1 40); do code=$(curl -s -o /dev/null -w %{http_code} http://127.0.0.1:8080/api/share-pages); if [ $code = 200 ]; then exit 0; fi; sleep 3; done; exit 1'

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

function ShowLogs($lines) {
	Write-Host ""
	Write-Host "--- 서버 로그 (최근 $lines 줄) ---" -ForegroundColor Yellow
	$null = Remote "sudo journalctl -u triplan -n $lines --no-pager"
}

# 백업해 둔 jar로 바꿔 끼우고 다시 켠다
function Restore {
	Step "백업해 둔 jar로 되돌립니다"
	if (-not (Remote "test -f $Backup")) {
		Fail "서버에 백업($Backup)이 없어서 되돌릴 수 없습니다."
	}
	if (-not (Remote "cp $Backup $Jar && sudo systemctl restart triplan")) {
		Fail "되돌리기에 실패했습니다. 서버에 접속해서 확인하세요."
	}
	Step "켜지기를 기다립니다 (최대 2분)"
	if (Remote $HealthCheck) {
		Write-Host "이전 버전으로 되돌렸습니다." -ForegroundColor Green
		return
	}
	ShowLogs 40
	Fail "이전 버전도 켜지지 않습니다. 서버 설정(application-secret.yaml)이나 DB를 확인하세요."
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

if ($Rollback) {
	Restore
	Pop-Location
	exit 0
}

# 1. 리뷰를 거쳐 main에 들어간 코드만 올린다
Step "최신 main인지 확인합니다"
$branch = git rev-parse --abbrev-ref HEAD
git fetch -q origin main
$behind = git rev-list --count HEAD..origin/main
$changed = git status --porcelain -- backend
Write-Host "브랜치: $branch / 받지 않은 main 커밋: $behind 개"
if (-not $AllowNonMain) {
	if ($branch -ne "main") {
		Fail "main 브랜치가 아닙니다. git switch main 후 git pull origin main을 하고 다시 실행하세요."
	}
	if ([int]$behind -gt 0) {
		Fail "최신 main이 아닙니다. git pull origin main을 하고 다시 실행하세요."
	}
	if ($changed) {
		Fail "backend에 커밋하지 않은 변경이 있습니다. 그대로 jar에 들어가므로 정리하고 다시 실행하세요."
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

# 3. 지금 서버에 있는 jar를 백업 (새 버전이 안 켜지면 이걸로 되돌린다)
Step "서버의 지금 jar를 백업합니다"
if (-not (Remote "if [ -f $Jar ]; then cp $Jar $Backup; fi")) {
	Fail "백업에 실패했습니다."
}

# 4. 새 jar 보내기
Step "새 jar를 서버로 보냅니다"
scp $built.FullName "${Server}:$Jar"
if ($LASTEXITCODE -ne 0) {
	Fail "jar를 보내지 못했습니다. 서버의 지금 버전은 그대로입니다."
}

# 5. 다시 켜고 확인 (켜지는 동안 30초~1분은 사이트가 502)
Step "백엔드를 다시 켭니다"
if (-not (Remote "sudo systemctl restart triplan")) {
	ShowLogs 40
	Restore
	Fail "다시 켜기에 실패해서 이전 버전으로 되돌렸습니다."
}
Step "켜지기를 기다립니다 (최대 2분)"
if (-not (Remote $HealthCheck)) {
	ShowLogs 60
	Restore
	Fail "새 버전이 켜지지 않아서 이전 버전으로 되돌렸습니다. 위 로그의 첫 ERROR 줄을 확인하세요."
}

ShowLogs 3
Write-Host ""
Write-Host "배포 완료. 문제가 생기면 -Rollback으로 바로 전 버전으로 되돌릴 수 있습니다." -ForegroundColor Green
Pop-Location
