package com.crawler.controller;


import com.crawler.entity.AlertTabulate;
import com.crawler.entity.Result;
import com.crawler.service.DataTabulateService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dataTabulate")
public class DataTabulateController {

    @Resource
    private DataTabulateService dataTabulateService;

    @GetMapping("/specialAlert")
    public Result  countSpecialAlert(@RequestParam Integer alertId) {

        AlertTabulate res = dataTabulateService.countSpecialAlert(alertId);

        return Result.success(res);
    }
}
