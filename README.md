# 📌 BattleStorm – Minecraft Storm Plugin

**BattleStorm**은 Minecraft PvP/배틀로얄 모드를 위한 **자기장(스톰)** 시스템 플러그인입니다.  
월드 보더를 단계적으로 축소시키며 점점 좁아지는 전장에서 긴장감 있는 전투를 유도합니다.

---

## ✨ 주요 기능
- **단계별 스톰 축소** — 반경 / 축소시간 / 대기시간 / 피해 / 경고거리 / 색상 설정 가능
- **BossBar 진행 표시** — 다음 축소까지 남은 시간과 진행 상황 표시
- **월드 보더 기반** — Minecraft 기본 제공 WorldBorder 사용으로 성능과 호환성 우수
- **커맨드 제어** — 시작, 중지, 조회, 편집, 추가, 삭제 및 중심 좌표 변경
- **유연한 경기 시간 조절** — 각 단계의 `delay`와 `duration`으로 경기 길이 조절

---

## 📦 의존성
- **Java 17+**
- **Paper API** (또는 Spigot 계열)  
  예: `io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT`
- **Maven** – `maven-compiler-plugin` 등 기본 플러그인 사용  
💡 Paper 서버 사용을 권장합니다.

---

## ⚙️ 설정 예시 (`config.yml`)
```yaml
world: world
auto-start: false
reset-border-on-stop: true
start-radius: 400
phases:
  - radius: 400
    duration: 90
    damage: 0.3
    warn: 5.0
    delay: 330
    color: PURPLE
  - radius: 250
    duration: 90
    damage: 0.5
    warn: 5.0
    delay: 330
    color: BLUE
center:
  x: 0.0
  z: 0.0
```

---

## 🧭 설치 방법
1. 빌드된 JAR를 서버의 `plugins` 폴더에 넣습니다.
2. 서버를 실행해 `config.yml`이 생성되면 원하는 값으로 수정합니다.
3. `/storm start` 명령어로 게임을 시작합니다.

---

## 📜 명령어 설명
> 권한: `battlestorm.manage`

### `/storm start`
- 게임을 시작하고 1단계 스톰을 가동합니다.
- `start-radius`와 `center` 값으로 보더 초기화 후 BossBar 표시.

### `/storm stop`
- 진행 중인 스톰을 즉시 중지합니다.
- BossBar 제거, `reset-border-on-stop`이 `true`면 보더 초기화.

### `/storm show`
- 현재 설정을 표시합니다.
- 각 단계의 `radius`, `delay`, `duration`, `damage`, `warn`, `color` 확인 가능.

### `/storm edit start radius <값>`
- 시작 반경을 수정하고 설정 파일에 저장.

### `/storm edit center here`
- 현재 플레이어 위치를 중심 좌표로 저장.

### `/storm edit <단계번호> <속성> <값>`
- 해당 단계 속성을 수정합니다.  
  속성: `radius`, `delay`, `duration`, `damage`, `warn`, `color`  
  (세션 메모리에서만 변경, 서버 재시작 시 초기화될 수 있음)

### `/storm add <반경> <축소시간> <대기시간> <피해> <경고거리> <색상>`
- 새로운 단계를 마지막에 추가 (세션 메모리 기준)

### `/storm remove <단계번호>`
- 해당 번호의 단계를 삭제 (세션 메모리 기준)


<h2>🛠 BattleStorm 플러그인 빌드 방법</h2>

<p>BattleStorm은 Maven 기반의 Spigot/Bukkit 플러그인입니다. 아래 절차를 따라 빌드하고 서버에 적용할 수 있습니다.</p>

<h3>1. 필수 조건</h3>
<ul>
  <li>Java 17 이상</li>
  <li>Apache Maven 3.8 이상</li>
  <li>Spigot 또는 Paper 서버 (API는 Maven에서 자동 다운로드)</li>
  <li>Git (소스 코드 클론용)</li>
</ul>

<h3>2. 빌드 절차</h3>
<pre>
# 1. GitHub 저장소 클론
git clone https://github.com/username/BattleStorm.git
cd BattleStorm

# 2. Maven 빌드 실행
mvn clean package

# 3. 빌드 결과 확인
# target/BattleStorm-<버전>.jar 파일이 생성됩니다.
</pre>

<h3>3. 서버 적용</h3>
<ol>
  <li>생성된 <code>BattleStorm-<버전>.jar</code> 파일을 서버의 <code>plugins</code> 폴더에 복사합니다.</li>
  <li>서버를 재시작하면 플러그인이 자동으로 로드됩니다.</li>
  <li><code>config.yml</code> 파일을 수정하여 스톰 설정을 적용할 수 있습니다.</li>
</ol>

<p>이제 서버에서 <code>/storm</code> 명령어를 통해 BattleStorm을 즐기실 수 있습니다!</p>
