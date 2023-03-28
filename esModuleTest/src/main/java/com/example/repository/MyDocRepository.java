package com.example.repository;


import com.example.document.MyDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyDocRepository extends ElasticsearchRepository<MyDoc, String> {

}
