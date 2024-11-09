package com.example.mealRecommend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
public class MealKitServiceData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String weeklyCost;
    private String prepTime;
    private String mealOptions;
    private String dietaryPreferences;
    private String deliveryFrequency;
    private String ingredientQuality;
    private String imageUrl; // New field for storing the image URL

    // Constructors
    public MealKitServiceData() {
    }

    public MealKitServiceData(String name, String weeklyCost,String prepTime, String mealOptions,
                              String dietaryPreferences, String deliveryFrequency,
                              String ingredientQuality, String imageUrl) {
        this.name = name;
        this.weeklyCost = weeklyCost;
        this.prepTime = prepTime;
        this.mealOptions = mealOptions;
        this.dietaryPreferences = dietaryPreferences;
        this.deliveryFrequency = deliveryFrequency;
        this.ingredientQuality = ingredientQuality;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWeeklyCost() {
        return weeklyCost;
    }

    public void setWeeklyCost(String weeklyCost) {
        this.weeklyCost = weeklyCost;
    }

    public String getprepTime() {
        return prepTime;
    }

    public void setprepTime(String prepTime) {
        this.prepTime = prepTime;
    }

    public String getMealOptions() {
        return mealOptions;
    }

    public void setMealOptions(String mealOptions) {
        this.mealOptions = mealOptions;
    }

    public String getDietaryPreferences() {
        return dietaryPreferences;
    }

    public void setDietaryPreferences(String dietaryPreferences) {
        this.dietaryPreferences = dietaryPreferences;
    }

    public String getDeliveryFrequency() {
        return deliveryFrequency;
    }

    public void setDeliveryFrequency(String deliveryFrequency) {
        this.deliveryFrequency = deliveryFrequency;
    }

    public String getIngredientQuality() {
        return ingredientQuality;
    }

    public void setIngredientQuality(String ingredientQuality) {
        this.ingredientQuality = ingredientQuality;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "MealKitServiceData{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", weeklyCost='" + weeklyCost + '\'' +
                ", mealOptions='" + mealOptions + '\'' +
                ", prepTime='" + prepTime + '\'' +
                ", dietaryPreferences='" + dietaryPreferences + '\'' +
                ", deliveryFrequency='" + deliveryFrequency + '\'' +
                ", ingredientQuality='" + ingredientQuality + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
