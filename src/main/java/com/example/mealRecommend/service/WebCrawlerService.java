package com.example.mealRecommend.service;

import com.example.mealRecommend.model.Recipe;
import com.example.mealRecommend.trie; // Import the Trie class
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WebCrawlerService {
    private static final double PRICE_PER_SERVING = 11.00;
    private List<Recipe> recipes = new ArrayList<>();
    private trie trie = new trie(); // Initialize the Trie

    @PostConstruct
    public void init() {
        // Automatically scrape data and populate the Trie on startup
        List<Recipe> scrapedRecipes = scrapeRecipes();
        for (Recipe recipe : scrapedRecipes) {
            // Insert individual words from the recipe name into the Trie
            Arrays.stream(recipe.getName().toLowerCase().split("\\s+"))
                    .forEach(trie::insert);

            // Insert dietary options into the Trie
            recipe.getdietaryOptions().forEach(option -> trie.insert(option.toLowerCase()));
        }
    }

    public List<String> getSuggestions(String prefix) {
        // Use Trie to get suggestions based on the prefix
        return trie.getCompletions(prefix.toLowerCase());
    }

    public List<Recipe> searchRecipes(String query) {
        String lowerCaseQuery = query.toLowerCase();
        return recipes.stream()
                .filter(recipe -> recipe.getName().toLowerCase().contains(lowerCaseQuery) ||
                        recipe.getdietaryOptions().stream().anyMatch(option -> option.toLowerCase().contains(lowerCaseQuery)))
                .collect(Collectors.toList());
    }

    public List<Recipe> scrapeRecipes() {
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");

        // Configure ChromeOptions for headless mode
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Run Chrome in headless mode
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        // Initialize WebDriver with ChromeOptions
        WebDriver driver = new ChromeDriver(options);
        List<Recipe> recipes = new ArrayList<>();

        try {
            driver.get("https://www.freshprep.ca/menu/this-week");
            driver.manage().window().maximize();

            // Wait for the page to load and find elements
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".row")));

            List<WebElement> recipeColumns = driver.findElements(By.xpath("//div[@class='row']//div[@class='recipe-col']"));

            for (WebElement recipeCol : recipeColumns) {
                Recipe recipe = new Recipe();

                // Extract recipe details
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
                recipe.setdietaryOptions(iconNames);
                recipe.setServes(servesCount);
                recipe.setCookingTime(cookingTime);

                try {
                    int numberOfServings = Integer.parseInt(servesCount.replaceAll("[^0-9]", ""));
                    double totalPrice = numberOfServings * PRICE_PER_SERVING;
                    recipe.setPrice(String.format("$%.2f", totalPrice));
                } catch (NumberFormatException e) {
                    recipe.setPrice("N/A"); // Set to "N/A" if the serving count cannot be parsed
                }

                recipes.add(recipe);
            }

            // Save scraped data in the service-level variable
            this.recipes = recipes;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
        return recipes;
    }

    public List<Recipe> getAllRecipes() {
        return new ArrayList<>(recipes);
    }
}
