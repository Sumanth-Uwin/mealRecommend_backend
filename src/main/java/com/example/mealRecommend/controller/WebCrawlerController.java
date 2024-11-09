
package com.example.mealRecommend.controller;

import com.example.mealRecommend.model.MealKitServiceData;
import com.example.mealRecommend.service.WebCrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class WebCrawlerController {

    @Autowired
    private WebCrawlerService webCrawlerService;

    @GetMapping("/crawl-meal-kit-services")
    public List<MealKitServiceData> crawlAllMealKitServices() throws IOException {
        return webCrawlerService.crawlAllServices();
    }
}
