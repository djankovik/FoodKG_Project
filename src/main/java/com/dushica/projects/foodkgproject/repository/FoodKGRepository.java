package com.dushica.projects.foodkgproject.repository;

import com.dushica.projects.foodkgproject.model.foodkg.Recipe;
import com.dushica.projects.foodkgproject.model.foodkg.datavis.CategoryCount;
import com.dushica.projects.foodkgproject.model.foodkg.datavis.CategoryQuantity;
import com.dushica.projects.foodkgproject.model.foodkg.datavis.CategoryQuantityCount;
import com.dushica.projects.foodkgproject.model.foodkg.datavis.CountryCount;

import java.util.List;
import java.util.Map;

public interface FoodKGRepository {
    //Food KG Data Vis
    Map<String, Float> getIngredientUnitQuantity();
    Map<String, Integer> getIngredientUsageInRecipes();
    Map<String,Integer> getIngredientUsageCount();
    List<CategoryCount> getWebsiteRecipeCount();
    List<CategoryCount> getRecipesCategorizedCount();
    List<CategoryCount> getPredicateCount();
    List<CategoryCount> getRdfTypeCount();
    List<CategoryCount> getIngredientFoodCategoryCount();
    List<CategoryQuantity> getMeasurementUnitCount();
    List<CountryCount> getRecipeCountPerCountry();
    List<CategoryCount> getMealsOfTheDayCount();

    //Food KG Data Model
    List<Recipe> getRecipesWhichContainFoodItem(String foodItem); //this also searches through the ingredients
    Integer getNumberOfRecipesWhichContainFoodItem(String foodItem); //this also searches through the ingredients
    Integer getNumberOfIngredientsWhichContainFoodItem(String foodItem);

    List<Recipe> searchRecipeByKeyword(String keyword); //this looks for the keyword only in the recipe name
    List<String> searchIngredientByKeyword(String keyword);

    List<Recipe> getAllRecipes();
    List<String> getAllIngredients();
}
