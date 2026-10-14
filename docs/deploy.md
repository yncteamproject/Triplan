# 배포 가이드

Triplan을 인터넷 주소로 열 수 있게 올리는 방법입니다. 처음 한 번 하는 설정과, 코드를 고친 뒤 다시 올리는 방법을 순서대로 적었습니다.

> [!WARNING]
> 이 저장소는 **공개**입니다. 서버 IP, 접속 키(`.pem`), 비밀번호, API 키를 이 문서나 이슈 · PR · 커밋에 적지 않습니다. 아래의 `{중괄호}`는 각자 실제 값으로 바꿔서 쓰고, 바꾼 값은 저장소에 올리지 않습니다.

## 구성

```
브라우저
 ├─ 화면   https://{프로젝트}.vercel.app        → Vercel (frontend 폴더를 빌드해서 제공)
 └─ API    https://{이름}.duckdns.org/api/...   → AWS EC2
                                                    ├─ Caddy: https 인증서, 요청을 백엔드로 전달
                                                    └─ 백엔드(jar): 127.0.0.1:8080, 서버 밖에서는 직접 못 들어옴
                                                         └─ DB: Neon (PostgreSQL)
```

| 부분 | 사용 | 고른 이유 |
|------|------|-----------|
| 화면 | Vercel | GitHub에 연결해 두면 `main`에 merge될 때마다 자동으로 다시 배포 |
| 백엔드 | AWS EC2 + 고정 IP(Elastic IP) | 오디세이는 **등록한 IP에서만** 동작해서 IP가 바뀌지 않는 서버가 필요 |
| 도메인 | DuckDNS (무료) | https 인증서는 IP가 아니라 도메인에 발급된다 |
| https | Caddy | 도메인만 적으면 인증서를 자동으로 받고 갱신 |
| DB | Neon | 지금 쓰는 것 그대로 |

화면(https)에서 API를 부르려면 API도 https여야 합니다. 브라우저가 https 화면에서 http 주소를 부르는 것을 막기 때문입니다. 그래서 백엔드에 도메인과 Caddy를 붙입니다.

## 코드에서 준비해 둔 것

| 파일 | 하는 일 |
|------|---------|
| `backend/src/main/resources/application-prod.yaml` | 서버 전용 설정. SQL 로그 끄기, H2 콘솔 끄기, 서버 안(127.0.0.1)에서만 8080 열기 |
| `backend/src/main/resources/application.yaml` | 실행 폴더(jar 옆)의 `application-secret.yaml`도 읽음. CORS 허용 주소를 설정값(`app.cors.allowed-origins`)으로 받음 |
| `backend/build.gradle` | 배포용 jar에 `application-secret.yaml`을 넣지 않음 |
| `frontend/vercel.json` | `/trips/3` 같은 주소에서 새로고침해도 404가 나지 않게 모든 주소를 `index.html`로 보냄 |
| `deploy/Caddyfile` | Caddy 설정 예시 |
| `deploy/triplan.service` | 백엔드를 서비스로 띄우는 설정 (재부팅 · 오류 때 자동 재시작) |
| `deploy/application-secret.example.yaml` | 서버에 둘 비밀 값 파일의 모양 |

각자 컴퓨터에서 실행하는 방법(`./gradlew bootRun`, `npm run dev`)은 바뀌지 않습니다.

---

## 처음 한 번: 서버 만들기

### 1. EC2 만들기 (AWS 콘솔)

