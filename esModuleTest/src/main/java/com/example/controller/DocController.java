package com.example.controller;


import com.example.repository.GeneralDocRepository;
import com.example.document.MyDoc;
import com.example.repository.MyDocRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DocController {

    private final MyDocRepository docRepository;
    private final GeneralDocRepository generalDocRepository;

    @RequestMapping(value = "/gen")
    private MyDoc genDoc(@RequestParam String index, @RequestParam String name) {
        MyDoc doc = MyDoc.builder().name(name).build();
        return generalDocRepository.indexDocument(index, doc);
    }

    @RequestMapping(value = "/list")
    private List<MyDoc> listDoc(@RequestParam String index) {
        List<MyDoc> docList = generalDocRepository.searchByIndex(index, PageRequest.of(0,10));
        return docList;
    }


}
