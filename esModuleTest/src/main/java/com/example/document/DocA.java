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
@Document( dynamic = Dynamic.FALSE, createIndex = true, indexName = "doc")
public class DocA {
    @Id
    private String id;

    private String name;
}
