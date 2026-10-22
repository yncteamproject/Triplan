# 배포 가이드

Triplan을 인터넷 주소로 열 수 있게 올리는 방법입니다. 처음 한 번 하는 설정, 코드를 고친 뒤 다시 올리는 방법(무중단), 문제가 생겼을 때 확인할 것을 순서대로 적었습니다.

> [!WARNING]
> 이 저장소는 **공개**입니다. 서버 IP, 접속 키(`.pem`), 비밀번호, API 키, DB 접속 주소를 이 문서나 이슈 · PR · 커밋에 적지 않습니다. 아래의 `{중괄호}`는 각자 실제 값으로 바꿔서 쓰고, 바꾼 값은 저장소에 올리지 않습니다.

> [!TIP]
> `{IP}`, `{이름}`, `{프로젝트}`는 **중괄호까지 지우고** 실제 값을 넣는 자리입니다. 예: `ubuntu@{IP}` → `ubuntu@13.125.10.20`(예시 숫자). PowerShell에서 중괄호를 남긴 채 실행하면 `inputFormat 매개 변수에 대해 잘못된 값` 같은 엉뚱한 오류가 납니다. (Nginx 설정 파일 안의 `{ }`는 문법이라 그대로 둡니다.)

## 구성

```
브라우저
 ├─ 화면  https://{프로젝트}.vercel.app  → Vercel (frontend 폴더를 빌드해서 제공, main에 merge되면 자동 배포)
 └─ API   https://{이름}.duckdns.org     → AWS EC2 (시드니, 고정 IP)
                                            ├─ Nginx: https(Certbot 인증서), 요청을 백엔드 칸으로 전달
                                            ├─ 백엔드 칸 8081 ┐ 평소에는 한 칸만 켜져 있음 (블루-그린)
                                            └─ 백엔드 칸 8082 ┘ 서버 안(127.0.0.1)에서만 열림
                                                 └─ DB: Neon PostgreSQL (시드니, 서버와 같은 지역)
```

| 부분 | 사용 | 고른 이유 |
|------|------|-----------|
| 화면 | Vercel | GitHub에 연결해 두면 `main`에 merge될 때마다 자동으로 다시 배포 |
| 백엔드 | AWS EC2 `t3.small` + 고정 IP(Elastic IP) | 오디세이는 **등록한 IP에서만** 동작해서 IP가 바뀌지 않는 서버가 필요. 메모리 2GB는 배포할 때 백엔드 두 개를 잠깐 같이 띄우기 위해 |
| 도메인 | DuckDNS (무료) | https 인증서는 IP가 아니라 도메인에 발급된다 |
| https · 전달 | Nginx + Certbot | 무중단 배포(두 칸 전환)를 설정 한 곳에서 다루고, 실무에서 가장 많이 쓰는 웹 서버. 처음에는 Caddy를 썼다 (아래 "기록") |
| 무중단 배포 | 블루-그린 (`deploy/blue-green.sh`) | 끄고 켜는 방식은 배포마다 24~32초 동안 사이트가 꺼졌다 |
| 자동 배포 | GitHub Actions + 최소 권한 AWS 키 + 배포 전용 SSH 키 | merge만 하면 백엔드도 배포. AWS 키는 SSH 규칙 넣고 빼기만, 서버 열쇠는 배포 명령만 가능 (아래 "자동 배포") |
| DB | Neon (시드니) | 서버와 같은 지역이어야 쿼리마다 왕복 시간이 붙지 않는다 (K12) |

화면(https)에서 API를 부르려면 API도 https여야 합니다. 브라우저가 https 화면에서 http 주소를 부르는 것을 막기 때문입니다. 그래서 백엔드에 도메인과 인증서를 붙입니다.

### 지금 서버 (2026-10-22 기준)

| 항목 | 값 |
|------|----|
| 지역 | 아시아 태평양(시드니) `ap-southeast-2`. AWS 콘솔도 이 지역으로 봐야 서버가 보인다 |
| 인스턴스 | `t3.small` (메모리 2GB, CPU 2개). 처음엔 `t3.micro`(1GB)였다 |
| OS · 디스크 | Ubuntu 26.04, 디스크 약 8GB (파일 시스템 6.7GB), 스왑 2GB |
| 프로그램 | Java 21, Nginx 1.28, Certbot 4.0 |
| 시간대 | 운영체제는 UTC, **백엔드는 한국 시간** (`deploy/triplan@.service`의 `TZ=Asia/Seoul`, K13) |
| DB | Neon, 시드니 `ap-southeast-2` |

### 저장소의 배포 관련 파일

| 파일 | 하는 일 |
|------|---------|
| `backend/src/main/resources/application-prod.yaml` | 서버 전용 설정. SQL 로그 · H2 콘솔 끄기, 서버 안(127.0.0.1)에서만 열기, **graceful shutdown**(끌 때 처리 중인 요청을 최대 20초 마저 처리) |
| `backend/src/main/resources/application.yaml` | 실행 폴더의 `application-secret.yaml`도 읽음. CORS 허용 주소를 설정값(`app.cors.allowed-origins`)으로 받음 |
| `backend/build.gradle` | 배포용 jar에 `application-secret.yaml`을 넣지 않음 |
| `frontend/vercel.json` | `/trips/3` 같은 주소에서 새로고침해도 404가 나지 않게 모든 주소를 `index.html`로 보냄 |
| `deploy/nginx-triplan.conf` | Nginx 설정: https, http → https, 백엔드 두 칸 등록 |
| `deploy/nginx-acme-only.conf` | 인증서를 처음 받을 때만 잠깐 쓰는 http 전용 설정 |
| `deploy/triplan@.service` | 백엔드 칸(8081 · 8082)을 서비스로 띄우는 설정. sudo 권한 없는 `triplan` 사용자로 실행 |
| `deploy/blue-green.sh` | 서버에서 무중단 전환을 하는 스크립트 (`status` · `deploy` · `rollback` · `init`) |
| `deploy/ci-deploy.sh` | 배포 전용 키로 접속했을 때 실행되는 유일한 명령. `deploy` · `rollback` · `status`만 허용 |
| `.github/workflows/deploy-backend.yml` | main에 merge되면 백엔드를 자동으로 무중단 배포 (GitHub Actions) |
| `deploy/application-secret.example.yaml` | 서버에 둘 비밀 값 파일의 모양 |
| `scripts/deploy-backend.ps1` | 내 컴퓨터에서 jar를 만들어 서버로 보내고 `blue-green.sh`로 전환 |
| `scripts/measure-downtime.mjs` | 배포하는 동안 사이트가 끊기는지 재는 스크립트 |

