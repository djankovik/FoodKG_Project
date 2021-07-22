package com.dushica.projects.foodkgproject.service;

import com.dushica.projects.foodkgproject.model.foodkg.Recipe;
import com.dushica.projects.foodkgproject.model.foodkg.datavis.*;

import java.util.List;
import java.util.Map;

public interface FoodKGService {
    Map<String,Integer> getIngredientUsageCount();
    Map<String, Float> getIngredientGrams();
    Map<String, Integer> getIngredientUsageInRecipes();

    List<CategoryQuantityCount> getIngredientUsageGramsAndRecipesPaged(int pageSize, int page);
    List<CategoryQuantityCount> getIngredientUsageGramsAndRecipes();
    List<CategoryCount> getWebsiteRecipeCount();
    List<CategoryCount> getRecipesCategorizedCount();
    List<CategoryCount> getPredicateCount();
    List<CategoryCount> getRdfTypeCount();
    List<CategoryCount> getIngredientFoodCategoryCount();
    List<CategoryQuantity> getMeasurementUnitCount();

    //Food KG browsing/searching
    //Food KG search
    List<Recipe> getRecipesWhichContainFoodItem(String foodItem); //this also searches through the ingredients
    Integer getNumberOfRecipesWhichContainFoodItem(String foodItem); //this also searches through the ingredients
    Integer getNumberOfIngredientsWhichContainFoodItem(String foodItem);

    List<Recipe> searchRecipeByKeyword(String keyword); //this looks for the keyword only in the recipe name
    List<String> searchIngredientByKeyword(String keyword);

    List<Recipe> getAllRecipes();
    List<String> getAllIngredients();

    List<CountryCount> getRecipesPerCountryCuisine();
    List<CategoryCount> getMealsOfTheDayCount();
}
