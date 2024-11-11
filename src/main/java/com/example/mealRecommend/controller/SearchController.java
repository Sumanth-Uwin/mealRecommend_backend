package com.example.mealRecommend.controller;

import com.example.mealRecommend.trie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class SearchController {
    private final trie trie;

    public SearchController() {
        trie = new trie();
        // Populate the trie with your mock vocabulary
        String[] vocabulary = {"vegetarian", "vegan", "keto", "gluten-free", "organic", "local", "sustainable", "delivery", "weekly", "meals", "servings", "family", "single", "diet", "healthy", "fresh", "quick", "easy"};
        for (String word : vocabulary) {
            trie.insert(word);
        }
    }

    @GetMapping("/search")
    public List<String> getCompletions(@RequestParam String prefix) {
        return trie.getCompletions(prefix);
    }
}
