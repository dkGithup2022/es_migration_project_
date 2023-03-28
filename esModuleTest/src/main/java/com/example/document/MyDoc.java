package com.example.document;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Dynamic;

@Getter
@Setter
@Builder
@Document( dynamic = Dynamic.FALSE, createIndex = false, indexName = "my_doc_3")
public class MyDoc {
    @Id
    private String id;
    private String name;
}
