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

    // Endpoint to trigger data scraping and load recipes when the application starts
    @PostConstruct
    public void loadInitialData() {
        webCrawlerService.scrapeRecipes(); // Scrape and store recipes on application startup
    }

    // Endpoint to get recipe suggestions based on a prefix
    @GetMapping("/suggestions")
    public List<String> getSuggestions(@RequestParam String prefix) {
        return webCrawlerService.getSuggestions(prefix);
    }

    // Endpoint to search recipes based on a query term
    @PostMapping("/recipes")
    public List<Recipe> searchRecipes(@RequestParam String query) {
        return webCrawlerService.searchRecipes(query);
    }

    // Endpoint to get all recipes (optional)
    @GetMapping("/all")
    public List<Recipe> getAllRecipes() {
        return webCrawlerService.getAllRecipes();
    }
}
