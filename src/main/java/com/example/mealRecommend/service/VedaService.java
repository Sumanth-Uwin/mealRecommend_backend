package com.example.mealRecommend.service;

import com.example.mealRecommend.model.Recipe;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VedaService {

    public static List<Recipe> scrapeRecipes() {
        // Set the path to the ChromeDriver
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage");
        WebDriver driver = new ChromeDriver(options);

        List<Recipe> recipes = new ArrayList<>();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            // Navigate to the Home Chef menu page
            driver.get("https://www.homechef.com/our-menu");

            // Wait for the page to load fully
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

            // Loop through each date in the dropdown
            while (true) {
                // Locate the dropdown menu and retrieve all options (re-fetch each loop to avoid stale elements)
                Select dateDropdown = new Select(driver.findElement(By.id("menu_select")));
                List<WebElement> dateOptions = dateDropdown.getOptions();

                for (int j = 1; j < 2; j++) {
                    // Refresh the date options to prevent stale elements
                    dateDropdown = new Select(driver.findElement(By.id("menu_select")));
                    dateOptions = dateDropdown.getOptions();

                    // Select each date
                    WebElement dateOption = dateOptions.get(j);
                    dateOption.click();

                    // Wait for the page to load after selecting a date
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/main/div[2]/section/section/ul")));

                    // Re-fetch all the <li> elements in all <ul> elements under the section
                    List<WebElement> menuItems = driver.findElements(By.xpath("/html/body/main/div[2]/section/section/ul/li"));

                    // Check if menu items are found
                    if (menuItems.isEmpty()) {
                        System.out.println("No menu items found on the page for date: " + dateOption.getText());
                        continue;
                    }

                    // Loop through each menu item and scrape the details
                    for (int i = 0; i < menuItems.size(); i++) {
                        WebElement menuItem = menuItems.get(i);

                        // Extract the relative URL for the recipe from the <a> element within <li>
                        WebElement linkElement = menuItem.findElement(By.cssSelector("a"));
                        String relativeUrl = linkElement.getAttribute("href");

                        // Check if the URL is already absolute, and prepend baseUrl if necessary
                        String fullUrl = relativeUrl.startsWith("http") ? relativeUrl : "https://www.homechef.com" + relativeUrl;

                        // Now navigate to the detailed recipe page
                        driver.get(fullUrl);

                        // Explicit wait for the recipe page to load and the required elements to appear
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("h1.h2.mb-0"))); // Wait for the dish name to be visible

                        try {
                            // Re-fetch the dish name, description, and price to avoid stale elements
                            String dishName = driver.findElement(By.cssSelector("h1.h2.mb-0")).getText();
                            String description = driver.findElement(By.cssSelector("h2.h5.text-charcoal-90.mt-0")).getText();
                            WebElement imgElement = driver.findElement(By.cssSelector("img.lazyloaded"));
                            String imageUrl = imgElement.getAttribute("src");

                            // Wait for the dietary preferences container to load
                            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//ul[contains(@class, 'flex-1')]")));

                            // Extract dietary preferences (tooltip text or visible text) from <li> elements
                            List<WebElement> dietaryPreferences = driver.findElements(By.xpath("//section[4]//ul/li")); // Modified XPath for flexibility
                            StringBuilder dietaryText = new StringBuilder();

                            for (WebElement preference : dietaryPreferences) {
                                String tooltip = preference.getAttribute("data-tooltip");
                                if (tooltip != null && !tooltip.isEmpty()) {
                                    dietaryText.append(tooltip).append("; ");  // Use tooltip text if available
                                } else {
                                    String text = preference.getText();
                                    if (text != null && !text.isEmpty()) {
                                        dietaryText.append(text).append("; ");  // Fallback to visible text
                                    }
                                }
                            }

                            // Check if dietaryText is empty and log the output before appending
                            if (dietaryText.length() == 0) {
                                System.out.println("No dietary options found, setting as 'null'");
                                dietaryText.append("null");
                            }

                            // Debugging - Output dietary options before setting in Recipe
                            //System.out.println("Dietary Options (before setting in Recipe): " + dietaryText.toString());

                            // Extract time and calories
                            String time = driver.findElement(By.cssSelector(".text.inline-block")).getText();
                            String serves = "1"; // This can be dynamic if available
                            String price = "$" + (10 + new Random().nextInt(41)); // Random price between $10 and $50

                            // Create the Recipe object and set its attributes
                            Recipe recipe = new Recipe();
                            recipe.setName(dishName);
                            recipe.setImageUrl(imageUrl);
                            recipe.setServes(serves);
                            recipe.setCookingTime(time);
                            //recipe.setdietaryOptions(dietaryText.toString()); // Convert StringBuilder to String
                            recipe.setdietaryOptions(extractDietaryOptions(String.join(" ", dietaryText)));

                            recipe.setPrice(price);

                            // Debugging - Output recipe details before adding to list
                            System.out.println("Recipe Created: " + recipe.toString());

                            // Add the full Recipe object to the list
                            recipes.add(recipe);

                        } catch (org.openqa.selenium.StaleElementReferenceException e) {
                            System.out.println("Stale element reference, retrying the page load.");
                            continue;
                        }

                        // Go back to the menu page after scraping the recipe
                        driver.navigate().back();

                        // Wait for the menu page to reload
                        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/main/div[2]/section/section/ul")));

                        // Re-fetch all the menu items after the page reloads
                        menuItems = driver.findElements(By.xpath("/html/body/main/div[2]/section/section/ul/li"));
                    }
                }

                // Exit after all dates have been processed
                break;
            }

        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close the browser
            driver.quit();
        }

        return recipes; // Return the list of full Recipe objects
    }
    private static List<String> extractDietaryOptions(String recipeName) {
        List<String> dietaryOptions = new ArrayList<>();
        String nameLowerCase = recipeName.toLowerCase();

        if (nameLowerCase.contains("keto-friendly")) {
            dietaryOptions.add("Keto");
        } else if (nameLowerCase.contains("gluten-smart")) {
            dietaryOptions.add("Gluten-free");
        } else if (nameLowerCase.contains("pescatarian")) {
            dietaryOptions.add("Non-veg");
        } else if (nameLowerCase.contains("vegetarian")) {
            dietaryOptions.add("Veg");
        } else if (nameLowerCase.contains("null")) {
            dietaryOptions.add("Null");
        }else if (!nameLowerCase.contains("Non-veg")&& !nameLowerCase.contains("Veg")) {
            dietaryOptions.add("Non-veg");
        }

        return dietaryOptions;
    }
    private static boolean containsProtein(String recipeName, String... proteins) {
        for (String protein : proteins) {
            if (recipeName.contains(protein)) {
                return true;
            }
        }
        return false;
    }
}
