package com.strh.mealmate.model;

import java.util.List;


public class Meal {
    private String id;
    private String name;
    private String description;
    private String serving;
    private List<String> ingredients;
    private String recipe;
    private String imageUrl;

    // Empty constructor required for Firestore
    public Meal() {}

    // Constructor for quick use (name + image)
    public Meal(String id, String name,String serving, String imageUrl) {
        this.id = id;
        this.name = name;
        this.serving = serving;
        this.imageUrl = imageUrl;
    }

    // Getters and setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getServing() { return serving; }
    public void setServing(String serving) { this.serving = serving; }

    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }

    public String getRecipe() { return recipe; }
    public void setRecipe(String recipe) { this.recipe = recipe; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}

