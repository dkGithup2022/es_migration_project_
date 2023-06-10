



# PGSQL -> ES 마이그레이션 정리


## :memo: Table of Contents

## 목차
- [문제상황](#문제상황)
- [DB 선정: Elasticsearch](#DB_선정:_Elasticsearch)
- [아쉬운점](#아쉬운점)
- [Migration 도구](#Migration_도구)

---


# 문제상황

###  어플리케이션 코드 작성의 불편함

같은 칼럼을 갖는 여러 테이블에 접근하는 코드를 작성하는 과정에서 관리가 아려운 코드를 작성하게 됩니다.

</br>


##### 너무 많은 entity, dto, repository 클래스


  <img width="466" alt="스크린샷 2023-03-27 오후 1 29 01" src="https://user-images.githubusercontent.com/104286091/227841070-14618c4f-f55f-480a-baf1-cf346db7a132.png">

각 코인의 종류별로 별도의 엔티티, dao 클래스가 필요하기 때문에 클래스 선언이 너무 많아집니다. 참을 수가 없습니다.


</br>



### DB 의 로그성 데이터 처리에 대한 지원 부족

RDMS 는 DB 상에서 집계, 라이프 사이클에 대한 기능을 지원하지 않습니다. Elasticsearch 등의 DB 로의 마이그레이션을 통해 아래와 같은 집계함수에 대한 지원을 기대해볼 수 있습니다.

<img width="400" alt="스크린샷 2023-03-27 오후 2 20 07" src="https://user-images.githubusercontent.com/104286091/227847528-9e01686d-bc4f-4c4d-9c38-e3e25fe406b2.png">



---


# DB 선정: Elasticsearch

### ES 선정 이유

##### 어플리케이션 코드의 단순화
Migration 과정에서 어플리케이션 코드를 바꿈으로써 코드를 더 간결하게 작성할 수 있을 것이라 예상됩니다.

20 개의 entity, dao 클래스가 필요한 캔들데이터의 클래스를
2개로 줄인 예시입니다.

<img width="844" alt="스크린샷 2023-03-29 오후 5 15 48" src="https://user-images.githubusercontent.com/104286091/228470595-d03f970f-6da1-4607-a5ce-1ec2c11f0759.png">


런타임 중 연산이 수행될 인덱스를 인자로 받아서 코드 구조를 간결하게 유지가 가능합니다.

- 샘플 코드 : ./esModuleTest


</br>

#### 집계 데이터 호출 함수를 ES 에서 제공함.
추후 기능 제작아 시간을 절약할 수 있을 것이라 예상됩니다.


</br>

## 아쉬운 점


##### 최적화 불가능

운영할 노드가 한개라서 아래와 같이 최적화할 수 없습니다.

- 읽기를 위한 replica shard 갯수 조정 불가
- shard sizing 에 따른 데이터 노드 추가 불가

실제로 read, write QPS 가 늘어날지 줄어들지는 BMT 를 돌려봐야 알 수 있습니다.


</br>

##### 스토리지 용량이 얼마나 들지에 대한 예측이 안됨.

현재 운영을 스토리지가 빠듯하게 운영하고 있습니다.

RDMS 로 이동 후, 기존의 postgreSQL 에서 운영하는 것보다 용량이 배로 필요하다면 스토리지를 추가해야 합니다.


</br>

#####  index 생성에 대한 관리를 직접해야 함
spring data jpa 의 @Entity, spring data elasticsearch 의 @Document 어노테이션을 적용할 순 없습니다.

인덱스를 관리하는 별도의 스크립트를 작성해야 합니다. 샘플 코드에선 json 파일로 mapping 과 setting 에 대한 관리를 합니다.


---

## Migration 작업
 기존에 작업 해둔 RDB 라이브러리와 새로 제작할 ES 라이브러리의 조합으로 진행할 예정입니다. 

 아래 내용에 대한 코드 링크: https://github.com/dkGithup2022/cdr_migration_pjt


</br>



## 마이그레이션 앱


### 앱 목적
1. 현재 pgsql 에 저장된 bithumb, upbit 가격정보 데이터를 elasticsearch 로 이전합니다.
2. orderbook 의 경우 현재 모든 변경분을 저장하고 있습니다. 이걸 es 에 옮길때는 1분에 1개 단위로 샘플링을 하도록 합니다 .

</br>

---

### 진행 상황 & 이슈
cdr_elasticsearch 에 추가된 bulk operation 의 테스트 코드에 이슈가 있습니다.

es bulk 결과가 es 에 바로 조회가 되지 않으면서 테스트가 깨지는 부분이 있어서 임시로 막아놓은 상황입니다.
해당 테스트는 module pom 파일의 <BulkOperationTestOnModule> 가 false 이면 실행되지 않으니, 테스트 깨지면 해당 밸류가 false 로 되어 있는지 확인


</br>

###### 기록: 실행 시 인텔리제이에서 실행할 것 추천.

* 6월 추가 이슈 :  bulk가 아닌 parametized test 반복에서도 비슷한 현상이 발견됨. 해당 문제는 테스트를 다시 돌리거나 jar 로 만들지 말고 인텔리제이에서 그냥 실행 버튼 누르는 것으로도 해결됨.



* 현재로서 해당 문제를 해결하는 가장 쉬운 방법은 읽기 세그먼트에 정보가 반영될 때까지 긴  sleep을 주는 것이지만 이걸 하느니 그냥 mvn test 없이 실행하는것이 낫다고 봄.



</br>

-----


### Migration

해당 코드의 /migration/AbstractMigration 에 공통 작업 내용에 대한 슈퍼 클래스가 있습니다.


##### super class


```agsl


@Slf4j
public abstract class AbstarctMigration
	<
		PG_TYPE,
		ES_TYPE,
		PG_REPO,
		ES_REPO extends ElasticsearchRepository,
		COIN_CODE> {

	protected static final Integer MAX_RETRY = 3;

	protected final ES_REPO esRepository;
	protected final ModelMapper modelMapper;

	@Getter
	@Setter
	protected Long cursorTimstamp = Long.MAX_VALUE;

	@Getter
	@Setter
	protected Integer bulkSize = 1000;
	protected COIN_CODE coinCode;
	protected Long migratedCnt = 0L;

	@Getter
	protected PG_REPO rdmsRepo;

	protected AbstarctMigration(ES_REPO esRepository, ModelMapper modelMapper) {
		this.esRepository = esRepository;
		this.modelMapper = modelMapper;
	}

	public void readyMigrate(COIN_CODE coinCode, int bulkSize) {
		this.coinCode = coinCode;
		this.bulkSize = bulkSize;
		this.cursorTimstamp = Long.MAX_VALUE;
	}

	public void migrate(COIN_CODE coinCode) {

		setPGRepo(coinCode);
		log.info("\n\n******************************************************");
		log.info("BEGIN MIGRATION JOB ON {}", coinCode.toString());
		migratedCnt = 0L;
		Long iter = 0L;
		while (true) {
			// read from postgres
			List<PG_TYPE> rows = read();
			List<ES_TYPE> docs = rows.stream().map(e -> mapToDoc(e)).collect(Collectors.toList());

			//update data
			bulkFetch(docs);

			// update total cnt
			migratedCnt += docs.size();

			// update timestamp
			updateTimeStamp(rows);

			// check it is end;
			if (endOfTable(rows))
				break;


			iter++;
			if (iter % 1000 == 0)
				printCurrentTimeStamp();

		}

		log.info("\n\n******************************************************");
		log.info("MIGRATION JOB DONE ON TICK {}", coinCode.toString());
		log.info("TOTAL {} DATA FETCHED ", migratedCnt);
	}

	/**
	 * 기입 대상이 elasticsearch 라 중복제거를 안해도 됨.
	 * id, index 모두 ESUtils 에서 칼럼 값보고 생성해서 기입함..
	 *
	 * @param docs
	 */
	public void fetch(List<ES_TYPE> docs) {
		int retry = 0;
		while (retry < MAX_RETRY) {
			try {
				for (ES_TYPE doc : docs)
					upsertDoc(doc);

				return;
			} catch (Exception e) {
				retry++;
				try {
					Thread.sleep(2000);
				} catch (InterruptedException ex) {
					throw new RuntimeException(ex);
				}

				log.error(e.getMessage());
			}
		}

		log.error("current status : {}", this.toString());
		throw new RuntimeException("CAN NOT READ UPSERT , ");
	}

	public void bulkFetch(List<ES_TYPE> docs) {
		int retry = 0;
		while (retry < MAX_RETRY) {
			try {
				bulkInsert(docs);
				return;
			} catch (Exception e) {
				retry++;
				try {
					Thread.sleep(2000);
				} catch (InterruptedException ex) {
					throw new RuntimeException(ex);
				}

				log.error(e.getMessage());
			}
		}

		log.error("current status : {}", this.toString());
		throw new RuntimeException("CAN NOT READ UPSERT , ");
	}

	public List read() {
		int retry = 0;
		while (retry < MAX_RETRY) {
			try {
				return readNextPage(cursorTimstamp, PageRequest.of(0, bulkSize + 1));
			} catch (Exception e) {
				retry++;
				try {
					Thread.sleep(2000);
				} catch (InterruptedException ex) {
					throw new RuntimeException(ex);
				}
				log.error(e.getMessage());
			}
		}
		log.error("current status : " + this.toString());
		throw new RuntimeException("CAN NOT READ FROM DATABASE , ");
	}

	protected void printCurrentTimeStamp() {
		LocalDateTime time =
			LocalDateTime.ofInstant(Instant.ofEpochMilli(cursorTimstamp), ZoneId.systemDefault());
		log.info("long timestamp : {}", cursorTimstamp);
		log.info("of date : {}", time);
	}

	protected abstract void updateTimeStamp(List<PG_TYPE> rows);

	protected abstract void bulkInsert(List<ES_TYPE> docs) throws JsonProcessingException;

	public boolean endOfTable(List list) {
		return list == null || list.size() == 0 || list.size() < this.bulkSize + 1;
	}

	public abstract ES_TYPE mapToDoc(PG_TYPE row);

	public abstract void upsertDoc(ES_TYPE doc);

	public abstract List<PG_TYPE> readNextPage(Long cursorTimstamp, Pageable pageable);

	public abstract void setPGRepo(COIN_CODE coinCode);

}
```



</br>

##### migration
아래는 모든 타입에 적용 될 migration 작업에 대한 코드 입니다.

1. postgresql dao 구현체 설정
2. timestamp 기준으로 역순으로 읽기 시작함 .
3. elasticsearch 로 bulk operation
4. 다음 read 를 위한 timestamp 업데이트
5. (2) 결과의 갯수 비교를 통해 더 읽을 데이터 있는지 확인
6. 2 ~ 5 를 2가 false 일 때 까지 반복 .

</br>

```
public void migrate(COIN_CODE coinCode) {

		setPGRepo(coinCode); // 1. postgresql dao 구현체 설정 
		log.info("\n\n******************************************************");
		log.info("BEGIN MIGRATION JOB ON {}", coinCode.toString());
		migratedCnt = 0L;
		Long iter = 0L;
		while (true) {
			// read from postgres
			List<PG_TYPE> rows = read(); // 2. timestamp 기준으로 역순으로 읽기 시작함 .
			List<ES_TYPE> docs = rows.stream().map(e -> mapToDoc(e)).collect(Collectors.toList());

			//update data
			bulkFetch(docs); // 3. elasticsearch 로 bulk operation 

			// update total cnt
			migratedCnt += docs.size();

			// update timestamp
			updateTimeStamp(rows); // 4.다음 read 를 위한 timestamp 업데이트

			// check it is end;
			if (endOfTable(rows)) // 5. (2) 결과의 갯수 비교를 통해 더 읽을 데이터 있는지 확인
				break;


			iter++;
			if (iter % 1000 == 0)
				printCurrentTimeStamp();

		}

		log.info("\n\n******************************************************");
		log.info("MIGRATION JOB DONE ON TICK {}", coinCode.toString());
		log.info("TOTAL {} DATA FETCHED ", migratedCnt);
	}

```


</br>

##### 읽기 연산

읽기, 쓰기 연산은 exception 에 대해 3번까지 시도하고 실패시 로그로 남기는 연산이 공통적으로 적용됩니다.


- pgsql read
```agsl
public List read() {
		int retry = 0;
		while (retry < MAX_RETRY) {
			try {
				return readNextPage(cursorTimstamp, PageRequest.of(0, bulkSize + 1));
			} catch (Exception e) {
				retry++;
				try {
					Thread.sleep(2000);
				} catch (InterruptedException ex) {
					throw new RuntimeException(ex);
				}
				log.error(e.getMessage());
			}
		}
		log.error("current status : " + this.toString());
		throw new RuntimeException("CAN NOT READ FROM DATABASE , ");
	}

```


</br>

- elasticsearch bulk
```agsl

	public void bulkFetch(List<ES_TYPE> docs) {
		int retry = 0;
		while (retry < MAX_RETRY) {
			try {
				bulkInsert(docs);
				return;
			} catch (Exception e) {
				retry++;
				try {
					Thread.sleep(2000);
				} catch (InterruptedException ex) {
					throw new RuntimeException(ex);
				}

				log.error(e.getMessage());
			}
		}

		log.error("current status : {}", this.toString());
		throw new RuntimeException("CAN NOT READ UPSERT , ");
	}

```


</br>

##### 맴버 변수

- ES_REPO esRepository;
- PG_REPO rdmsRepo;

각 구현체 클래스에 사용될 es dao, pgsql dao 에 대한 타입을 제너릭으로 선언합니다.


- PG_TYPE
- ES_TYPE

위의 dao 구현체에서 다룰 타입도 제너릭으로 받습니다.

가령 UbitTick에 대한 migration class 를 제작할 때,

- ES_REPO = UpbitTickRepository (elasticsearch dao)
- PG_REPO = UpbitTickRepository ( rdms dao)
- PG_TYPE = UpbitTick (rdms entity)
- ES_TYPE = UpbitTick (es document type)



- 구현체의 타입 정의 예시
-
```agsl
@Component
@Slf4j
public class BithumbTickMigration
	extends AbstarctMigration
	<
		BithumbTick,
		BithumbTickDoc,
		BithumbTickRepository,
		com.dk0124.cdr.es.dao.bithumb.BithumbTickRepository,
		BithumbCoinCode> {
	private final BithumbTickRepositoryUtils repoPicker;

	public BithumbTickMigration(com.dk0124.cdr.es.dao.bithumb.BithumbTickRepository esRepository,
		ModelMapper modelMapper, BithumbTickRepositoryUtils repoPicker) {
		super(esRepository, modelMapper);
		this.repoPicker = repoPicker;

	}

```




</br>





