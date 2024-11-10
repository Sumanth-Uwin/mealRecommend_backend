
package com.example.mealRecommend.controller;

import com.example.mealRecommend.model.Recipe;
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

    @GetMapping("/wb1")
    public List<Recipe> crawlAllMealKitServices() throws IOException {
        return webCrawlerService.scrapeRecipes();
    }
}
