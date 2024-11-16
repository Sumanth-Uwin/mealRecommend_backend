package com.example.mealRecommend.service;

import com.example.mealRecommend.model.Recipe;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Service
public class SnapKitchenService {

    public static List<Recipe> scrapeRecipes() {
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage");
        WebDriver driver = new ChromeDriver(options);

        List<Recipe> recipes = new ArrayList<>();

        try {
            driver.get("https://snapkitchen.com/Menu?scrollTo=92ad5e98-70e5-4983-7661-08da32a32998");

            // Scroll incrementally to ensure all recipes are loaded
            JavascriptExecutor js = (JavascriptExecutor) driver;
            long scrollHeight = (long) js.executeScript("return document.body.scrollHeight");
            int scrollStep = 300;
            int currentScroll = 0;

            while (currentScroll < scrollHeight) {
                js.executeScript("window.scrollBy(0, arguments[0]);", scrollStep);
                currentScroll += scrollStep;
                Thread.sleep(1000);
                scrollHeight = (long) js.executeScript("return document.body.scrollHeight");
            }

            // Locate recipe links
            List<WebElement> recipeLinkElements = driver.findElements(By.cssSelector(".menu-product-cart-button"));
            Set<String> recipeLinks = new HashSet<>();
            for (WebElement linkElement : recipeLinkElements) {
                String recipeLink = linkElement.getAttribute("href");
                if (recipeLink != null && !recipeLink.isEmpty()) {
                    recipeLinks.add(recipeLink);
                }
            }

            // Visit each recipe URL and extract data
            for (String link : recipeLinks) {
                driver.get(link);
                Thread.sleep(3000);

                Recipe recipe = new Recipe();
                try {
                    WebElement productTitleElement = driver.findElement(By.cssSelector("h1.productdetails-title"));
                    String productTitle = productTitleElement.getText();
                    int servings = extractServings(productTitle);
                    recipe.setName(cleanTitle(productTitle));
                    recipe.setServes(String.valueOf(servings));

                    // Extract labels
                    List<WebElement> labels = driver.findElements(By.cssSelector("div.productdetails-labels-tags img"));
                    List<String> dietaryOptions = new ArrayList<>();
                    for (WebElement label : labels) {
                        String labelTitle = label.getAttribute("title");
                        if (labelTitle != null && !labelTitle.isEmpty()) {
                            dietaryOptions.add(labelTitle);
                        }
                    }
                    recipe.setdietaryOptions(dietaryOptions);

                    // Extract first image
                    List<WebElement> productImages = driver.findElements(By.cssSelector("img.productdetails-image"));
                    if (!productImages.isEmpty()) {
                        recipe.setImageUrl(productImages.get(0).getAttribute("src"));
                    }

                    // Set price
                    recipe.setPrice(generatePrice(servings));

                    // Add cooking time or status
                    if (servings == 4) {
                        recipe.setCookingTime("60min");
                    } else if (servings == 1) {
                        recipe.setCookingTime("Ready to eat");
                    }

                    recipes.add(recipe);

                } catch (Exception e) {
                    System.out.println("Error extracting data for URL: " + link);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        return recipes;
    }

    // Extract number of servings from the product title
    private static int extractServings(String title) {
        if (title.matches(".*\\(For \\d+\\).*")) {
            int startIndex = title.indexOf("For") + 4;
            int endIndex = title.indexOf(")", startIndex);
            if (startIndex > 0 && endIndex > startIndex) {
                try {
                    return Integer.parseInt(title.substring(startIndex, endIndex).trim());
                } catch (NumberFormatException e) {
                    // If parsing fails, return default
                }
            }
        }
        return 1; // Default servings
    }

    // Clean product title by removing parentheses
    private static String cleanTitle(String title) {
        return title.replaceAll("\\s*\\([^)]*\\)", "").trim();
    }

    // Generate random price based on servings
    private static String generatePrice(int servings) {
        Random random = new Random();
        DecimalFormat df = new DecimalFormat("$#.00");
        if (servings == 4) {
            double price = 59.99 + (random.nextDouble() * (119.99 - 59.99));
            return df.format(price);
        } else if (servings == 1) {
            double price = 10.00 + (random.nextDouble() * (19.99 - 10.00));
            return df.format(price);
        }
        return "$0.00"; // Default price
    }
}
