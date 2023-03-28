package com.example.documentDto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class UpbitCandleDto {
    @NotNull
    @JsonAlias("timestamp")
    private Long timestamp;

    @NotNull
    @JsonAlias({"code","market","cd"})
    private String market;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonAlias("candle_date_time_utc")
    private Date candleDateTimeUtc;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonAlias("candle_date_time_kst")
    private Date candleDateTimeKst;

    @JsonAlias("opening_price")
    private Double openingPrice;

    @JsonAlias("high_price")
    private Double highPrice;

    @JsonAlias("low_price")
    private Double lowPrice;

    @JsonAlias("trade_price")
    private Double tradePrice;

    @JsonAlias("candle_acc_trade_price")
    private Double candleAccTradePrice;

    @JsonAlias("candle_acc_trade_volume")
    private Double candleAccTradeVolume;
}
