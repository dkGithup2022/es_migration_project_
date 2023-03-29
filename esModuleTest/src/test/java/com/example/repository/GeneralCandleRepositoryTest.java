package com.example.repository;

import com.dk0124.cdr.constants.coinCode.UpbitCoinCode.UpbitCoinCode;
import com.example.documentDto.UpbitCandleDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class GeneralCandleRepositoryTest {

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @InjectMocks
    private GeneralCandleRepository candleRepository;

    @Test
    void empty(){
        assertNotNull(candleRepository);
    }

    @ParameterizedTest()
    @MethodSource("get_index_names")
    void testIndex(String indexName) {
        // given
        UpbitCandleDto document =UpbitCandleDto.builder().highPrice(0.0).highPrice(0.0).market("CODE").build();
        IndexCoordinates indexCoordinates = IndexCoordinates.of(indexName);

        // when
        when(elasticsearchOperations.save(document, indexCoordinates)).thenReturn(document);
        UpbitCandleDto result = candleRepository.index(indexName, document);

        // then
        assertEquals(document, result);
    }

    static Stream<Arguments> get_index_names(){
        String UPBIT_CANDLE_PREFIX = "upbit_candle";
        return Arrays.stream(UpbitCoinCode.values())
                .map(code -> String.join("_", UPBIT_CANDLE_PREFIX, code.toString().toLowerCase(Locale.ROOT).replace("-", "_")))
                .map(Arguments::of);
    }
}