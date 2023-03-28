package com.example;

import com.dk0124.cdr.constants.coinCode.CoinCode;
import com.dk0124.cdr.constants.coinCode.UpbitCoinCode.UpbitCoinCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

//@Component
@RequiredArgsConstructor
public class InitialJob implements ApplicationRunner {

    private final ElasticsearchOperations elasticsearchOperations;
    private final String UpbitCandlePrefix = "upbit_candle";

    @Override
    public void run(ApplicationArguments args) throws Exception {
        for(CoinCode code : UpbitCoinCode.values()){
            String[] splitted = code.toString().toLowerCase(Locale.ROOT).split("-");
            String index = UpbitCandlePrefix + "_" + String.join("_",splitted);
            createUpbitCandleIndex(index);
        }
    }

    private final boolean createUpbitCandleIndex(String index) throws IOException {
        ClassPathResource mappingResource = new ClassPathResource("elastic/upbit/candle_mapping.json");
        String mappingJson = StreamUtils.copyToString(mappingResource.getInputStream(), StandardCharsets.UTF_8);
        Map<String, Object> mapping =
                new ObjectMapper().readValue(mappingJson, HashMap.class);

        ClassPathResource settingResource = new ClassPathResource("elastic/upbit/candle_setting.json");
        String settingJson = StreamUtils.copyToString(settingResource.getInputStream(), StandardCharsets.UTF_8);
        Map<String, Object> setting =
                new ObjectMapper().readValue(settingJson, HashMap.class);

        Document mapDoc = Document.from(mapping);

        return elasticsearchOperations.indexOps(IndexCoordinates.of(index, "from_json3")).create(setting,mapDoc);
    }

}
