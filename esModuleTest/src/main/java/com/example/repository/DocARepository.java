package com.example.repository;


import com.example.document.DocA;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocARepository  extends ElasticsearchRepository<DocA, String> {
    List<DocA> findByName(String name);

}
