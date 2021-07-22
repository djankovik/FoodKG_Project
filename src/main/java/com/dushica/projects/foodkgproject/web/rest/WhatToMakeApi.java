package com.dushica.projects.foodkgproject.web.rest;

import com.dushica.projects.foodkgproject.model.wtm.FoodThingDao;
import com.dushica.projects.foodkgproject.model.wtm.Ingredient;
import com.dushica.projects.foodkgproject.model.wtm.Recipe;
import com.dushica.projects.foodkgproject.service.WhatToMakeService;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping(path = "/api/whatToMake", produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
public class WhatToMakeApi {
    private final WhatToMakeService wtmService;

    public WhatToMakeApi(WhatToMakeService wtmService) {
        this.wtmService = wtmService;
    }

    @GetMapping("/getAllRecipes")
    public List<Recipe> getAllRecipes(){
        return wtmService.getAllRecipes();
    }
    @GetMapping("/getAllIngredients")
    public List<Ingredient> getAllIngredients(){
        return wtmService.getAllIngredients();
    }

    @GetMapping("/getAllNamedRecipes")
    public Map<String,Recipe> getAllNamedRecipes(){
        return wtmService.getAllNamedRecipes();
    }
    @GetMapping("/getAllNamedIngredients")
    public Map<String,Ingredient> getAllNamedIngredients(){
        return wtmService.getAllNamedIngredients();
    }

    @GetMapping("/allUrlNamePairs/{entity}")
    public Map<String, String> getAllRecipeNames(@PathVariable String entity) {
        return wtmService.getAllUrlNamePairsForEntity(entity);
    }

    @GetMapping("/allRecipeDao/{entity}")
    public List<FoodThingDao> getAllRecipeDao(@PathVariable String entity) {
        return wtmService.getAllDaoForEntity(entity);
    }

    //    getAllPrettyNameForEntity
    @GetMapping("/getPrettyNames/{entity}")
    public List<String> getAllPrettyNameForEntity(@PathVariable String entity) {
        return wtmService.getAllPrettyNameForEntity(entity);
    }

    @GetMapping("/getRecipesWithRestrictions")
    public List<Recipe> getRecipesWithRestrictions(@RequestParam(defaultValue = "") List<String> includeMeals,
                                                   @RequestParam(defaultValue = "") List<String> includeCourses,
                                                   @RequestParam(defaultValue = "") List<String> includeFlavors,
                                                   @RequestParam(defaultValue = "") List<String> includeTextures,
                                                   @RequestParam(defaultValue = "") List<String> includeIngredients,
                                                   @RequestParam(defaultValue = "") List<String> forbidMeals,
                                                   @RequestParam(defaultValue = "") List<String> forbidCourses,
                                                   @RequestParam(defaultValue = "") List<String> forbidTextures,
                                                   @RequestParam(defaultValue = "") List<String> forbidFlavors,
                                                   @RequestParam(defaultValue = "") List<String> forbidIngredients) {
//         createdPizza = pizzaService.createPizzaWithValidation(name,description,ingredients);
        return null;
    }
}