각자 컴퓨터에서 실행하는 방법(`./gradlew bootRun`, `npm run dev`)은 바뀌지 않습니다.

---

## 처음 한 번: 서버 만들기

새 서버를 처음부터 만들 때의 순서입니다. 지금 서버는 이미 이 상태입니다.

### 1. EC2 만들기 (AWS 콘솔)

1. 오른쪽 위 지역을 고릅니다. 지금 서버는 **시드니(ap-southeast-2)**입니다. 다른 지역으로 만든다면 **DB(Neon)도 같은 지역**으로 맞춥니다.
2. EC2 → 인스턴스 시작
   - 이름: `triplan`
   - OS: **Ubuntu Server LTS** 최신 (지금 서버는 26.04), 아키텍처 64비트(x86)
   - 인스턴스 유형: **`t3.small`**(메모리 2GB). `t3.micro`(1GB)는 배포할 때 백엔드 두 개(각 약 350MB)가 같이 뜰 자리가 없다
   - 키 페어: 새로 만들기 → 이름 `triplan`, RSA, `.pem` → 받은 파일을 **저장소 폴더 밖**(`C:\Users\{나}\.ssh\triplan.pem`)에 둡니다. 다시 받을 수 없으니 따로 백업해 둡니다
   - 네트워크 설정(보안 그룹):

     | 종류 | 포트 | 허용할 곳 | 이유 |
     |------|------|-----------|------|
     | SSH | 22 | **내 IP** | 서버 접속. 모두에게 열지 않는다 |
     | HTTP | 80 | 모든 곳 | 인증서 발급, https로 넘기기 |
     | HTTPS | 443 | 모든 곳 | API |

     백엔드 포트(8081 · 8082)는 열지 않습니다. 백엔드는 Nginx를 통해서만 들어옵니다.
   - 스토리지: 기본값(8GB, gp3). "파일 시스템"(EFS · FSx)과 "볼륨 추가"는 서버가 한 대라 쓰지 않습니다
3. EC2 → 탄력적 IP → 탄력적 IP 주소 할당 → 만든 IP를 선택하고 작업 → **탄력적 IP 주소 연결** → 인스턴스 `triplan` 선택 ("재연결 허용"은 체크하지 않음)

이제 서버 IP가 바뀌지 않습니다. 이 IP를 아래에서 `{IP}`라고 씁니다.

> [!NOTE]
> 집 · 학교처럼 접속하는 곳이 바뀌면 "내 IP"도 바뀌어서 SSH가 시간 초과로 막힙니다. 그때는 **인스턴스의 보안 탭에 연결된 보안 그룹**에서 SSH(22) 줄의 소스를 "내 IP"로 다시 고르고 저장합니다. 지금 IP는 `curl.exe ifconfig.me`로 볼 수 있습니다.

### 2. 서버에 접속하기

Windows에 기본으로 있는 PowerShell의 `ssh`를 씁니다. 키 파일을 `.ssh` 폴더로 옮기고, 나만 읽을 수 있게 바꿉니다 (안 하면 `UNPROTECTED PRIVATE KEY FILE` 오류):

```powershell
New-Item -ItemType Directory -Force $env:USERPROFILE\.ssh
Move-Item $env:USERPROFILE\Downloads\triplan.pem $env:USERPROFILE\.ssh\
icacls $env:USERPROFILE\.ssh\triplan.pem /inheritance:r /grant:r "$($env:USERNAME):R"
```

매번 키 경로와 IP를 치지 않게 `notepad $env:USERPROFILE\.ssh\config`로 설정 파일을 만들고 아래 4줄을 넣습니다. `{IP}`는 중괄호 없이 숫자로 바꾸고, `config.txt`가 아니라 확장자 없는 `config`로 저장합니다. 이 파일은 내 컴퓨터에만 있습니다.

```
Host triplan
    HostName {IP}
    User ubuntu
    IdentityFile ~/.ssh/triplan.pem
```

이제 `ssh triplan` 한 줄로 접속합니다. 처음 한 번은 `yes`를 입력합니다. 프롬프트가 `ubuntu@ip-…`로 바뀌면 서버 안이고, `exit`로 나옵니다. 아래 명령들과 `scripts/deploy-backend.ps1`은 이 `triplan` 이름을 씁니다.

### 3. 서버 준비 (서버 안에서)

```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y openjdk-21-jre-headless nginx certbot
java -version
```

중간에 보라색 화면(재시작할 서비스 선택)이 나오면 그대로 Enter를 누릅니다. 인증서 자동 갱신 타이머가 켜져 있는지 확인합니다 (`active`):

```bash
systemctl is-active certbot.timer || sudo systemctl enable --now certbot.timer
```

