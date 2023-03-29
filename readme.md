

# 데이터베이스 마이그레이션 진행 정리
가상화폐 로그성 데이터 수집 프로젝트의 데이터 베이스 migration 에 대해 기록합니다.

로그성 데이터를 RDMS (postgreSQL) 에 수집하기 때문에 생기는 문제점과  Elasticsearch 로의 마이그레이션에 대해 기록합니다. 

## 목차
1. [문제상황](#1._문제상황)
2. [DB 정하기: Elasticsearch](#2._DB_정하기:_Elasticsearch)
3. [logstash](#3.logstash)
4. [작업순서](#4._작업순서)


---

### 1. 문제상황

###### 문제 1: 일반적이지 않은 JPA 코드 생성.

같은 칼럼을 갖는 여러 테이블에 접근하는 코드를 작성하는 과정에서 JPA 용례와 반대되는 코드를 사용하게 됩니다.

1. 너무 많은 entity, dto, repository 클래스 
  

  <img width="466" alt="스크린샷 2023-03-27 오후 1 29 01" src="https://user-images.githubusercontent.com/104286091/227841070-14618c4f-f55f-480a-baf1-cf346db7a132.png">

각 코인의 종류별로 별도의 엔티티, dao 클래스가 필요하기 때문에 클래스 선언이 너무 많아집니다. 참을 수가 없습니다.


2. repository class 를 관리하는 클래스 구현 필요 .


```java
// 코드를 인자로 dao 클래스를 리턴하는 util 클래스 . 현재는 이렇게 작성했으나 사용성이 좋지 못함.
BithumbCandleRepository repository =
      bithumbCandleRepositoryUtils.getRepositoryFromCode(c.getCode());
  BithumbCandle saved = repository.save(c);
  
  // https://github.com/dkGithup2022/cdr_common_persistence
```

런타임중에 dao 클래스를 결정하는 클래스가 필요합니다. JPA를 쓰는 이점이 없다고 생각됩니다 .


###### 문제 2: 로그성 데이터 관리에 대한 데이터베이스의 기능부족.

postgresql 에서는 조회 가능한 집계 데이터를 생성하려면 별도의 크론을 등록해서 생성하는 수 밖에 없습니다.
Elasticsearch 에서는 다음과 같은 기능을 api 로 제공합니다.


- 집계함수를 es rest api 에서 제공 

<img width="513" alt="스크린샷 2023-03-27 오후 2 20 07" src="https://user-images.githubusercontent.com/104286091/227847528-9e01686d-bc4f-4c4d-9c38-e3e25fe406b2.png">

- 데이터 라이프 사이클 관리 (hot, warm, cold)

현재는 데이터를 6개월 밖에 수집하지 않아 필요는 없는 기능입니다. 
하지만 연단위 데이터가 쌓이게 된다면 오래된 데이터와 주로 조회되는 데이터의 구분이 필요할 것으로 예상됩니다. 


---
### 2. DB 정하기: Elasticsearch
#### ES 선정 이유
###### 어플리케이션 코드의 단순화
Migration 과정에서 어플리케이션 코드를 바꿈으로써 코드를 더 간결하게 작성할 수 있을 것이라 예상됩니다. 



<img width="752" alt="스크린샷 2023-03-29 오후 3 51 50" src="https://user-images.githubusercontent.com/104286091/228450139-f6d9a7fb-9cbf-431f-851a-c6330f0e0182.png">

런타임 중 연산이 수행될 인덱스를 인자로 받음 코드 구조를 간결하게 유지가 가능합니다 .



###### 집계 데이터 호출 함수를 DB 에서 제공함.
위의 Percentage 함수를 응용해 시각화 서비스를 제공하는 함수 제작에 부담을 줄일 수 있을 것이라 생각됩니다.


#### 아쉬운 점

온프레미스의 자원이 넉넉하지 않고 VM 을 하나밖에 쓰지 못합니다. 따라서 아래와 같은 불안요소가 생깁니다. 

###### 성능을 위한 결정이 아님
분산과 최적화의 이점을 살릴 수 없습니다. 노드가 하나라서 ES 최적화를 위한 기법을 사용하기 어렵습니다.
- 읽기를 위한 replica shard 갯수 조정 불가
- shard sizing 에 따른 데이터 노드 추가 불가

실제로 read, write QPS 가 늘어날지 줄어들지는 BMT 를 돌려봐야 알 수 있습니다.

###### 스토리지 용량이 얼마나 들지에 대한 예측이 안됨.

현재 운영을 스토리지가 빠듯하게 운영하고 있습니다.

RDMS 로 이동 후, 기존의 postgreSQL 에서 운영하는 것보다 용량이 배로 필요하다면 이 스토리지를 추가해야 합니다. 

######  index 생성에 대한 관리를 직접해야 함 
spring data jpa 의 @Entity, spring data elasticsearch 의 @Document 어노테이션을 적용할 순 없습니다.

인덱스를 관리하는 별도의 스크립트를 작성해야 합니다 . 



###### 2: 

---
### 3. logstash migration

---
### 4. 작업순서



