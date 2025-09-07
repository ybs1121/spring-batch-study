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



## JobParameter 및 스코프 개념 정리

### 1. JobParameter란?
- 배치 작업에 전달되는 입력값으로, 배치 실행 시 어떤 조건이나 데이터를 다룰지 결정함
- 동일한 Job을 다양한 파라미터로 유연하게 실행 가능
- 정적인 프로퍼티 값과 달리 실행 중 동적 변경 가능
- 모든 JobParameter는 메타데이터 저장소에 기록되어, Job 인스턴스 식별 및 재시작 처리, 실행 이력 추적 기능 제공

### 2. 커맨드라인에서 JobParameter 전달 예시
./gradlew bootRun --args='--spring.batch.job.name=dataProcessingJob inputFilePath=/data/input/users.csv,java.lang.String'

### 3. JobParameter 기본 표기법
parameterName=parameterValue,parameterType,identificationFlag

- parameterName: 파라미터 키 (Job 내 접근용)
- parameterValue: 실제 값
- parameterType: 타입 (예: java.lang.String, java.lang.Integer), 생략 시 String으로 간주
- identificationFlag: Job 인스턴스 식별용 여부 (true/false), 기본값 true

### 4. JSON 기반 표기법 (Spring Batch 5 이상)
- 쉼표 등 구분자가 포함된 값 처리 문제 해결
- 예시:  
  infiltrationTargets='{"value": "판교_서버실,안산_데이터센터", "type": "java.lang.String"}'

### 5. JobParameter 프로그래밍 방식 생성 예시
JobParameters jobParameters = new JobParametersBuilder()
.addJobParameter("inputFilePath", "/data/input/users.csv", String.class)
.toJobParameters();

jobLauncher.run(dataProcessingJob, jobParameters);

### 6. Job과 Step의 Scope

#### JobScope와 StepScope 개념
- 두 스코프는 Job 실행 시점에 빈이 생성되어 실행 종료 시 소멸됨 (지연 생성)
- 런타임에 전달된 동적 JobParameter를 빈에 주입 가능
- 여러 Job/Step 동시 실행 시 각각 독립적인 빈 인스턴스 사용해 동시성 문제 방지

#### 예시 - @StepScope 활용 Tasklet
@Bean
@StepScope
public Tasklet systemInfiltrationTasklet(@Value("#{jobParameters['infiltrationTargets']}") String infiltrationTargets) {
// 매 Step 실행마다 새 인스턴스 생성, 파라미터 주입
}

#### 주의사항
- Step 빈에는 @JobScope, @StepScope 사용 권장하지 않음 (스코프 활성화 시점 문제로 오류 발생)
- 대신 Tasklet, Reader, Writer 등 컴포넌트에 스코프 선언하여 파라미터 주입
- 스코프 대상 클래스는 반드시 상속 가능해야 함 (프록시 생성 관련)

### 7. ExecutionContext란?
- Job/Step 실행 중 상태를 저장하는 key-value 저장소
- 배치 중단 시 상태 복원 및 Step 간 데이터 공유 가능
- JobExecutionContext와 StepExecutionContext는 범위가 다름
    - jobExecutionContext: Job 전체에서 접근 가능
    - stepExecutionContext: 해당 Step 내에서만 접근 가능

#### ExecutionContext 주입 예시
@Bean
@JobScope
public Tasklet taskletWithJobContext(@Value("#{jobExecutionContext['previousState']}") String prevState) { ... }

@Bean
@StepScope
public Tasklet taskletWithStepContext(@Value("#{stepExecutionContext['currentStatus']}") String currStatus) { ... }

#### 데이터 접근 제한과 공유 방법
- StepExecutionContext는 다른 Step에서 접근 불가 (독립성 보장)
- Step 간 데이터 공유 필요 시 JobExecutionContext 활용

---

### 요약
- JobParameter: 배치 실행 시 입력 데이터로서, 동적 파라미터를 지원하고 식별 및 재시작 관리를 돕는다.
- JobScope & StepScope: 실행 시점에 동적으로 빈을 생성해 파라미터 주입과 동시성 문제 해결에 유용하다.
- ExecutionContext: 배치 실행 중 상태를 저장하고 재시작 및 Step 간 데이터 공유에 활용되는 key-value 저장소이다.

---

이 내용을 통해 JobParameter와 배치 실행 시 스코프 및 상태 관리를 쉽고 안전하게 활용할 수 있습니다.

Spring Batch Listener 개념 정리

