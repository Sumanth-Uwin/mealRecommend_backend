package com.example.mealRecommend.controller;

import com.example.mealRecommend.model.Recipe;
import com.example.mealRecommend.service.WebCrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;

@RestController
@RequestMapping("/search")
public class WebCrawlerController {

    @Autowired
    private WebCrawlerService webCrawlerService;

    // Load and merge data from all sources at application startup
    @PostConstruct
    public void loadInitialData() {
        webCrawlerService.init(); // Ensure the initialization method aligns with the service
    }

    @GetMapping("/suggestions")
    public List<String> getSuggestions(@RequestParam String prefix) {
        return webCrawlerService.getSuggestions(prefix);
    }

    @PostMapping("/recipes")
    public List<Recipe> searchRecipes(@RequestParam String query) {
        return webCrawlerService.searchRecipes(query);
    }

    @GetMapping("/all")
    public List<Recipe> getAllRecipes() {
        return webCrawlerService.getAllRecipes();
    }
}
