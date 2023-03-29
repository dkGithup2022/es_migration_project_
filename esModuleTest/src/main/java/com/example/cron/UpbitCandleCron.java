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


/**
 *  Upbit API 와 연동 확인용 cron
 *  -> 중복 데이터 확인 ->  repository 클래스에 id 추가 필요 .
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UpbitCandleCron {

    private final ObjectMapper objectMapper;
    private final GeneralCandleRepository candleRepository;

    @Scheduled(cron = "00 */3 * * * *")
    public void upbitCandleSupply() throws JsonProcessingException, InterruptedException {
        Long current = System.currentTimeMillis();
        for (UpbitCoinCode code : UpbitCoinCode.values()) {
            Thread.sleep(500);
            List<UpbitCandleDto> candles = getMinutesCandlesForCode(code, current, 10);
            for (UpbitCandleDto candle : candles) {
                candleRepository.index(getIndex(candle.getMarket()), candle);
            }
            log.info("code : {} -> size: {}", code, candles.size());
        }
    }



    private String getRequestUrl(UpbitCoinCode code, Long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String baseUrl = Uri.UPBIT_REST_CANDLE_MINUTES_URI.getAddress();
        String marketParam = "?market=" + code.toString();
        String to = "&to=" + sdf.format(new Date(millis));
        String suffix = "&count=10";
        return baseUrl + marketParam + to + suffix;
    }

    public List<UpbitCandleDto> getMinutesCandlesForCode(UpbitCoinCode code, Long millis, int count) throws InterruptedException {
        String requestUrl = getRequestUrl(code, millis);

        RestTemplate restTemplate = new RestTemplate();
        List<UpbitCandleDto> minutesCandles = Collections.emptyList();

        for (int i = 0; i < 3; i++) { // 최대 3번 재시도
            try {
                ResponseEntity<String> response = restTemplate.getForEntity(requestUrl, String.class);
                minutesCandles = Arrays.asList(objectMapper.readValue(response.getBody(), UpbitCandleDto[].class));
                break;
            } catch (HttpClientErrorException e) {
                if (e.getRawStatusCode() == 429) {
                    Thread.sleep(500);
                } else {
                    throw e;
                }
            } catch (JsonProcessingException e) {
                log.error("Invalid res body: JsonProcessingException ");
                log.error(e.getMessage());
            }
        }

        return minutesCandles;
    }


    private String getIndex(String market) {
        String UpbitCandlePrefix = "upbit_candle";
        if (UpbitCoinCode.fromString(market) == null)
            throw new RuntimeException("INVALID CODE");
        String[] splitted = market.toString().toLowerCase(Locale.ROOT).split("-");
        return UpbitCandlePrefix + "_" + String.join("_", splitted);

    }
}