Spring Batch Listener는 배치 처리 과정에서 특정 이벤트가 발생할 때 이를 감지하고 원하는 작업을 수행하게 해주는 기능입니다.
Job 시작/끝, Step 실행 전/후, 청크 단위 및 아이템 단위 처리 전후 등 다양한 시점에 개입해서 로깅, 모니터링, 에러 처리 등 부가적인 작업을 할 수 있습니다.

1. JobExecutionListener
- Job이 시작될 때와 끝날 때 호출됩니다.
- 예를 들어, 작업 시작 전에 준비가 필요한 자원을 마련하거나, 작업 끝난 후 결과를 이메일로 보내는 등의 일을 할 수 있습니다.
- afterJob() 메서드는 작업 결과가 저장되기 전에 호출되어 작업 상태를 바꾸는 것도 가능합니다.

JobExecutionListener 예시 코드:

    public interface JobExecutionListener {
    default void beforeJob(JobExecution jobExecution) { }
    default void afterJob(JobExecution jobExecution) { }
    }

2. StepExecutionListener
- Step 실행 전과 후에 호출됩니다.
- Step이 언제 시작하고 끝났는지, 몇 개의 데이터를 처리했는지 기록할 때 사용합니다.

StepExecutionListener 예시 코드:

    public interface StepExecutionListener extends StepListener {
    default void beforeStep(StepExecution stepExecution) { }
    default ExitStatus afterStep(StepExecution stepExecution) { return null; }
    }

3. ChunkListener
- 청크 단위 작업(작은 데이터 묶음 처리)이 시작되기 전, 끝난 후, 또는 오류가 발생했을 때 호출됩니다.
- 청크 처리를 모니터링하거나 오류 발생 시 알림을 보낼 때 사용합니다.

ChunkListener 예시 코드:
public interface ChunkListener extends StepListener {
default void beforeChunk(ChunkContext context) { }
default void afterChunk(ChunkContext context) { }
default void afterChunkError(ChunkContext context) { }
}

4. ItemReadListener, ItemProcessListener, ItemWriteListener
- 각각 데이터 읽기, 처리, 쓰기 작업 전후 및 에러 시점에 호출됩니다.

ItemReadListener 예시 코드:

    public interface ItemReadListener<T> extends StepListener {
    default void beforeRead() { }
    default void afterRead(T item) { }
    default void onReadError(Exception ex) { }
    }

ItemProcessListener 예시 코드:

    public interface ItemProcessListener<T, S> extends StepListener {
    default void beforeProcess(T item) { }
    default void afterProcess(T item, S result) { }
    default void onProcessError(T item, Exception e) { }
    }

ItemWriteListener 예시 코드:

    public interface ItemWriteListener<S> extends StepListener {
    default void beforeWrite(Chunk<? extends S> items) { }
    default void afterWrite(Chunk<? extends S> items) { }
    default void onWriteError(Exception exception, Chunk<? extends S> items) { }
    }

- ItemReadListener.afterRead()는 데이터가 더 이상 없으면 호출되지 않습니다.
- ItemProcessListener.afterProcess()는 필터링을 위해 null을 반환해도 호출됩니다.
- ItemWriteListener.afterWrite()는 트랜잭션 커밋 전에 실행됩니다.

배치 리스너 활용 장점
- 단계별 실행 시간과 처리 데이터 수 등 상태를 정확히 기록할 수 있습니다.
- 작업 종료 상태를 확인하고 결과에 따라 자동 후속 작업을 할 수 있습니다.
- 작업 도중 데이터를 가공하거나 다음 단계에 전달할 정보를 준비할 수 있습니다.
- 오류 발생 시 관리자 알림 등 부가 작업을 별도로 분리해 처리할 수 있습니다.


왜 JobParameters가 아닌 ExecutionContext를 사용할까?

- JobParameters는 한 번 생성되면 변경할 수 없다.
- Spring Batch의 핵심 철학은 배치 작업이 재현 가능하고 일관성을 유지하는 것인데,
  이를 위해 JobParameters는 불변(immutable)하도록 설계되었다.
- 재현 가능성: 같은 JobParameters로 실행된 작업은 항상 똑같은 결과를 내야 한다.
  실행 중에 JobParameters가 바뀌면 그걸 보장할 수 없다.
- 추적 가능성: 실행 기록과 JobParameters는 메타데이터 저장소에 저장되는데,
  변경 가능하면 기록과 작업 결과가 달라질 수 있다.

따라서 실행 중 동적으로 바뀌어야 하는 데이터는 ExecutionContext로 관리한다.

