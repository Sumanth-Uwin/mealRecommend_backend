package com.example.mealRecommend.service;

import com.example.mealRecommend.model.Recipe;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class DinnerlyRecipeScraper {

    private static final String MENU_URL = "https://dinnerly.com/menu";

    public static List<Recipe> scrapeRecipes() {
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080", "--no-sandbox", "--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(40));
        List<Recipe> recipes = new ArrayList<>();

        try {
            driver.get(MENU_URL);
            scrollToLoadRecipes(driver);

            List<WebElement> recipeElements = driver.findElements(By.className("menu-page__recipe"));

            for (int i = 0; i < recipeElements.size(); i++) {
                if (i % 30 == 0 && i != 0) {
                    restartBrowser(driver);
                }

                recipeElements = driver.findElements(By.className("menu-page__recipe"));
                WebElement recipeElement = recipeElements.get(i);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", recipeElement);

                Recipe recipe = loadRecipeDetails(driver, wait, i);
                if (recipe != null) {
                    recipes.add(recipe);
                }
                navigateBackToMenu(driver, wait);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
        return recipes;
    }

    private static void scrollToLoadRecipes(WebDriver driver) throws InterruptedException {
        int previousCount = 0;
        while (true) {
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 1000)");
            Thread.sleep(1000);
            List<WebElement> recipeElements = driver.findElements(By.className("menu-page__recipe"));
            if (recipeElements.size() == previousCount) break;
            previousCount = recipeElements.size();
        }
    }

    private static Recipe loadRecipeDetails(WebDriver driver, WebDriverWait wait, int index) {
        try {
            WebElement recipeElement = driver.findElements(By.className("menu-page__recipe")).get(index);
            recipeElement.click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("recipe-title")));

            Recipe recipe = new Recipe();
            WebElement nameElement = driver.findElement(By.className("recipe-title"));
            recipe.setName(nameElement.getText());

            WebElement imageElement = driver.findElement(By.className("recipe-image"));
            recipe.setImageUrl(imageElement.getAttribute("src"));

            recipe.setServes("4");

            WebElement cookingTimeElement = driver.findElement(By.className("dish-details__attribute-detail"));
            recipe.setCookingTime(cookingTimeElement.getText());

            List<WebElement> dietaryOptionElements = driver.findElements(By.className("recipe-attributes__labels"));
            List<String> dietaryOptions = new ArrayList<>();
            for (WebElement dietaryOption : dietaryOptionElements) {
                dietaryOptions.add(dietaryOption.getText());
            }
            recipe.setdietaryOptions(dietaryOptions);

            recipe.setPrice("$30.00");

            return recipe;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void navigateBackToMenu(WebDriver driver, WebDriverWait wait) {
        try {
            driver.navigate().to(MENU_URL);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("menu-page__recipe")));
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, -300)");
            Thread.sleep(1000);
        } catch (Exception e) {
            System.err.println("Failed to navigate back to menu.");
        }
    }

    private static void restartBrowser(WebDriver driver) {
        driver.quit();
        System.setProperty("webdriver.chrome.driver", "path/to/chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080", "--no-sandbox", "--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        driver.get(MENU_URL);
    }
}
