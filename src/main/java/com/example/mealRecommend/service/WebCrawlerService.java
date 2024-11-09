package com.example.mealRecommend.service;

import com.example.mealRecommend.model.MealKitServiceData;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class WebCrawlerService {

    static {
        // Automatically sets up the ChromeDriver for Selenium
        WebDriverManager.chromedriver().setup();
    }

    public List<MealKitServiceData> crawlAllServices() {
        // Set the system property for the ChromeDriver and initialize WebDriver
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        List<MealKitServiceData> mealKitServices = new ArrayList<>();

        try {
            // List of URLs to scrape
            String[] urls = {
                    "https://www.wecookmeals.ca/en/week-menu/2024-10-06",
                    // Add other URLs as needed
            };

            for (String url : urls) {
                driver.get(url);
                driver.manage().window().maximize();

                // Scrape main menu items
                mealKitServices.addAll(scrapeMenuItems(driver));

                // Handle different sections if needed (e.g., Family Meal, Upsells)
                mealKitServices.addAll(clickAndScrapeSection(driver, ".swiper-slide.w-fit.page-menu-item__family-meal", "Family Meal Recipes"));
                mealKitServices.addAll(clickAndScrapeSection(driver, ".swiper-slide.w-fit.page-menu-item__week_upsells", "Week Upsells Recipes"));
                mealKitServices.addAll(clickAndScrapeSection(driver, ".swiper-slide.w-fit.page-menu-item__groceries", "Groceries Recipes"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        return mealKitServices;
    }

    private List<MealKitServiceData> scrapeMenuItems(WebDriver driver) {
        List<MealKitServiceData> data = new ArrayList<>();
        List<WebElement> recipeImageElements = driver.findElements(By.xpath("//img[contains(@src,'https://cdn.')]"));
        for (WebElement element : recipeImageElements) {
            String imageUrl = element.getAttribute("src");
            String recipeTitle = element.getAttribute("alt");

            if (recipeTitle != null && !recipeTitle.isEmpty()) {
                MealKitServiceData serviceData = new MealKitServiceData();
                serviceData.setName(recipeTitle);
                serviceData.setImageUrl(imageUrl);
                data.add(serviceData);
            }
        }
        return data;
    }

    private List<MealKitServiceData> clickAndScrapeSection(WebDriver driver, String cssSelector, String sectionName) {
        List<MealKitServiceData> data = new ArrayList<>();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(40));
        try {
            WebElement sectionElement = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(cssSelector)));
            sectionElement.click(); // Click to reveal the section

            // Wait for the section content to load
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".text-body-sm, .text-body-md")));

            // Scrape the menu items in the clicked section
            data.addAll(scrapeMenuItems(driver));
        } catch (Exception e) {
            System.out.println("Error while processing section: " + sectionName);
            e.printStackTrace();
        }
        return data;
    }
}