1. 오른쪽 위 지역을 **서울(ap-northeast-2)**로 바꿉니다.
2. EC2 → 인스턴스 시작
   - 이름: `triplan`
   - OS: **Ubuntu Server 24.04 LTS**
   - 인스턴스 유형: "프리 티어 사용 가능" 표시가 있는 것. 메모리 2GB짜리(예: `t3.small`)가 있으면 그것, 없으면 1GB(`t3.micro`)를 고르고 아래 3번의 스왑을 꼭 만듭니다
   - 키 페어: 새로 만들기 → `.pem` 파일을 받아서 **저장소 폴더 밖**(예: `C:\Users\{나}\.ssh\triplan.pem`)에 둡니다. 다시 받을 수 없습니다
   - 네트워크 설정(보안 그룹):

     | 종류 | 포트 | 허용할 곳 | 이유 |
     |------|------|-----------|------|
     | SSH | 22 | **내 IP** | 서버 접속. 모두에게 열지 않는다 |
     | HTTP | 80 | 모든 곳 | 인증서 발급, https로 넘기기 |
     | HTTPS | 443 | 모든 곳 | API |

     8080은 열지 않습니다. 백엔드는 Caddy를 통해서만 들어옵니다.
   - 저장소: 기본값(8GB 이상)
3. EC2 → 탄력적 IP → 탄력적 IP 주소 할당 → 만든 IP를 선택하고 **연결** → 인스턴스 `triplan` 선택

이제 서버 IP가 바뀌지 않습니다. 이 IP를 아래에서 `{IP}`라고 씁니다.

> [!NOTE]
> 집 · 학교처럼 접속하는 곳이 바뀌면 "내 IP"도 바뀌어서 SSH가 안 될 수 있습니다. 그때는 보안 그룹의 22번 규칙을 지금 IP로 고칩니다.

### 2. 서버에 접속하기

Git Bash에서:

```bash
ssh -i ~/.ssh/triplan.pem ubuntu@{IP}
```

`UNPROTECTED PRIVATE KEY FILE` 오류가 나면 PowerShell에서 키 파일을 나만 읽을 수 있게 바꿉니다:

```powershell
icacls $env:USERPROFILE\.ssh\triplan.pem /inheritance:r /grant:r "$($env:USERNAME):R"
```

### 3. 서버 준비 (서버 안에서)

```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y openjdk-21-jre-headless caddy
java -version
```

메모리가 작아서 백엔드가 멈추지 않게 스왑(디스크를 메모리처럼 쓰는 공간) 2GB를 만듭니다:

```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
free -h
```

백엔드를 둘 폴더를 만듭니다:

```bash
sudo mkdir -p /opt/triplan
sudo chown ubuntu:ubuntu /opt/triplan
```

### 4. 도메인 연결 (DuckDNS)

