package com.example.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


/**
 * 임시로만 있는 거
 */
@Component
@RequiredArgsConstructor
public class CreateCandleIndex {
    //private final String UPBIT_CANDLE_PREFIX = "upbit_candle";
    private final ElasticsearchOperations elasticsearchOperations;

    public void createIndexWithMappingAndSetting(String index) throws IOException {
        Map<String, Object> mapping = readResourceAsMap("elastic/upbit/candle_mapping.json");
        Map<String, Object> setting = readResourceAsMap("elastic/upbit/candle_setting.json");

        Document mapDoc = Document.from(mapping);
        IndexCoordinates indexCoordinates = IndexCoordinates.of(index);
        elasticsearchOperations.indexOps(indexCoordinates).create(setting, mapDoc);
    }

    private Map<String, Object> readResourceAsMap(String resourcePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        String json = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        return new ObjectMapper().readValue(json, HashMap.class);
    }
}