잘못된 예 - ExecutionContext에 데이터를 하드코딩하지 말자
@Override
public void beforeJob(JobExecution jobExecution) {
jobExecution.getExecutionContext().put("targetDate", LocalDate.now()); // 위험하다
}

- 이유: 만약 어제 데이터를 다시 처리하고 싶을 때, 수정하지 않으면 불가능하다.
- 하드코딩된 방식은 배치의 유연성을 크게 떨어뜨린다.

정답 - 외부에서 필요한 값을 JobParameters로 받자
예시:
./gradlew bootRun --args='--spring.batch.job.name=systemInfiltrationJob -date=2024-10-13'

- 이렇게 하면 배치 작업이 유연해진다.
- 대부분의 데이터는 JobParameters를 통해 외부에서 받는 것이 가장 안전하고 좋다.
- ExecutionContext와 JobExecutionListener는 외부에서 파라미터를 받을 수 없을 때만 사용하자.

---

Step 간 데이터 공유 - ExecutionContextPromotionListener

- Step 하나하나가 가진 ExecutionContext 데이터를 Job 수준 ExecutionContext로 올려주는 역할
- 이를 ‘승격(Promotion)’이라고 하고, ExecutionContextPromotionListener가 이를 수행한다.
- afterStep() 메서드에서 승격 작업을 한다.

주의사항:
- Step은 독립적으로 설계하는 게 좋다.
- Step 간 데이터 의존성을 최소화해야 유지보수가 쉽고 복잡도가 덜해진다.

---

정리

- JobParameters는 불변이라서 실행 중 변경할 수 없으니, 재현성 및 추적성을 보장한다.
- 실행 중 동적이고 변경이 필요한 데이터는 ExecutionContext를 사용한다.
- 데이터 값을 하드코딩하지 말고, 외부에서 JobParameters로 전달받자.
- 여러 Step 간에 데이터를 공유할 때 ExecutionContextPromotionListener를 사용해 안전하게 처리하자.

이렇게 하면 배치 작업은 유연하면서도 견고하고 유지보수가 쉬워진다.

---
파일 기반 배치 처리 시작하기
---
Spring Batch에서 파일 입출력을 다룰 때 가장 기본이 되는 무기는 FlatFileItemReader와 FlatFileItemWriter입니다. 이들을 이해하면 파일 기반 배치 처리의 흐름을 쉽게 잡을 수 있습니다.
---
FlatFileItemReader
---
FlatFileItemReader는 플랫 파일(CSV, TSV 등)을 한 줄씩 읽어 도메인 객체로 변환하는 리더입니다.

동작 과정은 크게 두 단계로 나눌 수 있습니다.

파일에서 한 줄을 읽는다

읽은 문자열을 객체로 변환한다

이 변환 과정의 핵심은 LineMapper입니다.

LineMapper : 한 줄(String)을 객체로 변환
interface LineMapper<T> {
T mapLine(String line, int lineNumber) throws Exception;
}

LineTokenizer : 한 줄을 여러 필드로 분리

FieldSetMapper : 분리된 필드를 객체 프로퍼티에 매핑

Spring Batch 기본 구현체인 DefaultLineMapper는
LineTokenizer → FieldSet → FieldSetMapper 순서로 처리하여 최종 객체를 생성합니다.

즉,
파일 한 줄 → 토큰화 → 객체 매핑 → 도메인 객체 반환

간단 예시 (CSV → 객체 변환)

CSV 데이터:
1001,Kim,30

처리 흐름:
LineTokenizer → [1001][Kim][30]
FieldSetMapper → Person(id, name, age)
최종 반환 → new Person(1001, "Kim", 30)

FlatFileItemWriter

FlatFileItemWriter는 객체를 받아 문자열로 변환하고 파일에 기록합니다. (Reader의 반대 역할)

핵심 컴포넌트는 LineAggregator입니다.

LineAggregator : 객체를 문자열 한 줄로 변환
interface LineAggregator<T> {
String aggregate(T item);
}

간단 예시 (객체 → CSV 변환)

객체: new Person(1001, "Kim", 30)
LineAggregator → "1001,Kim,30"
파일에 기록 → CSV 저장 완료

MultiResourceItemReader

여러 개의 파일을 순차적으로 읽어야 한다면 MultiResourceItemReader를 사용합니다.
첫 번째 파일을 다 읽으면 → 두 번째 파일로 넘어가고 → 세 번째 파일로 이어지는 방식으로 동작합니다.

