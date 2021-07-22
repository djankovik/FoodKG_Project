package com.dushica.projects.foodkgproject.service.impl;

import com.dushica.projects.foodkgproject.bootstrap.DataHolder;
import com.dushica.projects.foodkgproject.model.foodkg.Recipe;
import com.dushica.projects.foodkgproject.model.foodkg.datavis.*;
import com.dushica.projects.foodkgproject.repository.FoodKGRepository;
import com.dushica.projects.foodkgproject.service.FoodKGService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FoodKGServiceImpl implements FoodKGService {
    private final FoodKGRepository foodRepository;

    public FoodKGServiceImpl(FoodKGRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    @Override
    public Map<String, Integer> getIngredientUsageCount() {
        return foodRepository.getIngredientUsageCount();
    }

    @Override
    public Map<String, Float> getIngredientGrams() {
        return  foodRepository.getIngredientUnitQuantity();
    }

    @Override
    public Map<String, Integer> getIngredientUsageInRecipes() {
        return foodRepository.getIngredientUsageInRecipes();
    }

    @Override
    public List<CategoryQuantityCount> getIngredientUsageGramsAndRecipesPaged(int pageSize,int page) {
        Map<String, Float> grams = getIngredientGrams();
        Map<String,Integer> count = getIngredientUsageInRecipes();
        List<CategoryQuantityCount> stats = new ArrayList<>(0);

        for(String key:count.keySet()){
            String label = key.replaceAll("^\\S*\\/","").replaceAll(">","").replaceAll("%20"," ");
            stats.add(new CategoryQuantityCount(label,count.get(key),grams.get(key)));
        }
        Collections.sort(stats, Collections.reverseOrder());

        return stats.subList(page*pageSize,(page+1)*pageSize<stats.size()?(page+1)*pageSize:stats.size()-1);
    }

    @Override
    public List<CategoryQuantityCount> getIngredientUsageGramsAndRecipes() {
        Map<String, Float> grams = getIngredientGrams();
        Map<String,Integer> count = getIngredientUsageInRecipes();
        List<CategoryQuantityCount> stats = new ArrayList<>(0);

        for(String key:count.keySet()){
            String label = key.replaceAll("^\\S*\\/","").replaceAll(">","").replaceAll("%20"," ");
            stats.add(new CategoryQuantityCount(label,count.get(key),grams.get(key)));
        }
        stats.sort(Collections.reverseOrder());

        return stats;
    }

    @Override
    public List<CategoryCount> getWebsiteRecipeCount() {
        return foodRepository.getWebsiteRecipeCount();
    }

    @Override
    public List<CategoryCount> getRecipesCategorizedCount() {
        return foodRepository.getRecipesCategorizedCount();
    }

    @Override
    public List<CategoryCount> getPredicateCount() {
        return foodRepository.getPredicateCount();
    }

    @Override
    public List<CategoryCount> getRdfTypeCount() {
        return foodRepository.getRdfTypeCount();
    }

    @Override
    public List<CategoryCount> getIngredientFoodCategoryCount() {
        return foodRepository.getIngredientFoodCategoryCount();
    }

    @Override
    public List<CategoryQuantity> getMeasurementUnitCount() {
        return foodRepository.getMeasurementUnitCount();
    }

    @Override
    public List<Recipe> getRecipesWhichContainFoodItem(String foodItem) {
        return foodRepository.getRecipesWhichContainFoodItem(foodItem);
    }

    @Override
    public Integer getNumberOfRecipesWhichContainFoodItem(String foodItem) {
        return foodRepository.getNumberOfRecipesWhichContainFoodItem(foodItem);
    }

    @Override
    public Integer getNumberOfIngredientsWhichContainFoodItem(String foodItem) {
        return foodRepository.getNumberOfIngredientsWhichContainFoodItem(foodItem);
    }

    @Override
    public List<Recipe> searchRecipeByKeyword(String keyword) {
        return foodRepository.searchRecipeByKeyword(keyword);
    }

    @Override
    public List<String> searchIngredientByKeyword(String keyword) {
        return foodRepository.searchIngredientByKeyword(keyword);
    }

    @Override
    public List<Recipe> getAllRecipes() {
        return DataHolder.recipes;
        //return foodRepository.getAllRecipes();
    }

    @Override
    public List<String> getAllIngredients() {
        return foodRepository.getAllIngredients();
    }

    @Override
    public List<CountryCount> getRecipesPerCountryCuisine() {
        return foodRepository.getRecipeCountPerCountry();
    }

    @Override
    public List<CategoryCount> getMealsOfTheDayCount() {
        return foodRepository.getMealsOfTheDayCount();
    }

}
