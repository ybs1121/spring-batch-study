# 🌱 Spring Batch 개념 정리

Spring Batch의 핵심 개념과 데이터 처리 흐름을 정리한 문서입니다.  
배치 작업(Job)부터 각 컴포넌트(ItemReader, ItemProcessor, ItemWriter)까지 한눈에 이해할 수 있도록 설명합니다.

---

## 📑 목차
1. [Job (작업 단위)](#-job-작업-단위)
2. [Step (단계)](#-step-단계)
3. [JobLauncher](#-joblauncher)
4. [JobRepository](#-jobrepository)
5. [ExecutionContext](#-executioncontext)
6. [데이터 처리 컴포넌트](#-데이터-처리-컴포넌트)
7. [Batch Flow](#-typical-batch-flow)
8. [정리](#-정리)

---

## 🌐 Job (작업 단위)
- Spring Batch의 **최상위 개념**
- 하나의 **완전한 배치 프로세스**
- 여러 Step으로 구성되며 **모든 Step이 성공해야 Job도 성공**

---

## 🪜 Step (단계)
Job을 구성하는 **실행 단위**  
일반적으로 다음과 같은 단계로 구성:

1️⃣ **데이터 읽기 (Reader)**  
2️⃣ **데이터 가공 (Processor)**  
3️⃣ **데이터 저장 (Writer)**

---

## 🚀 JobLauncher
- **Batch Job 실행 시작점**
- Job 실행 시 **필요한 파라미터**를 외부에서 전달 가능

---

## 📂 JobRepository
- 배치 실행과 관련된 모든 **메타데이터 저장소**
- 주요 저장 정보:
    - Job/Step 실행 이력
    - 실행 상태 (성공, 실패, 중단 등)
    - 결과 데이터/처리 건수 등

---

## 🗝️ ExecutionContext
- `Key-Value` 구조로 Job/Step의 상태 정보를 저장하는 객체
- 주요 기능:
    - **Job ↔ Step 간 데이터 공유**
    - **배치 재시작 시 이전 상태 복원**

---

## ⚙️ 데이터 처리 컴포넌트
Spring Batch 데이터 처리의 핵심은 아래 3가지 컴포넌트입니다.

### 📖 ItemReader (읽기)
- 데이터 소스로부터 데이터를 읽음
- 예: DB 조회, CSV/JSON 파일 읽기, 메시지 큐 리스닝

### 🔄 ItemProcessor (가공)
- Reader → Writer 사이에서 데이터 변환/검증/필터링 수행
- 선택적으로 사용 가능

### 📝 ItemWriter (쓰기)
- 가공된 데이터를 최종 저장소로 기록
- 예: DB 저장, 파일 생성, 외부 API 전송

---

## Step 유형: 태스크릿 vs 청크 지향 처리

### 1. 태스크릿(Tasklet) 지향 처리
- 단순한 작업을 처리할 때 사용 (예: 알림 전송, 로그 파일 삭제)
- 하나의 함수가 실행되어 작업 수행
- 작업 완료 또는 반복 여부를 `RepeatStatus`로 반환
    - `RepeatStatus.FINISHED` : 작업 종료, 다음 Step으로 이동
    - `RepeatStatus.CONTINUABLE`: 작업 계속 실행(반복)
- 큰 작업을 작은 트랜잭션 단위로 나누어 안정적인 처리 가능
- DB 트랜잭션이 불필요하면 `ResourcelessTransactionManager` 사용 가능

### 2. 청크(Chunk) 지향 처리
- 대용량 데이터를 작은 단위(청크)로 나누어 처리
- 처리 순서: 데이터 읽기(ItemReader) → 데이터 가공(ItemProcessor) → 데이터 저장(ItemWriter)
- 청크 단위로 트랜잭션 관리, 오류 발생 시 미처리 청크부터 재시작 가능
- ItemReader는 한 번에 하나씩 데이터를 읽으며, 더 이상 데이터가 없으면 `null` 반환해 종료 신호
- ItemProcessor는 데이터를 가공하거나 필터링해 `null`을 반환하면 해당 데이터는 무시됨
- ItemWriter는 청크 단위 데이터를 한 번에 저장

### 3. 비교

| 항목         | 태스크릿 지향 처리                | 청크 지향 처리                       |
|------------|-------------------------|-----------------------------|
| 대상 작업     | 단순하고 짧은 작업               | 대용량 데이터 처리                  |
| 처리 방식     | 단일 함수 반복 호출, RepeatStatus 상태 사용 | 읽기-처리-쓰기 3단계 반복             |
| 트랜잭션 단위  | 전체 또는 반복 실행 단위            | 청크(데이터 묶음) 단위               |
| 복잡도       | 간단                            | 중간~복잡                            |
| 활용 예      | 알림, 로그 삭제, 단순 API 호출         | 파일, DB 데이터 읽기·변환·저장           |

---

**요약:**
- 단순 작업일 경우 태스크릿 방식을,
- 데이터량이 많고 안정적 처리가 필요할 땐 청크 방식을 선택하세요.