메모리가 모자랄 때를 대비해 스왑(디스크를 메모리처럼 쓰는 공간) 2GB를 만듭니다:

```bash
sudo fallocate -l 2G /swapfile && sudo chmod 600 /swapfile && sudo mkswap /swapfile && sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

백엔드를 둘 폴더와, 백엔드를 실행할 **sudo 권한 없는 사용자** `triplan`을 만듭니다. `ubuntu`는 비밀번호 없이 sudo를 쓸 수 있어서, 백엔드를 `ubuntu`로 실행하면 jar 하나로 서버 전체를 가질 수 있기 때문입니다:

```bash
sudo mkdir -p /opt/triplan/slots/8081 /opt/triplan/slots/8082
sudo chown -R ubuntu:ubuntu /opt/triplan
sudo useradd --system --no-create-home --shell /usr/sbin/nologin triplan
```

### 4. 도메인 연결 (DuckDNS)

1. [duckdns.org](https://www.duckdns.org)에 로그인
2. 원하는 이름(`{이름}`)을 적고 add domain
3. current ip 칸의 값(지금 내 컴퓨터 IP)을 지우고 `{IP}`를 넣은 뒤 update ip

내 컴퓨터에서 연결됐는지 봅니다 (`{IP}`가 나오면 성공):

```powershell
nslookup {이름}.duckdns.org
```

### 5. 백엔드 올리기

최신 `main`을 받은 **내 컴퓨터**(저장소 맨 위 폴더, PowerShell)에서 jar를 만들고 필요한 파일을 서버로 보냅니다:

```powershell
cd backend
.\gradlew.bat bootJar
cd ..
scp backend/build/libs/triplan-0.0.1-SNAPSHOT.jar triplan:/opt/triplan/slots/8081/triplan.jar
scp deploy/application-secret.example.yaml triplan:/opt/triplan/application-secret.yaml
scp deploy/triplan@.service deploy/blue-green.sh triplan:/tmp/
```

`application-secret.example.yaml`은 빈칸만 있는 양식입니다. 각자 컴퓨터의 비밀 값 파일은 git에도 jar에도 들어가지 않아서 서버로 저절로 가지 않으므로, 서버에 따로 두고 채웁니다.

**서버 안**에서 비밀 값을 채웁니다:

```bash
nano /opt/triplan/application-secret.yaml
sudo chown ubuntu:triplan /opt/triplan/application-secret.yaml
sudo chmod 640 /opt/triplan/application-secret.yaml
```

`640`: 주인 `ubuntu`는 고칠 수 있고, 백엔드(`triplan`)는 읽기만, 다른 사용자는 못 읽습니다.

- DB: 각자 컴퓨터의 `application-secret.yaml`과 같은 `url` · `username` · `password` (아래 "DB" 참고)
  - `url`은 반드시 **`jdbc:postgresql://`**로 시작합니다. Neon 콘솔의 주소(`postgresql://계정:비밀번호@…`)를 그대로 붙이면 백엔드가 켜지지 못하고 계속 재시작합니다 (로그: `claims to not accept jdbcUrl, postgresql://…`)
  - 들여쓰기는 건드리지 말고 `:` 뒤의 값만 바꿉니다. 탭 문자는 쓰지 않습니다
- 오디세이 키: 서버의 고정 IP를 등록한 키
- JWT 키: 새로 만듭니다 → `openssl rand -base64 48`의 결과
- `allowed-origins`: Vercel 주소를 아직 모르면 일단 두고, 7번이 끝난 뒤 고칩니다

서비스를 등록하고 8081 칸을 켭니다:

```bash
sudo install -m 644 "/tmp/triplan@.service" /etc/systemd/system/triplan@.service
install -m 755 /tmp/blue-green.sh /opt/triplan/blue-green.sh
sudo systemctl daemon-reload
echo 8081 > /opt/triplan/active-slot
sudo systemctl enable --now triplan@8081
```

20초쯤 뒤 확인합니다 (`200`이 나오면 성공):

```bash
curl -s -o /dev/null -w "%{http_code}\n" "http://127.0.0.1:8081/api/share-pages?size=1"
```

### 6. https 열기 (Nginx + Certbot, 서버 안에서)

인증서가 아직 없으면 https 설정으로는 Nginx가 켜지지 않습니다. 그래서 **http만 여는 임시 설정으로 인증서를 먼저 받고**, 그다음 본 설정으로 바꿉니다. 내 컴퓨터에서 설정 파일 두 개를 보냅니다:

```powershell
scp deploy/nginx-acme-only.conf deploy/nginx-triplan.conf triplan:/tmp/
```

서버 안에서 (`{이름}`은 DuckDNS 이름으로):

```bash
sed "s/{이름}/{이름}/g" /tmp/nginx-acme-only.conf | sudo tee /etc/nginx/sites-available/triplan > /dev/null
sudo ln -sf /etc/nginx/sites-available/triplan /etc/nginx/sites-enabled/triplan
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t && sudo systemctl reload nginx
sudo certbot certonly --webroot -w /var/www/html -d {이름}.duckdns.org --deploy-hook "systemctl reload nginx"
```

Certbot은 이메일(인증서 만료 알림용), 약관 동의(`Y`), 홍보 메일 수신(선택)을 묻습니다. `Successfully received certificate`가 나오면 본 설정으로 바꿉니다:

```bash
sed "s/{이름}/{이름}/g" /tmp/nginx-triplan.conf | sudo tee /etc/nginx/sites-available/triplan > /dev/null
sudo nginx -t && sudo systemctl reload nginx
sudo certbot renew --dry-run
```

