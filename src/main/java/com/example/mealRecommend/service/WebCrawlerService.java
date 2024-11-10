package com.example.mealRecommend.service;

import com.example.mealRecommend.model.Recipe;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Service;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class WebCrawlerService {
    private static final double PRICE_PER_SERVING = 11.00;

    public List<Recipe> scrapeRecipes() {
        // Use WebDriverManager to automatically download the appropriate WebDriver

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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
        return recipes;
    }
}
