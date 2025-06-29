# api-backend

## 프로젝트 개요

**api-backend**는 "Container-Monitoring-Limited-Http" 프로젝트에서 클라이언트(frontend)와 다른 백엔드 서버(metrics-backend)로부터 API 요청을 받아 처리하며, 데이터베이스와 관련된 모든 역할을 담당하는 Java Spring 기반 서버입니다.

이 프로젝트는 다음과 같은 주요 기능을 수행합니다:

- DB 초기 데이터 입력, API를 통한 데이터 저장 등 DB와 관련된 모든 작업 처리
- 머신(Host, Container) 정보 관리 및 임계치(Threshold) 초과 데이터 관리
- SSE(Server-Sent Events) 방식으로 임계치 초과 데이터를 실시간으로 클라이언트에 전달
- API 요청에 따라 날짜별 임계치 초과 데이터 조회, 임계치 설정/조회 등 다양한 기능 제공
- 다른 백엔드 서버의 API 호출(`/inventory/{machineId}/{type}` 등)에 대해 캐시를 활용한 고유 ID 치환 기능 제공
- 클라이언트와 다른 백엔드 서버 사이의 데이터 흐름을 중계하는 핵심 브릿지 역할 수행

---

## 환경설정 및 실행 방법

### 1. 필수 환경 파일(env.properties) 직접 생성

**중요:**  
이 프로젝트는 보안상 이유로 `.gitignore`에 의해 `env.properties` 파일이 포함되어 있지 않습니다.  
따라서, 프로젝트를 clone 또는 다운로드한 후, 아래와 같이 직접 환경 파일을 생성해야 합니다.

1. `src/main/resources/properties` 폴더를 생성합니다.
2. 해당 폴더 안에 `env.properties` 파일을 만듭니다.
3. 아래 내용을 복사해 직접 입력합니다.

    ```
    DATABASE_URL=jdbc:mysql://<엔드포인트>/<스키마 이름>
    DATABASE_USERNAME=<Username>
    DATABASE_PASSWORD=<Password>

    cors.allowed-origins=<주소1>,<주소2>,...
    ```

> ⚠️ 실제 운영 환경에서는 DB 정보와 비밀번호 등 민감한 정보는 안전하게 관리하세요.

### 2-1. 도커에서 실행 방법 (metrics-backend와 api-backend등 한꺼번에 compose하는 경우)

1. metrics-backend의 실행방법을 따라하세요.

### 2-2. 도커에서 실행 방법 (api-backend만 도커에 올리는 경우)

1. 환경 설정 파일(`env.properties`)이 준비되어 있는지 확인
2. 도커에 MySQL의 데이터베이스가 존재하는지 확인
   >(도커에 DB를 올리는 과정은 아래의 '데이터베이스 관련 안내'를 참고하시길 바랍니다.)
3. 네트워크 생성
   ```
   docker network create monitoring_network 
    ```
4. 도커에 올려 실행
   ```
   docker run -d --name api-backend --network monitoring_network -p 8004:8004 api-backend
    ```

### 2-3. 배포 및 실행 방법

1. 환경 설정 파일(`env.properties`)이 준비되어 있는지 확인

2. 프로젝트 빌드
    ```
    ./gradlew build
    ```
   또는
    ```
    ./gradlew bootJar
    ```

3. 생성된 jar 파일을 서버에 복사

4. jar 파일 실행(예시)
    ```
    java -jar <프로젝트.jar 파일 이름>.jar
    ```

5. (필요시) 포트 및 환경 변수 설정

> 자세한 환경 파일 및 DB 설정 방법은 위의 [1. 필수 환경 파일(env.properties) 직접 생성](#1.-필수-환경-파일(env.properties)-직접-생성) 섹션을 참고하세요.


---

## 주요 기술 스택 및 의존성

- Java 21
- Spring Boot 3.4.4
- Spring Data JPA & JDBC
- Spring Kafka
- MySQL
- Caffeine Cache
- Jackson (JavaTime 지원)
- Lombok
- JUnit

<details>
<summary><strong>build.gradle 주요 설정 보기</strong></summary>

</details>

---

## API 목록

### 1. 다른 백엔드 서버와 통신하는 API

| 메서드 | 엔드포인트 | 설명 |
|--------|------------|------|
| GET    | `/api/metrics/threshold-check` | 임계치 초과 여부를 확인 |
| POST   | `/api/violation-store`         | 임계치 초과(이상) 기록 저장 |
| GET    | `/api/inventory/{machineId}/{type}` | machineId와 type에 따른 고유 ID를 반환(캐시 활용) |

### 2. 클라이언트와 통신하는 API

| 메서드 | 엔드포인트 | 설명 |
|--------|------------|------|
| GET    | `/api/metrics/threshold-setting` | 임계치(Threshold) 정보 조회 |
| POST   | `/api/metrics/threshold-setting` | 임계치(Threshold) 정보 설정 |
| POST   | `/api/metrics/threshold-history` | 날짜별 임계치 초과 이력 조회 |
| GET    | `/api/metrics/threshold-alert`   | SSE 방식으로 임계치 초과 실시간 알림 전송 |

---

## 데이터베이스 관련 안내
- 개별적으로 api-backend를 도커에 올린다면 다음과 같은 절차를 따라하세요.
- MySQL 데이터베이스를 도커에 올리는 과정입니다.
- (주의) api-backend를 실행시키기 전 필요한 과정입니다.
1. 다음 명령어를 입력하여 볼륨을 생성해주세요.
    ```
    docker volume create mysql-db
    ```
2. 다음 명령어를 입력하여 도커에 DB를 올려주세요.
    ```
    docker run -d --name mysql-db --restart always -p 3307:3306 \ 
   -e MYSQL_ROOT_PASSWORD=[루트 비밀번호 임의 설정] \ 
   -e MYSQL_DATABASE=monitoring_db \ 
   -e MYSQL_USER=[유저 이름] \ 
   -e MYSQL_PASSWORD=[유저 비밀번호] \
   -e TZ=Asia/Seoul \
   -v [프로젝트 폴더 경로]/db/mysql/data:/var/lib/mysql \
   -v [프로젝트 폴더 경로]/db/mysql/config:/etc/mysql/conf.d \ 
   -v [프로젝트 폴더 경로]/db/mysql/init:/docker-entrypoint-initdb.d mysql:8.0
    ```
- 테이블 및 컬럼 구조는 프로젝트의 엔티티 클래스를 참고하세요.
- 프로젝트 실행 시, host/container 타입과 주요 메트릭, 임계치 기본 정보는 자동으로 입력됩니다.

---

## 프로젝트 주요 기능

- 머신(Host, Container) 정보 관리 및 캐싱
- 임계치(Threshold) 관리 및 실시간 알림(SSE)
- 날짜별 임계치 초과 데이터 조회, 임계치 설정/조회
- API 중계 및 데이터 브릿지 역할

---

## 주의사항

- 개발 및 테스트 환경에서는 반드시 `env.properties` 파일을 직접 생성 후 실행하세요.
- 민감 정보는 외부에 노출되지 않도록 주의하세요.

---

## 문의 및 기여

- 이 프로젝트에 대한 문의, 개선 제안, 버그 제보는 이슈 또는 PR로 남겨주세요.

