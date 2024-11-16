package com.example.mealRecommend.service;

import com.example.mealRecommend.model.Recipe;
import com.example.mealRecommend.trie;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WebCrawlerService {
    private static final double PRICE_PER_SERVING = 11.00;
    private List<Recipe> mergedRecipes = new ArrayList<>();
    private trie trie = new trie();
    private boolean initialized = false;

    @Autowired
    private WebScraper webScraper;

    @PostConstruct
    public synchronized void init() {
        if (initialized) {
            return; // Ensure scraping runs only once
        }

        System.out.println("Starting Web Scraping...");
        List<Recipe> allRecipes = new ArrayList<>();

        try {
            // Centralized scraping execution
            allRecipes.addAll(scrapeRecipesFromFreshPrep());
            allRecipes.addAll(webScraper.scrapeRecipes());
            allRecipes.addAll(VedaService.scrapeRecipes());
            allRecipes.addAll(DinnerlyRecipeScraper.scrapeRecipes());
            allRecipes.addAll(SnapKitchenService.scrapeRecipes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Merge recipes and remove duplicates
        mergedRecipes = mergeRecipes(allRecipes);

        // Populate the Trie for suggestions
        populateTrie();

        initialized = true;
        System.out.println("Web Scraping Completed. Total Recipes: " + mergedRecipes.size());
    }

    public List<Recipe> getAllRecipes() {
        return new ArrayList<>(mergedRecipes);
    }

    public List<String> getSuggestions(String prefix) {
        return trie.getCompletions(prefix.toLowerCase());
    }

    public List<Recipe> searchRecipes(String query) {
        String lowerCaseQuery = query.toLowerCase();
        return mergedRecipes.stream()
                .filter(recipe -> recipe.getName().toLowerCase().contains(lowerCaseQuery) ||
                        recipe.getdietaryOptions().stream().anyMatch(option -> option.toLowerCase().contains(lowerCaseQuery)))
                .collect(Collectors.toList());
    }

    private List<Recipe> scrapeRecipesFromFreshPrep() {
        WebDriver driver = initializeDriver();
        List<Recipe> recipes = new ArrayList<>();

        try {
            driver.get("https://www.freshprep.ca/menu/this-week");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".row")));

            List<WebElement> recipeColumns = driver.findElements(By.xpath("//div[@class='row']//div[@class='recipe-col']"));

            for (WebElement recipeCol : recipeColumns) {
                Recipe recipe = new Recipe();
                String recipeName = recipeCol.findElement(By.xpath(".//h3")).getText();
                String imgUrl = recipeCol.findElement(By.xpath(".//img[@class='logo lazyload']")).getAttribute("src");

                List<WebElement> dietaryIcons = recipeCol.findElements(By.xpath(".//div[@class='recipe-icons']//img[contains(@src, 'dietary-icons-v-2/')]"));
                List<String> iconNames = dietaryIcons.stream()
                        .map(icon -> icon.getAttribute("src"))
                        .map(src -> src.substring(src.lastIndexOf('/') + 1, src.indexOf('.', src.lastIndexOf('/'))))
                        .collect(Collectors.toList());

                String servesCount = recipeCol.findElement(By.xpath(".//div[contains(@class, 'info-title') and text()='Serves']/following-sibling::div")).getText();
                String cookingTime = recipeCol.findElement(By.xpath(".//div[contains(@class, 'info-title') and text()='Time']/following-sibling::div")).getText();

                recipe.setName(recipeName);
                recipe.setImageUrl(imgUrl);
                recipe.setdietaryOptions(extractDietaryOptions(String.join(" ", iconNames)));
                recipe.setServes(servesCount);
                recipe.setCookingTime(cookingTime);

                try {
                    int numberOfServings = Integer.parseInt(servesCount.replaceAll("[^0-9]", ""));
                    recipe.setPrice(String.format("$%.2f", numberOfServings * PRICE_PER_SERVING));
                } catch (NumberFormatException e) {
                    recipe.setPrice("N/A");
                }

                recipes.add(recipe);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        return recipes;
    }

    private WebDriver initializeDriver() {
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage");
        return new ChromeDriver(options);
    }

    private void populateTrie() {
        for (Recipe recipe : mergedRecipes) {
            Arrays.stream(recipe.getName().toLowerCase().split("\\s+")).forEach(trie::insert);
            recipe.getdietaryOptions().forEach(option -> trie.insert(option.toLowerCase()));
        }
    }

    private List<Recipe> mergeRecipes(List<Recipe>... sources) {
        List<Recipe> allRecipes = new ArrayList<>();
        for (List<Recipe> source : sources) {
            allRecipes.addAll(source);
        }
        return allRecipes.stream().distinct().collect(Collectors.toList());
    }

    private List<String> extractDietaryOptions(String recipeName) {
        List<String> dietaryOptions = new ArrayList<>();
        String nameLowerCase = recipeName.toLowerCase();

        if (containsProtein(nameLowerCase, "chicken", "beef", "pork", "lamb", "fish", "turkey", "duck", "sausage", "seafood", "bacon", "poultry")) {
            dietaryOptions.add("Non-Veg");
        } else if (nameLowerCase.contains("veg") || nameLowerCase.contains("vegetarian")) {
            dietaryOptions.add("Veg");
        } else if (nameLowerCase.contains("vegan")) {
            dietaryOptions.add("Vegan");
        } else if (nameLowerCase.contains("keto")) {
            dietaryOptions.add("Keto");
        } else if (nameLowerCase.contains("gluten")) {
            dietaryOptions.add("Gluten");
        } else {
            dietaryOptions.add("Vegetarian");
        }

        return dietaryOptions;
    }

    private boolean containsProtein(String recipeName, String... proteins) {
        for (String protein : proteins) {
            if (recipeName.contains(protein)) {
                return true;
            }
        }
        return false;
    }
}
