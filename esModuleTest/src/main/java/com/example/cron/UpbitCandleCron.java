package com.example.cron;


import com.dk0124.cdr.constants.Uri;
import com.dk0124.cdr.constants.coinCode.UpbitCoinCode.UpbitCoinCode;
import com.example.documentDto.UpbitCandleDto;
import com.example.repository.GeneralCandleRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpbitCandleCron {

    private final ObjectMapper objectMapper;
    private final GeneralCandleRepository candleRepository;

    @Scheduled(cron = "00 */3 * * * *")
    public void upbitCandleSupply() throws JsonProcessingException, InterruptedException {
        Long current = System.currentTimeMillis();
        log.info("upbitCandleSupply | current time : {}", current);
        for (UpbitCoinCode code : UpbitCoinCode.values()) {
            Thread.sleep(500);
            List<UpbitCandleDto> candles = getMinutesCandles(code, current, 10);
            for (UpbitCandleDto candle : candles) {
                candleRepository.indexDocument(getIndex(candle.getMarket()), candle);
            }
            log.info("code : {} -> size: {}", code, candles.size());
        }
    }

    public List<UpbitCandleDto> getMinutesCandles(UpbitCoinCode code, Long millis, int count) throws JsonProcessingException, InterruptedException {
        RestTemplate restTemplate = new RestTemplate();
        String url = getRequestUrl(code, millis);
        log.info("getLastMinutesCandles () :  url : {}", url);
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            List<UpbitCandleDto> candles = Arrays.asList(objectMapper.readValue(response.getBody(), UpbitCandleDto[].class));
            log.info("code : {} , candle : {}", code, candles.get(0).toString());
            return candles;
        } catch (HttpClientErrorException clientErrorException) {
            if (clientErrorException.getRawStatusCode() == 429) {
                Thread.sleep(500);
                return getMinutesCandles(code, millis, count);
            }
            log.error("getMinutesCandles | code: {} , millis : {}, count ; {}", code, millis, count);
        }
        return new ArrayList<UpbitCandleDto>();

    }

    private String getRequestUrl(UpbitCoinCode code, Long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String baseUrl = Uri.UPBIT_REST_CANDLE_MINUTES_URI.getAddress();
        String marketParam = "?market=" + code.toString();
        String to = "&to=" + sdf.format(new Date(millis));
        String suffix = "&count=10";
        return baseUrl + marketParam + to + suffix;
    }

    private String getIndex(String market) {
        String UpbitCandlePrefix = "upbit_candle";
        if (UpbitCoinCode.fromString(market) == null)
            throw new RuntimeException("INVALID CODE");
        String[] splitted = market.toString().toLowerCase(Locale.ROOT).split("-");
        return UpbitCandlePrefix + "_" + String.join("_", splitted);

    }
}
