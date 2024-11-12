package com.example.mealRecommend.service;

import com.example.mealRecommend.model.Recipe;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class WebScraper {

    private static final double PRICE_PER_SERVING = 11.00; // Price per serving

    public List<Recipe> scrapeRecipes() {
        String baseUrl = "https://www.hellofresh.ca/recipes/";
        //String[] tags = {
        //     "canadian-recipes"};

        String[] tags = {
                "canadian-recipes", "asian-recipes", "american-recipes", "australian-recipes",
                "british-recipes", "chinese-recipes", "french-recipes", "fusion-recipes",
                "german-recipes", "cuban-recipes", "greek-recipes", "indian-recipes",
                "african-recipes", "european-recipes", "caribbean-recipes", "eastern-european-recipes"
        };

        List<Recipe> recipes = new ArrayList<>();

        // Loop through each recipe category and scrape the recipes
        for (String tag : tags) {
            String url = baseUrl + tag;
            try {
                Document doc = Jsoup.connect(url).get();
                Elements links = doc.select("a[href]");

                List<String> recipeLinks = new ArrayList<>();
                // Extract recipe links from the page
                for (Element link : links) {
                    String href = link.attr("href");
                    if (href.contains("recipe") && href.startsWith("https://")) {
                        recipeLinks.add(href);
                    }
                }

                // Crawl each recipe and add the full Recipe object to the list
                for (String recipeLink : recipeLinks) {
                    try {
                        Document recipeDoc = Jsoup.connect(recipeLink).get();

                        // Extract recipe details
                        String recipeName = recipeDoc.select("h1").text(); // Extract recipe name
                        String prepTime = extractTime(recipeDoc); // Extract prep time
                        String imageUrl = extractImageUrl(recipeDoc); // Extract image URL
                        if ("Not Found".equals(imageUrl)) {
                            continue; // Skip if image URL is not found
                        }
                        List<String> dietaryOptions = extractDietaryOptions(recipeName);

                        // Create the Recipe object and set its attributes
                        Recipe recipe = new Recipe();
                        recipe.setName(recipeName);
                        recipe.setImageUrl(imageUrl);
                        recipe.setServes("2"); // Default serving size, can be modified as needed
                        recipe.setCookingTime(prepTime);
                        recipe.setdietaryOptions(dietaryOptions);

                        // Set price with a random range between $10 and $50
                        String price = "$" + (10 + new Random().nextInt(41));
                        recipe.setPrice(price);

                        recipes.add(recipe); // Add the full Recipe object to the list

                    } catch (IOException e) {
                        System.err.println("Failed to scrape recipe page: " + recipeLink);
                        e.printStackTrace(); // Handle error while crawling individual recipes
                    }
                }

            } catch (IOException e) {
                System.err.println("Failed to scrape category page: " + url);
                e.printStackTrace(); // Handle error while scraping the category page
            }
        }

        return recipes; // Return the list of full Recipe objects
    }


    private List<String> extractDietaryOptions(String recipeName) {
        List<String> dietaryOptions = new ArrayList<>();

        // Convert recipe name to lower case for case-insensitive comparison
        String nameLowerCase = recipeName.toLowerCase();

        // Check for protein-based non-vegetarian options
        if (containsProtein(nameLowerCase, "chicken", "beef", "pork", "lamb", "fish", "turkey", "duck", "sausage", "seafood", "bacon")) {
            dietaryOptions.add("Non-Veg");
        }
        // Check for vegetarian (excluding non-veg)
        else if ((nameLowerCase.contains("veg") || nameLowerCase.contains("vegetarian")) && !dietaryOptions.contains("Non-Veg")) {
            dietaryOptions.add("Veg");
        }
        // Check for vegan
        else if (nameLowerCase.contains("vegan") && !dietaryOptions.contains("Non-Veg")) {
            dietaryOptions.add("Vegan");
        }
        // Check for keto
        else if (nameLowerCase.contains("keto")) {
            dietaryOptions.add("Keto");
        }
        // Check for gluten-free
        else if (nameLowerCase.contains("gluten-free")) {
            dietaryOptions.add("Gluten-Free");
        }
        // Check for paleo
        else if (nameLowerCase.contains("paleo")) {
            dietaryOptions.add("Paleo");
        }
        // Check for low-carb
        else if (nameLowerCase.contains("low-carb")) {
            dietaryOptions.add("Low-Carb");
        }
        // Check for dairy-free
        else if (nameLowerCase.contains("dairy-free")) {
            dietaryOptions.add("Dairy-Free");
        }
        // Check for sugar-free
        else if (nameLowerCase.contains("sugar-free")) {
            dietaryOptions.add("Sugar-Free");
        }
        // Check for high-protein
        else if (nameLowerCase.contains("high-protein")) {
            dietaryOptions.add("High-Protein");
        }
        // Check for low-fat
        else if (nameLowerCase.contains("low-fat")) {
            dietaryOptions.add("Low-Fat");
        }
        // Check for high-fiber
        else if (nameLowerCase.contains("high-fiber")) {
            dietaryOptions.add("High-Fiber");
        }
        // Check for whole30
        else if (nameLowerCase.contains("whole30")) {
            dietaryOptions.add("Whole30");
        }
        // Check for detox
        else if (nameLowerCase.contains("detox")) {
            dietaryOptions.add("Detox");
        }
        // Check for meal prep
        else if (nameLowerCase.contains("meal-prep")) {
            dietaryOptions.add("Meal-Prep");
        }
        // Check for heart-healthy
        else if (nameLowerCase.contains("heart-healthy")) {
            dietaryOptions.add("Heart-Healthy");
        }
        // Check for clean eating
        else if (nameLowerCase.contains("clean-eating")) {
            dietaryOptions.add("Clean-Eating");
        }
        // Check for anti-inflammatory
        else if (nameLowerCase.contains("anti-inflammatory")) {
            dietaryOptions.add("Anti-Inflammatory");
        }
        // Check for brain-boosting
        else if (nameLowerCase.contains("brain-boosting")) {
            dietaryOptions.add("Brain-Boosting");
        }
        // Check for low-sodium
        else if (nameLowerCase.contains("low-sodium")) {
            dietaryOptions.add("Low-Sodium");
        }
        // Check for raw
        else if (nameLowerCase.contains("raw")) {
            dietaryOptions.add("Raw");
        }
        // Check for legume-free
        else if (nameLowerCase.contains("legume-free")) {
            dietaryOptions.add("Legume-Free");
        }
        // Check for soy-free
        else if (nameLowerCase.contains("soy-free")) {
            dietaryOptions.add("Soy-Free");
        }
        // Check for caffeine-free
        else if (nameLowerCase.contains("caffeine-free")) {
            dietaryOptions.add("Caffeine-Free");
        }
        // Check for energy-boosting
        else if (nameLowerCase.contains("energy-boosting")) {
            dietaryOptions.add("Energy-Boosting");
        }

        // Additional meal type categories
        else if (nameLowerCase.contains("breakfast")) {
            dietaryOptions.add("Breakfast");
        }
        else if (nameLowerCase.contains("lunch")) {
            dietaryOptions.add("Lunch");
        }
        else if (nameLowerCase.contains("dinner")) {
            dietaryOptions.add("Dinner");
        }
        else if (nameLowerCase.contains("snack")) {
            dietaryOptions.add("Snack");
        }
        else if (nameLowerCase.contains("dessert")) {
            dietaryOptions.add("Dessert");
        }
        else if (nameLowerCase.contains("smoothie")) {
            dietaryOptions.add("Smoothie");
        }
        else if (nameLowerCase.contains("salad")) {
            dietaryOptions.add("Salad");
        }
        else if (nameLowerCase.contains("soup")) {
            dietaryOptions.add("Soup");
        }
        else if (nameLowerCase.contains("wrap")) {
            dietaryOptions.add("Wrap");
        }
        else if (nameLowerCase.contains("pizza")) {
            dietaryOptions.add("Pizza");
        }
        else if (nameLowerCase.contains("pasta")) {
            dietaryOptions.add("Pasta");
        }
        else if (nameLowerCase.contains("burger")) {
            dietaryOptions.add("Burger");
        }
        else {
            dietaryOptions.add("Vegetarian");
        }

        // Return the list of dietary options
        return dietaryOptions;
    }


    // Helper function to check if recipe name contains any of the specified protein types
    private boolean containsProtein(String recipeName, String... proteins) {
        for (String protein : proteins) {
            if (recipeName.contains(protein)) {
                return true;
            }
        }
        return false;
    }



    private String extractTime(Document recipeDoc) {
        // Extract preparation time using a specific class or tag
        Elements timeDiv = recipeDoc.select("div[data-zest='hellofresh'] div[display='flex']:has(span[data-translation-id='recipe-detail.preparation-time'])");
        if (!timeDiv.isEmpty()) {
            Elements timeSpan = timeDiv.select("span.sc-54d3413f-0.iIitYD");
            return timeSpan.isEmpty() ? "Not Found" : timeSpan.first().text();
        }
        return "Not Found";
    }

    private String extractImageUrl(Document recipeDoc) {
        // Extract image URL for the recipe
        Element imageElement = recipeDoc.selectFirst("div[data-test-id='recipe-hero-image'] img");
        if (imageElement != null) {
            String imageUrl = imageElement.attr("src");
            return imageUrl.isEmpty() ? "Not Found" : imageUrl;
        }
        return "Not Found";
    }

    private List<String> extractIngredients(Document recipeDoc) {
        // Extract ingredients or dietary options from the recipe
        List<String> ingredients = new ArrayList<>();
        Elements ingredientElements = recipeDoc.select("span[data-test-id='description-body-title']");

        for (Element ingredient : ingredientElements) {
            String text = ingredient.text().trim();
            if (text.contains("•")) {
                String[] parts = text.split("•", 2);
                if (parts.length > 1) {
                    String ingredientText = parts[1].trim();
                    ingredients.add(ingredientText);
                }
            }
        }
        return ingredients;
    }
}