1. [duckdns.org](https://www.duckdns.org)에 로그인
2. 원하는 이름(`{이름}`)을 적고 add domain
3. current ip 칸에 `{IP}`를 넣고 update ip

내 컴퓨터에서 연결됐는지 봅니다 (`{IP}`가 나오면 성공):

```bash
nslookup {이름}.duckdns.org
```

### 5. 백엔드 올리기

**내 컴퓨터**(저장소 폴더)에서 jar를 만들고 서버로 보냅니다:

```bash
cd backend
./gradlew bootJar
scp -i ~/.ssh/triplan.pem build/libs/triplan-0.0.1-SNAPSHOT.jar ubuntu@{IP}:/opt/triplan/triplan.jar
scp -i ~/.ssh/triplan.pem ../deploy/application-secret.example.yaml ubuntu@{IP}:/opt/triplan/application-secret.yaml
scp -i ~/.ssh/triplan.pem ../deploy/triplan.service ubuntu@{IP}:/tmp/triplan.service
```

**서버 안**에서 비밀 값을 채웁니다:

```bash
nano /opt/triplan/application-secret.yaml
chmod 600 /opt/triplan/application-secret.yaml
```

- DB · 오디세이 키: 각자 컴퓨터의 `application-secret.yaml`과 같은 값 (DB는 아래 "DB" 참고)
- JWT 키: 새로 만듭니다 → `openssl rand -base64 48`의 결과
- `allowed-origins`: Vercel 주소를 아직 모르면 일단 두고, 7번이 끝난 뒤 고칩니다

서비스로 등록하고 켭니다:

```bash
sudo mv /tmp/triplan.service /etc/systemd/system/triplan.service
sudo systemctl daemon-reload
sudo systemctl enable --now triplan
sudo systemctl status triplan
```

20초쯤 뒤 서버 안에서 확인합니다 (`200`이 나오면 성공):

```bash
curl -s -o /dev/null -w "%{http_code}\n" http://127.0.0.1:8080/api/share-pages
```

### 6. https 열기 (Caddy, 서버 안에서)

`deploy/Caddyfile`의 내용을 `{이름}`만 바꿔서 넣습니다:

```bash
sudo nano /etc/caddy/Caddyfile
sudo systemctl reload caddy
```

1분쯤 뒤 브라우저에서 `https://{이름}.duckdns.org/api/share-pages`를 열어 글 목록(JSON)이 보이면 성공입니다. 주소창에 자물쇠가 보여야 합니다.

### 7. 화면 올리기 (Vercel)

1. [vercel.com](https://vercel.com)에 GitHub 계정으로 로그인
2. Add New → Project → `yncteamproject/Triplan` 가져오기 (공개 저장소라 무료 요금제로 연결됩니다)
3. 설정
   - Root Directory: **`frontend`**
   - Framework: Vite (자동으로 잡힘). 빌드 명령 · 결과 폴더는 기본값
   - Environment Variables:

     | 이름 | 값 |
     |------|----|
     | `VITE_API_BASE_URL` | `https://{이름}.duckdns.org` (끝에 `/` 없이) |
     | `VITE_KAKAO_MAP_KEY` | 카카오 JavaScript 키 |
4. Deploy → 끝나면 나오는 주소가 `https://{프로젝트}.vercel.app`

`VITE_`로 시작하는 값은 **빌드할 때** 화면 코드에 들어갑니다. Vercel에서 값을 바꿨으면 Deployments → 최근 배포 → Redeploy를 해야 반영됩니다.

### 8. 서로 등록하기

| 어디에 | 무엇을 | 안 하면 |
|--------|--------|---------|
| 서버 `application-secret.yaml`의 `app.cors.allowed-origins` | `https://{프로젝트}.vercel.app` → `sudo systemctl restart triplan` | 화면에서 API 호출이 CORS 오류로 막힘 |
| 카카오 Developers → 앱 → 플랫폼 → Web | `https://{프로젝트}.vercel.app` 추가 (`http://localhost:5173`은 그대로 둠) | 지도 · 장소 검색이 안 됨 |
| 오디세이(ODsay LAB) → Server 플랫폼 | `{IP}` 등록 (기존 키에 추가하거나 배포용 키를 새로 발급) | 대중교통 경로가 502 |

### 9. 확인

`https://{프로젝트}.vercel.app`에서:

- [ ] 메인 · 게시판 목록이 열린다 (로그인 없이)
- [ ] 회원가입 → 로그인 → 여행 만들기
- [ ] `/trips/{번호}`에서 새로고침해도 화면이 나온다
- [ ] 장소 검색으로 방문지 추가 → 지도에 마커
- [ ] 이동 구간의 "대중교통 경로"
- [ ] 게시판 글쓰기 · 댓글 · 내 여행으로 복사

---

## 코드를 고친 뒤 다시 올리기

**화면**: `main`에 merge되면 Vercel이 자동으로 다시 배포합니다. 따로 할 일이 없습니다.

**백엔드**: 내 컴퓨터에서 jar를 만들어 보내고, 서버에서 다시 켭니다.

```bash
cd backend
./gradlew bootJar
scp -i ~/.ssh/triplan.pem build/libs/triplan-0.0.1-SNAPSHOT.jar ubuntu@{IP}:/opt/triplan/triplan.jar
ssh -i ~/.ssh/triplan.pem ubuntu@{IP} "sudo systemctl restart triplan"
```

최신 `main`에서 jar를 만들어야 합니다. 다른 브랜치에서 만들면 merge 안 된 코드가 올라갑니다.

## 로그 보기 (서버 안에서)

```bash
sudo journalctl -u triplan -f
```

- `-f`: 새로 찍히는 로그를 계속 보여줌 (`Ctrl + C`로 나옴)
- 최근 것만: `sudo journalctl -u triplan -n 200`
- 대중교통이 502면 `오디세이 호출 실패`를 찾습니다 (원인별 설명은 [API 명세](api.md)의 오디세이 항목)

## 문제가 생기면

| 증상 | 확인할 것 |
|------|-----------|
| 화면에서 모든 요청이 실패, 브라우저 콘솔에 `CORS` | `allowed-origins`가 Vercel 주소와 **정확히** 같은지 (`https`, 끝의 `/` 없음), 고친 뒤 restart 했는지 |
| `https://{이름}.duckdns.org`가 안 열림 | 보안 그룹 80 · 443, DuckDNS의 IP, `sudo journalctl -u caddy -n 50` |
| Caddy가 502 | 백엔드가 꺼져 있음 → `sudo systemctl status triplan`, 로그 |
| 백엔드가 계속 재시작 | 로그에서 첫 오류. 대부분 `application-secret.yaml` 값 오류(DB 주소 · 들여쓰기) |
| 새로고침하면 404 | `frontend/vercel.json`이 있는지, Root Directory가 `frontend`인지 |
| 지도가 안 나옴 | 카카오 Web 플랫폼에 Vercel 주소, Vercel의 `VITE_KAKAO_MAP_KEY`, Redeploy 했는지 |
| 화면은 되는데 데이터가 이상함 | Vercel의 `VITE_API_BASE_URL`이 서버 주소인지 (localhost면 안 됨) |

## DB

배포 서버는 **각자 컴퓨터의 `bootRun`과 같은 Neon DB**를 씁니다. 서버의 `application-secret.yaml`에는 각자 쓰는 것과 같은 DB 접속 정보를 넣습니다.

같은 DB라서 지켜야 할 것:

- 개발하면서 만든 데이터가 배포된 화면에 그대로 보입니다. 테스트용 글 · 여행은 확인이 끝나면 지웁니다.
- 테이블은 `ddl-auto: update`로 바뀝니다. **merge 전 브랜치를 `bootRun`하면 그 브랜치의 테이블 변경(컬럼 추가 등)이 배포 DB에도 바로 들어갑니다.** 테이블을 바꾸는 작업은 먼저 H2(테스트 · 각자 설정)로 확인하고, PR에 적어서 서로 알립니다.
- 비밀번호가 같은 DB를 가리키므로, 서버 파일이든 각자 파일이든 유출되면 둘 다 위험합니다.

나중에 나누고 싶으면 Neon 콘솔에서 배포용 브랜치(예: `production`)를 만들고 서버의 접속 주소만 바꾸면 됩니다. 코드는 고치지 않습니다.

## 비용과 정리

- AWS 무료 범위는 가입 시기와 요금제에 따라 다릅니다. 가입할 때 받은 크레딧이 있으면 먼저 쓰이고, 유료 요금제면 크레딧을 다 쓴 뒤 카드로 결제됩니다. **결제 → 예산(Budgets)**에서 월별 예산 알림(예: 15달러)을 만들어 두면 예상보다 많이 쓸 때 메일로 알 수 있습니다.
- 서버 크기는 `t3.micro`(메모리 1GB) + 스왑 2GB로 시작합니다. 느리면 인스턴스를 중지하고 유형만 `t3.small`로 바꿔 다시 시작하면 됩니다 (고정 IP와 서버 안의 파일은 그대로).
- 고정 IP(공인 IPv4)도 요금 대상입니다. 발표가 끝나 더 쓰지 않으면 **인스턴스 종료 + 탄력적 IP 릴리스**를 둘 다 해야 요금이 멈춥니다.
- Vercel(Hobby), DuckDNS, Caddy 인증서는 무료입니다.