구조 다이어그램
FlatFileItemReader 동작 구조

파일 한 줄
↓
LineTokenizer (문자열 → 토큰 분리)
↓
FieldSet (토큰 모음)
↓
FieldSetMapper (객체 매핑)
↓
도메인 객체 반환

FlatFileItemWriter 동작 구조

도메인 객체
↓
LineAggregator (객체 → 문자열 변환)
↓
파일 한 줄 기록

MultiResourceItemReader 동작 구조

File1 → 다 읽으면 → File2 → 다 읽으면 → File3 → ...

정리

FlatFileItemReader : 파일 → 객체 변환

FlatFileItemWriter : 객체 → 파일 기록

MultiResourceItemReader : 여러 파일을 순차적으로 처리

핵심 개념과 예시만 이해해도 Spring Batch의 파일 기반 배치 처리를 쉽게 다룰 수 있습니다.

---
FlatFileItemWriter는 데이터를 플랫 파일 형식으로 쓰는 작업을 담당하는 컴포넌트다. 파일 포맷을 맞추고, 데이터를 한 줄씩 작성하며, 내부적으로 버퍼링을 통해 최적화하는 등의 저수준 작업을 직접 구현하지 않아도 된다.  
작성 과정은 크게 두 단계로 나뉜다.

--------------------------------------------------------
필드 추출과 라인 결합

1단계: FieldExtractor (필드 추출)
- FieldExtractor 인터페이스는 도메인 객체에서 필요한 필드를 추출하는 역할을 한다.
- 예를 들어, DeathNote 객체에서 이름과 사인을 배열 형태로 뽑아내는 식이다.
- BeanWrapperFieldExtractor는 일반적인 Java Bean 객체에서 getter 메서드를 통해 필드를 추출하고, RecordFieldExtractor는 Java Record에서 필드를 추출한다.
- 인터페이스 구조는 다음과 같다.
    - extract(T item): 객체에서 배열 형태로 필드 값을 반환한다.

2단계: LineAggregator (문자열 결합)
- LineAggregator 인터페이스는 FieldExtractor가 추출한 배열 데이터를 하나의 문자열로 결합하는 기능을 한다.
- CSV, 탭 구분자 등 구분자 기반 형식이나 고정 길이 형식 등 원하는 포맷으로 문자열을 만들 수 있다.
- 대표적인 구현체로 DelimitedLineAggregator (구분자 기반)와 FormatterLineAggregator (포맷 기반), PassThroughLineAggregator (입력 객체를 그대로 문자열로 출력) 등이 있다.
- 인터페이스 구조는 다음과 같다.
    - aggregate(T item): 객체 데이터를 받아 문자열로 반환한다.

--------------------------------------------------------
FlatFileItemWriter 동작 방식
- 객체를 FieldExtractor에 넘겨 필드 배열을 추출한다.
- 그 배열을 LineAggregator에 넘겨 한 줄의 문자열로 결합한다.
- 결합된 문자열을 파일에 쓴다.

--------------------------------------------------------
코드 예시
1) FieldExtractor 인터페이스 및 구현 예시 (DeathNote 객체용)
- 인터페이스:  
  public interface FieldExtractor<T> {  
  Object[] extract(T item);  
  }
- 구현체 예:  
  public class DeathNoteFieldExtractor implements FieldExtractor<DeathNote> {  
  @Override  
  public Object[] extract(DeathNote deathNote) {  
  return new Object[]{deathNote.getName(), deathNote.getCauseOfDeath()};  
  }  
  }

2) LineAggregator 인터페이스 예시
- 인터페이스:  
  public interface LineAggregator<T> {  
  String aggregate(T item);  
  }

3) FlatFileItemWriter 설정 간단 예시
- Java 코드 예:  
  FlatFileItemWriterBuilder<User> builder = new FlatFileItemWriterBuilder<User>()  
  .name("userWriter")  
  .resource(new FileSystemResource("output.csv"))  
  .lineAggregator(new DelimitedLineAggregator<>()) // 예: 콤마(,) 구분자 사용  
  .build();

--------------------------------------------------------
요약
- FlatFileItemWriter는 도메인 객체 데이터를 플랫 파일로 쉽게 쓰게 해주는 Spring Batch 컴포넌트다.
- 데이터 추출은 FieldExtractor가, 문자열 결합은 LineAggregator가 각각 담당한다.
- 다양한 구현체를 통해 원하는 파일 포맷(CSV, 고정 길이 등) 지원 가능하다.  

