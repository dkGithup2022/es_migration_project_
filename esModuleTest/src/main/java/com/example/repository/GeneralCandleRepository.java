package com.example.repository;

import com.example.document.MyDoc;
import com.example.documentDto.UpbitCandleDto;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.*;
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

    public UpbitCandleDto indexDocument(String index, UpbitCandleDto document) {
        IndexCoordinates indexCoordinates = IndexCoordinates.of(index);
        return  elasticsearchOperations.save(document, indexCoordinates);
    }

    public List<UpbitCandleDto> searchByIndex(String index, Pageable pageable) {
        IndexCoordinates indexCoordinates = IndexCoordinates.of(index);

        QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();

        Query searchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .withPageable(pageable)
                .build();

        List<UpbitCandleDto> docList = elasticsearchOperations.search(searchQuery, UpbitCandleDto.class, indexCoordinates)
                .get().map(SearchHit::getContent).collect(Collectors.toList());

        return docList;
    }
}



