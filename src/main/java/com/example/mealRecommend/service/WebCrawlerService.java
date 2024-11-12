package com.example.mealRecommend.service;

import com.example.mealRecommend.model.Recipe;
import com.example.mealRecommend.trie;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WebCrawlerService {
    private static final double PRICE_PER_SERVING = 11.00;
    private List<Recipe> mergedRecipes = new ArrayList<>();
    private trie trie = new trie();

    @Autowired
    private WebScraper webScraper; // Second scraper service for additional data sources

    @PostConstruct
    public void init() {
        // Load and merge data from both scraping sources at startup
        List<Recipe> source1Recipes = scrapeRecipesFromSource1();
        List<Recipe> source2Recipes = webScraper.scrapeRecipes();

        // Merge the data and remove duplicates
        mergedRecipes = mergeRecipes(source1Recipes, source2Recipes);

        // Populate the Trie for suggestions
        for (Recipe recipe : mergedRecipes) {
            Arrays.stream(recipe.getName().toLowerCase().split("\\s+")).forEach(trie::insert);
            recipe.getdietaryOptions().forEach(option -> trie.insert(option.toLowerCase()));
        }
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

    public List<Recipe> scrapeRecipesFromSource1() {
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage");
        WebDriver driver = new ChromeDriver(options);
        List<Recipe> recipes = new ArrayList<>();

        try {
            driver.get("https://www.freshprep.ca/menu/this-week");
            driver.manage().window().maximize();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".row")));

            List<WebElement> recipeColumns = driver.findElements(By.xpath("//div[@class='row']//div[@class='recipe-col']"));

            for (WebElement recipeCol : recipeColumns) {
                Recipe recipe = new Recipe();
                String recipeName = recipeCol.findElement(By.xpath(".//h3")).getText();
                String imgUrl = recipeCol.findElement(By.xpath(".//img[@class='logo lazyload']")).getAttribute("src");
                List<WebElement> dietaryIcons = recipeCol.findElements(By.xpath(".//div[@class='recipe-icons']//img[contains(@src, 'dietary-icons-v-2/')]"));
                List<String> iconNames = new ArrayList<>();

                for (WebElement icon : dietaryIcons) {
                    String iconSrc = icon.getAttribute("src");
                    int lastSlashIndex = iconSrc.lastIndexOf('/');
                    int dotIndex = iconSrc.indexOf('.', lastSlashIndex);
                    if (lastSlashIndex != -1 && dotIndex != -1 && dotIndex > lastSlashIndex) {
                        String iconName = iconSrc.substring(lastSlashIndex + 1, dotIndex);
                        iconNames.add(iconName);
                    }
                }

                String servesCount = recipeCol.findElement(By.xpath(".//div[@class='serving-info']//div[contains(@class, 'info-title') and text()='Serves']/following-sibling::div[@class='info-content']")).getText();
                String cookingTime = recipeCol.findElement(By.xpath(".//div[@class='serving-info']//div[contains(@class, 'info-title') and text()='Time']/following-sibling::div[@class='info-content']")).getText();

                recipe.setName(recipeName);
                recipe.setImageUrl(imgUrl);
                recipe.setdietaryOptions(extractDietaryOptions(String.join(" ", iconNames)));
                recipe.setServes(servesCount);
                recipe.setCookingTime(cookingTime);
                try {
                    int numberOfServings = Integer.parseInt(servesCount.replaceAll("[^0-9]", ""));
                    double totalPrice = numberOfServings * PRICE_PER_SERVING;
                    recipe.setPrice(String.format("$%.2f", totalPrice));
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

    private List<Recipe> mergeRecipes(List<Recipe> source1, List<Recipe> source2) {
        List<Recipe> allRecipes = new ArrayList<>(source1);
        allRecipes.addAll(source2);
        return allRecipes.stream().distinct().collect(Collectors.toList());
    }

    private List<String> extractDietaryOptions(String recipeName) {
        List<String> dietaryOptions = new ArrayList<>();
        String nameLowerCase = recipeName.toLowerCase();

        if (containsProtein(nameLowerCase, "chicken", "beef", "pork", "lamb", "fish", "turkey", "duck", "sausage", "seafood", "bacon", "poultry")) {
            dietaryOptions.add("Non-Veg");
        } else if ((nameLowerCase.contains("veg") || nameLowerCase.contains("vegetarian"))) {
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
