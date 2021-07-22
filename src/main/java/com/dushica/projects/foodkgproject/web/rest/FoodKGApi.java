package com.dushica.projects.foodkgproject.web.rest;

import com.dushica.projects.foodkgproject.model.foodkg.Recipe;
import com.dushica.projects.foodkgproject.model.foodkg.datavis.*;
import com.dushica.projects.foodkgproject.service.FoodKGService;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping(path = "/api/foodkg", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
public class FoodKGApi {
    private final FoodKGService foodKGService;

    public FoodKGApi(FoodKGService foodService) {
        this.foodKGService = foodService;
    }

    //DATA VIS
    @GetMapping("/ingredientUsageCount")
    public Map<String,Integer> getIngredientUsageCount() {
        return foodKGService.getIngredientUsageCount();
    }

    @GetMapping("/ingredientUsedGrams")
    public Map<String,Float> getIngredientUsedGrams() {
        return foodKGService.getIngredientGrams();
    }

    @GetMapping("/ingredientCategoryQuantityCountPaged")
    public List<CategoryQuantityCount> getIngredientStatisticsPaged(@RequestHeader(name = "page", defaultValue = "0", required = false) int page,
                                                               @RequestHeader(name = "page-size", defaultValue = "100", required = false) int size) {
        return foodKGService.getIngredientUsageGramsAndRecipesPaged(size,page);
    }

    @GetMapping("/ingredientCategoryQuantityCount")
    public List<CategoryQuantityCount> getIngredientStatistics() {
        return foodKGService.getIngredientUsageGramsAndRecipes();
    }

    @GetMapping("/websiteRecipeCount")
    public List<CategoryCount> getWebsiteRecipeCount() {
        return foodKGService.getWebsiteRecipeCount();
    }

    @GetMapping("/recipesPerCategory")
    public List<CategoryCount> getRecipesCategorizedCount() {
        return foodKGService.getRecipesCategorizedCount();
    }

    @GetMapping("/predicateCount")
    public List<CategoryCount> getPredicateCount() {
        return foodKGService.getPredicateCount();
    }

    @GetMapping("/rdfTypesCount")
    public List<CategoryCount> getRDFTypeCount() {
        return foodKGService.getRdfTypeCount();
    }


    @GetMapping("/ingredientFoodCategoryCount")
    public List<CategoryCount> getIngredientFoodCategoryCount() {
        return foodKGService.getIngredientFoodCategoryCount();
    }

    @GetMapping("/ingredientMeasurementUnitQuantity")
    public List<CategoryQuantity> getMeasurementUnitCount() {
        return foodKGService.getMeasurementUnitCount();
    }

    @GetMapping("/recipesPerCountryCuisine")
    public List<CountryCount> getRecipesPerCountryCuisine() {
        return foodKGService.getRecipesPerCountryCuisine();
    }

    @GetMapping("/mealRecipeCount")
    public List<CategoryCount> getMealRecipeCount() {
        return foodKGService.getMealsOfTheDayCount();
    }


    //Search/Browse FOOD-KG
    @GetMapping(path = "/recipesWhichContain", params = "foodItem")
    public List<Recipe> getRecipesWhichContainFoodItem(@RequestParam String foodItem) {
        return foodKGService.getRecipesWhichContainFoodItem(foodItem);
    }

    @GetMapping(path = "/numberOfRecipesWhichContain", params = "foodItem")
    public Integer getNumberOfRecipesWhichContainFoodItem(@QueryParam("foodItem") String foodItem) {
        return foodKGService.getNumberOfRecipesWhichContainFoodItem(foodItem);
    }

    @GetMapping(path = "/numberOfIngredientsWhichContain", params = "foodItem")
    public Integer getNumberOfIngredientsWhichContainFoodItem(@QueryParam("foodItem") String foodItem) {
        return foodKGService.getNumberOfIngredientsWhichContainFoodItem(foodItem);
    }

    @GetMapping(path = "/searchRecipes", params = "keyword")
    public List<Recipe> searchRecipeByKeyword(@RequestParam String keyword) {
        return foodKGService.searchRecipeByKeyword(keyword);
    }

    @GetMapping(path = "/searchIngredients", params = "keyword")
    public List<String> searchIngredientByKeyword(@RequestParam String keyword) {
        return foodKGService.searchIngredientByKeyword(keyword);
    }

    @GetMapping("/allRecipesPaged")
    public List<Recipe> getAllRecipesPaged(@RequestHeader(name = "page", defaultValue = "0", required = false) int page,
                                      @RequestHeader(name = "page-size", defaultValue = "100", required = false) int size) {
        List<Recipe> results = foodKGService.getAllRecipes();
        return results.subList(page,(page+1)*size < results.size()?(page+1)*size:results.size());
    }

    @GetMapping("/allIngredientsPaged")
    public List<String> getAllIngredientsPaged(@RequestHeader(name = "page", defaultValue = "0", required = false) int page,
                                          @RequestHeader(name = "page-size", defaultValue = "100", required = false) int size) {
        List<String> results = foodKGService.getAllIngredients();
        return results.subList(page,(page+1)*size < results.size()?(page+1)*size:results.size());
    }
    @GetMapping("/allRecipes")
    public List<Recipe> getAllRecipes() {
        return foodKGService.getAllRecipes();
    }

    @GetMapping("/allIngredients")
    public List<String> getAllIngredients() {
        return foodKGService.getAllIngredients();
    }

}
