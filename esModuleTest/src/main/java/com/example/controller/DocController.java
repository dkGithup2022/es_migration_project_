package com.example.controller;


import com.example.documentDto.UpbitCandleDto;
import com.example.repository.GeneralCandleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DocController {

    private final GeneralCandleRepository candleRepository;

    @RequestMapping(value = "/list")
    private List<UpbitCandleDto> listDoc(@RequestParam String index) {
        List<UpbitCandleDto> docList = candleRepository.search(index, PageRequest.of(0,10));
        return docList;
    }
}
