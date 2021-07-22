package com.dushica.projects.foodkgproject.model.wtm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Recipe {
    String url;
    String name;
    Set<String> ingredients; //list of label names for ingredients
    Map<String, Set<String>> ingredientSubstitutes; //ingredient name mapped to list of substitutes for that ingredient
    Set<String> recommendedForCourses; //list of label names for courses
    Set<String> recommendedForMeals; //list of label names for meals
    Integer cookTime;
    Integer cookingTemperature;
    Integer serves;
    String dcSource;
    String label;
    String skosDefinition;
    String derivedFromUrl;

    public Recipe(String uri){
        this.url = uri;
        this.name = uri.replaceAll("^\\S*\\/","")
                .replaceAll(">","")
                .replaceAll("(.)([A-Z])", "$1 $2");
        this.ingredients = new  HashSet<>();
        this.ingredientSubstitutes = new HashMap<>();
        this.recommendedForCourses = new HashSet<>();
        this.recommendedForMeals = new HashSet<>();
    }

    public void addIngredient(String ingredient){
        this.ingredients.add(ingredient);
    }

    public void addCourse(String course){
        this.recommendedForCourses.add(course);
    }
    public void addMeal(String meal){
        this.recommendedForMeals.add(meal);
    }

    public void addSubstituteForIngredient(String ingredient, String substitute){
        this.ingredientSubstitutes.computeIfAbsent(ingredient, k -> new HashSet<String>());
        if(this.ingredientSubstitutes.containsKey(ingredient)){
            this.ingredientSubstitutes.get(ingredient).add(substitute);
        }
    }
    public String getName(){
       return this.name == null ? this.url.replaceAll("^\\S*\\/","")
                .replaceAll(">","")
                .replaceAll("(.)([A-Z])", "$1 $2") : name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recipe that = (Recipe) o;
        return url.equals(that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}