- `sed` 명령의 **두 번째** `{이름}`만 실제 이름으로 바꿉니다. 예: `sed "s/{이름}/triplan-ync/g" …`
- `--deploy-hook`: 인증서가 자동 갱신되면 Nginx가 새 인증서를 다시 읽게 합니다 (Certbot이 기억해 두었다가 갱신할 때마다 실행)
- `renew --dry-run`: 자동 갱신을 연습만 해 봅니다. `Congratulations, all simulated renewals succeeded`면 정상

브라우저에서 `https://{이름}.duckdns.org/api/share-pages`를 열어 글 목록(JSON)이 보이고 주소창에 자물쇠가 있으면 성공입니다.

### 7. 화면 올리기 (Vercel)

1. [vercel.com](https://vercel.com)에 GitHub 계정으로 로그인
2. Add New → Project → `yncteamproject/Triplan` 가져오기 (공개 저장소라 무료 요금제로 연결됩니다)
3. 설정
   - Root Directory: **`frontend`**
   - Framework: Vite (자동으로 잡힘). 빌드 명령 · 결과 폴더는 기본값
   - Environment Variables (Production은 꼭 체크):

     | 이름 | 값 |
     |------|----|
     | `VITE_API_BASE_URL` | `https://{이름}.duckdns.org` (끝에 `/` 없이) |
     | `VITE_KAKAO_MAP_KEY` | 카카오 JavaScript 키 |
4. Deploy → 대시보드의 Domains에 보이는 가장 짧은 주소가 `https://{프로젝트}.vercel.app`

`VITE_`로 시작하는 값은 **빌드할 때** 화면 코드에 들어갑니다. Vercel에서 값을 바꿨으면 Deployments → 최근 배포 → Redeploy를 해야 반영됩니다.

### 8. 서로 등록하기

| 어디에 | 무엇을 | 안 하면 |
|--------|--------|---------|
| 서버 `application-secret.yaml`의 `app.cors.allowed-origins` | `https://{프로젝트}.vercel.app` → 아래 "서버 설정값만 바꿨을 때"로 반영 | 화면에서 API 호출이 CORS 오류로 막힘 |
| 카카오 Developers → 앱 → 플랫폼 → Web | `https://{프로젝트}.vercel.app` 추가 (`http://localhost:5173`은 그대로 둠) | 지도 · 장소 검색이 안 됨 |
| 오디세이(ODsay LAB) → Server 플랫폼 | `{IP}` 등록 | 대중교통 경로가 502 |

### 9. 확인

`https://{프로젝트}.vercel.app`에서:

- [ ] 메인 · 게시판 목록이 열린다 (로그인 없이)
- [ ] 회원가입 → 로그인 → 여행 만들기
- [ ] `/trips/{번호}`에서 새로고침해도 화면이 나온다
- [ ] 장소 검색으로 방문지 추가 → 지도에 마커
- [ ] 이동 구간의 "대중교통 경로"
- [ ] 게시판 글쓰기 · 댓글 · 내 여행으로 복사

---

## 무중단 배포 (블루-그린)

백엔드 자리를 두 칸(8081 · 8082) 두고 번갈아 씁니다. **Nginx에는 두 칸이 모두 등록되어 있고, 배포할 때도 Nginx 설정은 고치지 않습니다.**

```
평소:       Nginx ─┬─ 8081 (서비스 중)
                   └─ 8082 (꺼짐) ← 여기로 간 요청은 연결 거부 → Nginx가 즉시 8081로 다시 보냄

배포:  ① 8082에 새 jar를 넣고 실행 (8081은 계속 서비스, 약 20초)
       ② 8082가 200으로 응답하는지 확인 (그동안 두 칸이 함께 요청을 받음)
       ③ 8081 종료: 새 요청은 거부하고(→ Nginx가 8082로) 처리 중인 요청만 마치고 끝남 (graceful shutdown)
       → 다음 배포는 반대로 (8081에 새 버전)
```

- **새 버전이 켜지지 않으면** ③으로 가지 않습니다. 옛 칸이 계속 서비스하므로 사용자는 아무것도 느끼지 못합니다. 실패한 jar는 치우고 쉬는 칸에는 그 전 버전 jar를 되살려 둡니다.
- **바로 전 버전**은 쉬는 칸에 남아 있어서 `rollback`으로 같은 방식(무중단)으로 되돌릴 수 있습니다.
- **서비스 중인 칸**은 `/opt/triplan/active-slot`에 적혀 있습니다. 재부팅하면 그 칸만 자동으로 켜집니다.

**Nginx 설정을 이렇게 한 이유** (`deploy/nginx-triplan.conf`)

| 설정 | 이유 |
|------|------|
| 두 칸을 모두 등록, 배포 때 reload 안 함 | 처음에는 전환할 때마다 설정을 바꾸고 reload했는데, reload 순간 쉬고 있던 연결(keep-alive)이 닫혀 배포마다 요청 2건이 실패했다 |
| `proxy_next_upstream error timeout http_502 http_503` | 꺼진 칸에서 연결이 거부되면 다른 칸으로 다시 보낸다. 연결 전에 거부된 요청은 POST도 안전하게 다시 보낸다 |
| `max_fails=0` | "실패한 칸을 몇 초 건너뛰기"를 끈다. 켜면 새 칸이 켜지는 동안 "죽은 칸"으로 기억된 채로 옛 칸을 끄는 순간 두 칸 모두 죽었다고 판단해 최대 5초 동안 502(`no live upstreams`)가 났다 |

**알고 있을 것**

- 새 칸이 켜진 직후부터 옛 칸이 꺼질 때까지 1~2초 동안 **두 버전이 함께 요청을 받습니다.** 두 버전이 같은 DB를 쓰므로, 테이블을 지우거나 이름을 바꾸는 변경은 피합니다 (컬럼 추가는 괜찮음).
- 새 버전은 **켜지는 순간부터** 일부 요청을 받습니다. Spring은 준비가 끝난 뒤 포트를 열어서 대부분 괜찮지만, "켜졌는데 오류를 내는" 버전이면 확인에 실패해 꺼질 때까지 일부 요청이 그 버전으로 갑니다.
- 오디세이 경로 캐시는 칸마다 메모리에 있어서, 배포하면 비워집니다.
- Nginx `error.log`에 `connect() failed (111: Connection refused)`가 계속 쌓이는 것은 **정상**입니다. 꺼진 칸에 먼저 연결해 본 요청마다 한 줄씩 남습니다. 로그는 매일 나눠 압축되고 오래된 것은 지워집니다(logrotate).

---

## 코드를 고친 뒤 다시 올리기

| 바뀐 것 | 할 일 |
|---------|-------|
| 화면(`frontend`) | 없음. `main`에 merge되면 Vercel이 자동으로 다시 배포 (팀원이 merge한 커밋이 자동 배포되지 않으면 Vercel 대시보드에서 Redeploy) |
| 백엔드(`backend`) | 없음. `main`에 merge되면 GitHub Actions가 자동으로 무중단 배포 (아래 "자동 배포"). 급할 때는 아래 스크립트로 직접 |
| 전환 스크립트(`deploy/blue-green.sh`) · 서버 설정 파일 | merge 후 사람이 직접 올림. 자동 배포 키로는 바꿀 수 없게 막아 두었음 (`deploy-backend.ps1`은 실행할 때 `blue-green.sh`도 같이 올림) |
| 서버 설정값(`application-secret.yaml`)만 | 서버에서 파일을 고친 뒤, 서비스 중인 jar를 그대로 다시 배포 (무중단, 아래 명령) |
| Vercel 환경 변수(`VITE_…`)만 | Vercel에서 값을 고치고 Redeploy (빌드할 때 들어가는 값이라 다시 빌드해야 반영) |

### 자동 배포 (GitHub Actions)

`.github/workflows/deploy-backend.yml`이 main에 `backend/`가 바뀐 커밋이 들어오면 자동으로 실행됩니다.

```
테스트 · jar 빌드 (실패하면 여기서 멈춤, 서버는 그대로)
 → 배포 전용 AWS 사용자로 로그인 (권한은 SSH 규칙 넣고 빼기뿐)
 → 보안 그룹에 "이 실행의 IP에서 SSH" 규칙 추가
 → 배포 전용 키로 접속해 jar를 보냄 → 서버의 ci-deploy.sh → blue-green.sh deploy (무중단)
 → 보안 그룹 규칙 삭제 (실패해도 반드시)
```

결과는 Actions 화면의 실행 기록 → Summary에 서버 출력과 함께 나옵니다. **Actions → 백엔드 배포 → Run workflow**에서 직접 실행할 수도 있습니다:

| 고르는 값 | 하는 일 |
|-----------|---------|
| `status` | 어느 칸이 서비스 중인지만 봄 (서버는 그대로) |
| `deploy` | 지금 main을 다시 배포 |
| `rollback` | 바로 전 버전으로 무중단 되돌리기 |

**권한을 이렇게 좁혀 두었습니다** (공개 저장소에서 서버를 자동으로 건드리므로)

| 열쇠 | 할 수 있는 일 | 새어도 |
|------|---------------|--------|
| AWS 액세스 키 (IAM 사용자 `triplan-github-deploy`) | 보안 그룹 하나에 SSH 규칙 넣기 · 빼기만. GitHub `production` 환경(main 브랜치)에서만 꺼낼 수 있음 | 누군가 자기 IP로 SSH 포트를 열 수는 있지만, 서버에 들어갈 SSH 키가 없어서 들어오지 못함. IAM에서 키를 비활성화 · 교체 |
| 배포 전용 SSH 키 | `deploy` · `rollback` · `status`만 (`restrict,command=` 로 묶음). 셸 · 임의 명령 · 파일 복사 · 포트 포워딩 불가 | jar를 배포할 수는 있지만, jar는 sudo 권한 없는 `triplan` 사용자로 실행됨. 전환 스크립트는 못 바꿈 |
| 서버 지문 (`known_hosts`) | — | 지문이 다른 서버(가짜)에는 접속하지 않음 |

열쇠를 이렇게 나눠 두어서 **하나만 새서는 서버에 아무것도 할 수 없습니다.** AWS 키와 배포 키가 함께 새야 jar를 배포할 수 있고, 그때도 jar는 sudo 없는 사용자로 실행됩니다.

> [!NOTE]
> 원래는 AWS 키를 저장하지 않는 **OIDC**(실행할 때마다 몇 분짜리 임시 권한)를 쓰려고 했습니다. 이 AWS 계정은 무료 요금제라 OIDC 공급자 등록이 막혀 있어서, 권한을 최소로 줄인 액세스 키로 바꿨습니다. 유료 요금제로 바꾸면 OIDC로 옮기는 것을 권합니다 (워크플로의 AWS 로그인 단계만 `role-to-assume`으로 바꾸면 됨).

**GitHub에 넣어 둔 값** (저장소 Settings → Environments → `production`, main 브랜치에서만 사용 가능)

| 이름 | 종류 | 내용 |
|------|------|------|
| `AWS_ACCESS_KEY_ID` | Secret | 배포 전용 IAM 사용자의 액세스 키 ID |
| `AWS_SECRET_ACCESS_KEY` | Secret | 같은 키의 비밀 액세스 키 (만들 때 한 번만 보임) |
| `EC2_SECURITY_GROUP_ID` | Secret | 서버의 보안 그룹 ID (`sg-…`) |
| `EC2_SSH_KEY` | Secret | 배포 전용 개인 키 (`triplan-ci-deploy`) 전체 |
| `AWS_REGION` | Variable | `ap-southeast-2` |
| `EC2_HOST` | Variable | `{이름}.duckdns.org` |
| `EC2_KNOWN_HOSTS` | Variable | `ssh-keyscan -t ed25519 {이름}.duckdns.org` 결과 한 줄 (지문을 기존 접속과 비교해 확인한 것) |

**처음 설정하는 법**

1. 배포 전용 키 만들기 (내 컴퓨터, 저장소 밖): `ssh-keygen -t ed25519 -N "" -C triplan-ci-deploy -f $env:USERPROFILE\.ssh\triplan-ci-deploy`
2. 서버의 `~/.ssh/authorized_keys`에 한 줄 추가: `restrict,command="/opt/triplan/ci-deploy.sh" ` + 공개 키(`triplan-ci-deploy.pub` 내용). `ci-deploy.sh`는 root 소유 `755`로 설치 (`sudo install -o root -g root -m 755 …`)
3. AWS IAM → 정책 → 정책 생성(JSON): `ec2:AuthorizeSecurityGroupIngress` · `ec2:RevokeSecurityGroupIngress`만, 리소스는 서버 보안 그룹 ARN 하나 (`arn:aws:ec2:ap-southeast-2:{계정ID}:security-group/{sg-…}`). 이름 `triplan-deploy-ssh-rule`
4. AWS IAM → IAM 사용자 → 사용자 생성: 이름 `triplan-github-deploy`, 콘솔 접근 없음, 권한은 위 정책만 직접 연결
5. 그 사용자 → 보안 자격 증명 → 액세스 키 만들기 (사용 사례: AWS 외부에서 실행되는 애플리케이션). 비밀 액세스 키는 이 화면에서만 보이므로 바로 GitHub Secrets에 넣고, 다른 곳에 저장하지 않음
6. GitHub → Settings → Environments → `production` 만들기 → Deployment branches를 `main`만 → 위 표의 값 등록

**AWS 키 교체** (사람이 바뀌었거나 새었다고 의심될 때, 또는 몇 달에 한 번): IAM 사용자 → 보안 자격 증명 → 새 액세스 키 만들기 → GitHub Secrets 두 값 교체 → Actions에서 `status`로 확인 → 옛 키 비활성화 → 삭제

### 백엔드: 스크립트로 올리기 (직접, 급할 때)

최신 `main`을 받은 뒤, 저장소 맨 위 폴더의 PowerShell에서 실행합니다.

```powershell
git switch main
git pull origin main
powershell -ExecutionPolicy Bypass -File scripts\deploy-backend.ps1
```

`scripts/deploy-backend.ps1`이 하는 일:

1. 서버(`ssh triplan`)에 접속되는지 확인
2. `main` 브랜치이고, 최신이고, `backend` · `deploy`에 커밋 안 한 변경이 없는지 확인 (아니면 멈춤. 리뷰 안 된 코드가 올라가지 않게)
3. jar 빌드
4. jar와 `deploy/blue-green.sh`를 서버로 보냄 (전환 스크립트도 리뷰된 버전으로 맞춤)
5. 서버의 `blue-green.sh deploy`로 무중단 전환 (위 "무중단 배포")

그 밖의 사용법:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\deploy-backend.ps1 -Status     # 어느 칸이 서비스 중인지
powershell -ExecutionPolicy Bypass -File scripts\deploy-backend.ps1 -Rollback   # 바로 전 버전으로 (무중단)
```

> [!NOTE]
> 테이블 · 컬럼을 바꾼 코드는 서버가 켜질 때 `ddl-auto: update`로 같은 DB에 바로 반영됩니다. 컬럼 추가는 자동이지만, 삭제 · 이름 변경은 자동으로 되지 않고 되돌리기(`-Rollback`)로도 DB는 원래대로 돌아가지 않습니다. 이런 변경은 PR에 적고 서로 알린 뒤 올립니다.

### 서버 설정값만 바꿨을 때 (무중단)

`application-secret.yaml`을 고친 뒤, 서비스 중인 jar를 쉬는 칸으로 복사해 다시 배포합니다. 새 설정은 새로 켜지는 칸에 적용됩니다:

```powershell
ssh triplan 'A=$(cat /opt/triplan/active-slot); cp /opt/triplan/slots/$A/triplan.jar /tmp/triplan-new.jar && /opt/triplan/blue-green.sh deploy /tmp/triplan-new.jar'
```

### 백엔드: 직접 올리기

스크립트가 하는 일을 손으로 하면 이렇습니다.

```powershell
cd backend
.\gradlew.bat bootJar
cd ..
scp backend/build/libs/triplan-0.0.1-SNAPSHOT.jar triplan:/tmp/triplan-new.jar
ssh triplan "/opt/triplan/blue-green.sh deploy /tmp/triplan-new.jar"
```

## 끊김 측정

배포 방식을 바꾸거나 서버 설정을 바꿨다면 실제로 끊기지 않는지 잽니다. 창을 두 개 씁니다.

```powershell
node scripts/measure-downtime.mjs --rps 10 --timeout 3000 --label 이름
```

측정이 시작되면 다른 창에서 배포하고, 끝나면 `Ctrl + C`. 요약(요청 수, 실패 수와 원인, 가장 긴 끊김, 응답 시간)이 나오고 `measurements/` 폴더에 결과가 남습니다(git에 올라가지 않음).

- 서버가 1초에 처리할 수 있는 양보다 적게 보냅니다. 많이 보내면 측정이 과부하를 만들어 배포와 상관없는 실패가 섞입니다.
- 한 번만 재지 말고 **세 번** 잽니다. 한 번은 운 좋게 0건이 나오기도 했습니다 (아래 "기록").

## 로그 보기 (서버 안에서)

```bash
sudo journalctl -u triplan@$(cat /opt/triplan/active-slot) -f
```

- `-f`: 새로 찍히는 로그를 계속 보여줌 (`Ctrl + C`로 나옴). 최근 것만 보려면 `-n 200`
- 대중교통이 502면 `오디세이 호출 실패`를 찾습니다 (원인별 설명은 [API 명세](api.md)의 오디세이 항목)
- Nginx: `sudo tail -f /var/log/nginx/access.log` (요청), `/var/log/nginx/error.log` (전달 실패 등)

## 문제가 생기면

| 증상 | 확인할 것 |
|------|-----------|
| 화면에서 모든 요청이 실패, 브라우저 콘솔에 `CORS` | `allowed-origins`가 Vercel 주소와 **정확히** 같은지 (`https`, 끝의 `/` 없음), 고친 뒤 다시 배포했는지 |
| 화면은 되는데 데이터가 이상함 | Vercel의 `VITE_API_BASE_URL`이 서버 주소인지 (localhost면 안 됨) |
| `https://{이름}.duckdns.org`가 안 열림 | 보안 그룹 80 · 443, DuckDNS의 IP, `systemctl status nginx` |
| 사이트가 502 | 두 칸 모두 꺼짐 → `/opt/triplan/blue-green.sh status`, 서비스 중인 칸의 로그 |
| 백엔드가 계속 재시작 (`activating`) | 로그의 첫 오류. 대부분 `application-secret.yaml` 값 오류 |
| 배포 사이트에서 쓴 글 · 댓글 시각이 9시간 이르게 보임 | 서비스 설정에 `Environment="TZ=Asia/Seoul"`이 있는지 (`systemctl cat triplan@8081`), 넣은 뒤 같은 jar를 다시 배포했는지 (K13) |
| 로그에 `claims to not accept jdbcUrl, postgresql://…` | DB `url` 앞에 `jdbc:`가 빠짐 |
| 배포했는데 "켜지다가 종료됐습니다" | 출력된 원인 줄 확인. 사이트는 옛 버전으로 계속 동작 중 |
| Nginx `error.log`에 `no live upstreams` | `nginx-triplan.conf`의 두 칸에 `max_fails=0`이 있는지 |
| Nginx `error.log`에 `connect() failed (111)` | 정상 (꺼진 칸에 먼저 연결해 본 기록) |
| 인증서가 만료됨 | `systemctl is-active certbot.timer`(active여야 함), `sudo certbot renew --dry-run` |
| AWS 콘솔에 서버가 안 보임 | 오른쪽 위 지역이 **시드니**인지 |
| 자동 배포가 `AWS 로그인`에서 실패 (`InvalidClientTokenId` · `SignatureDoesNotMatch`) | GitHub Secrets의 `AWS_ACCESS_KEY_ID` · `AWS_SECRET_ACCESS_KEY`가 맞는지, 키가 비활성화되지 않았는지. 값이 비어 있으면 `production` 환경의 Secrets인지 (저장소 Secrets가 아님) |
| 자동 배포가 `SSH 허용`에서 실패 (`UnauthorizedOperation`) | 사용자에 연결한 정책의 보안 그룹 ARN · 계정 ID · 지역이 맞는지 |
| 자동 배포가 `SSH 포트가 열리지 않았습니다` | `EC2_HOST` 값, 보안 그룹 ID가 서버에 연결된 그룹인지 |
| 자동 배포가 `Host key verification failed` | 서버 지문이 바뀜(서버를 새로 만든 경우 등). `EC2_KNOWN_HOSTS`를 다시 받아 기존 접속 지문과 비교 후 교체 |
| 자동 배포가 `허용되지 않은 요청입니다` | 정상적인 제한. 배포 키로는 `deploy` · `rollback` · `status`만 됨 |
| 보안 그룹에 `github-actions-deploy` 규칙이 남아 있음 | 배포가 중간에 강제로 끊긴 경우. 콘솔에서 그 규칙을 지움 |
| 백엔드가 `application-secret.yaml`을 못 읽음 (`Permission denied`) | 파일이 `ubuntu:triplan`, `640`인지 |
| `ssh`가 한참 멈췄다가 `Connection timed out` | 내 IP가 바뀜. 인스턴스 보안 탭의 보안 그룹에서 SSH 소스를 "내 IP"로 다시 |
| 새로고침하면 404 | `frontend/vercel.json`이 있는지, Root Directory가 `frontend`인지 |
| 지도가 안 나옴 | 카카오 Web 플랫폼에 Vercel 주소, Vercel의 `VITE_KAKAO_MAP_KEY`, Redeploy 했는지 |
| PowerShell에서 `inputFormat 매개 변수에 대해 잘못된 값` | 명령에 `{IP}`처럼 중괄호가 남아 있음. 중괄호까지 지우고 실제 값으로 |
| Vercel 가져오기 목록에 저장소가 없음 | GitHub에서 Vercel 앱이 `yncteamproject` 조직의 `Triplan`에 접근하도록 허용 (조직 소유자 승인 필요) |
| 스크립트 실행이 막힘 (`이 시스템에서 스크립트를 실행할 수 없으므로`) | `powershell -ExecutionPolicy Bypass -File …`로 실행 |

## DB

배포 서버는 **각자 컴퓨터의 `bootRun`과 같은 Neon DB**를 씁니다. DB는 서버와 같은 **시드니**에 있습니다.

- **같은 지역이어야 하는 이유**: 처음에는 DB가 싱가포르에 있어서 서버 → DB 왕복이 0.18초였고, 쿼리를 여러 번 보내는 목록 API가 서버 안에서만 1.6초 걸렸습니다. 시드니로 옮긴 뒤 왕복 0.004초, 목록 API 0.04초가 됐습니다 (K12, [#94](https://github.com/yncteamproject/Triplan/issues/94)).
- **각자 컴퓨터에서는**: 한국 → 시드니라 `bootRun`할 때는 응답이 조금 느릴 수 있습니다. 테스트는 H2를 써서 상관없습니다.

같은 DB라서 지켜야 할 것:

- 개발하면서 만든 데이터가 배포된 화면에 그대로 보입니다. 테스트용 글 · 여행은 확인이 끝나면 지웁니다.
- 테이블은 `ddl-auto: update`로 바뀝니다. **merge 전 브랜치를 `bootRun`하면 그 브랜치의 테이블 변경(컬럼 추가 등)이 배포 DB에도 바로 들어갑니다.** 테이블을 바꾸는 작업은 먼저 H2(테스트)로 확인하고, PR에 적어서 서로 알립니다.
- 비밀번호가 같은 DB를 가리키므로, 서버 파일이든 각자 파일이든 유출되면 둘 다 위험합니다.

## 비용과 정리

- AWS 무료 범위는 가입 시기와 요금제에 따라 다릅니다. 가입할 때 받은 크레딧이 있으면 먼저 쓰입니다. **결제 → 예산(Budgets)**에서 월별 예산 알림을 만들어 두면 예상보다 많이 쓸 때 메일로 알 수 있습니다.
- `t3.small`은 `t3.micro`의 약 두 배 요금입니다. 인스턴스 유형은 중지 → 작업 → 인스턴스 설정 → 인스턴스 유형 변경으로 바꿀 수 있습니다 (고정 IP와 서버 안의 파일은 그대로, 바꾸는 몇 분 동안은 꺼짐).
- 고정 IP(공인 IPv4)도 요금 대상입니다. 발표가 끝나 더 쓰지 않으면 **인스턴스 종료 + 탄력적 IP 릴리스**를 둘 다 해야 요금이 멈춥니다.
- Vercel(Hobby), DuckDNS, Let's Encrypt 인증서는 무료입니다.

---

## 기록: 서버를 바꿔 온 과정

| 날짜 | 바뀐 것 | 이유 |
|------|---------|------|
| 2026-10-14 | 첫 배포: `t3.micro` + Caddy + 백엔드 하나(8080) | 가장 간단한 구성 |
| 2026-10-22 | DB를 싱가포르 → 시드니 Neon 프로젝트로 이전 | 응답 2.4초, 1초 약 6건이 한계 (K12) |
| 2026-10-22 | `t3.micro` → `t3.small` | 무중단 배포 때 백엔드 두 개(합계 약 1.1GB)가 1GB에 들어가지 않음 |
| 2026-10-22 | Caddy → Nginx + Certbot, 백엔드를 두 칸으로 (블루-그린) | 배포마다 24~32초 끊김 → 0 (#92) |
| 2026-10-22 | 백엔드 실행 사용자 `ubuntu` → `triplan`(sudo 없음), 배포 전용 키 · 최소 권한 AWS 키 · GitHub Actions 자동 배포 | 자동 배포를 넣으면서 열쇠가 새어도 피해를 좁히려고 (#92). 사용자 전환도 블루-그린으로 해서 실패 0건. OIDC는 무료 요금제라 등록 불가 |

**배포 중 끊김 측정** (1초 10건, 같은 방식으로 3번씩)

| 방식 | 결과 |
|------|------|
| 끄고 켜기 (Caddy, 백엔드 하나) | 배포마다 백엔드가 24~32초 꺼짐 (502 242~319건) |
| 블루-그린 + Nginx 설정 바꿔 reload | 2건씩 실패 (0.48%), 끊김 0.3초. 502는 0. reload 때 keep-alive 연결이 닫힘 |
| 블루-그린 + reload 없음 + `max_fails=1` | 0건 / 3건 / 31건 (3.4초). `no live upstreams` |
| **블루-그린 + reload 없음 + `max_fails=0` (지금)** | **0건 / 0건 / 0건** |
| 망가진 버전 배포 → rollback | 0건 (전환하지 않고 옛 버전 유지, 되돌리기도 무중단) |

**Caddy에서 Nginx로 옮긴 순서** (사이트 끊김 0.4초)

1. Nginx · Certbot 설치. 80번 포트를 Caddy가 쓰고 있어서, 설치 중 서비스 자동 시작을 잠깐 막았다 (`/usr/sbin/policy-rc.d`). 이때 Certbot 갱신 타이머도 시작되지 못해서 나중에 따로 켰다
2. `blue-green.sh init`: 옛 백엔드(8080)를 켜 둔 채 8081 칸을 하나 더 실행 (15초, 메모리 1,101/1,905MB)
3. Caddy가 인증서 확인 경로(`/.well-known/acme-challenge/`)를 `/var/www/html`에서 꺼내 주도록 설정 → Caddy가 서비스하는 중에 Certbot으로 인증서 발급 (끊김 없음)
4. Nginx 본 설정을 넣고 검사 → `systemctl stop caddy && systemctl start nginx` 한 줄로 전환 (측정: 실패 1건, 0.4초)
5. 옛 백엔드(`triplan.service`)와 Caddy를 끄고 재부팅 때 켜지지 않게 함, Nginx는 켜지게 함
