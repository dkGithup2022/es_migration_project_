



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

## Migration 도구


