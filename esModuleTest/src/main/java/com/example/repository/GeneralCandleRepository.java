package com.example.repository;


import com.example.documentDto.UpbitCandleDto;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GeneralCandleRepository {

    private final ElasticsearchOperations elasticsearchOperations;

    public UpbitCandleDto index(String indexName, UpbitCandleDto document) {
        IndexCoordinates indexCoordinates = IndexCoordinates.of(indexName);
        return elasticsearchOperations.save(document, indexCoordinates);
    }

    public List<UpbitCandleDto> search(String indexName, Pageable pageable) {
        IndexCoordinates indexCoordinates = IndexCoordinates.of(indexName);

        QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
        Query searchQuery = buildSearchQuery(queryBuilder, pageable);

        return elasticsearchOperations.search(searchQuery, UpbitCandleDto.class, indexCoordinates)
                .get().map(SearchHit::getContent).collect(Collectors.toList());
    }

    private Query buildSearchQuery(QueryBuilder queryBuilder, Pageable pageable) {
        return new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .withPageable(pageable)
                .build();
    }

        /*
    public UpbitCandleDto index(String indexName,String id ,UpbitCandleDto document) {
        IndexRequest indexRequest = new IndexRequest(indexName);
        indexRequest.id(id);
        indexRequest.source(document, XContentType.JSON);
        IndexCoordinates indexCoordinates = IndexCoordinates.of(indexName);
        return elasticsearchOperations.save(document, indexCoordinates);
    }
     */
}



