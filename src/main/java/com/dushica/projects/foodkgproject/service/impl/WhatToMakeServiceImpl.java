package com.dushica.projects.foodkgproject.service.impl;

import com.dushica.projects.foodkgproject.model.wtm.FoodThingDao;
import com.dushica.projects.foodkgproject.model.wtm.Ingredient;
import com.dushica.projects.foodkgproject.model.wtm.Recipe;
import com.dushica.projects.foodkgproject.repository.WhatToMakeRepository;
import com.dushica.projects.foodkgproject.service.WhatToMakeService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WhatToMakeServiceImpl implements WhatToMakeService {
    private final WhatToMakeRepository wtmRepository;

    public WhatToMakeServiceImpl(WhatToMakeRepository wtmRepository) {
        this.wtmRepository = wtmRepository;
    }

    @Override
    public Map<String, String> getAllUrlNamePairsForEntity(String entity) {
        if(entity.equals("Ingredient") || entity.equals("Recipe") || entity.equals("Texture") || entity.equals("Flavor") ||
                entity.equals("Course") || entity.equals("Meal")){
            return wtmRepository.getAllUrlNamePairsForEntity(entity);
        }
        return new HashMap<>();
    }

    @Override
    public List<FoodThingDao> getAllDaoForEntity(String entity) {
        if(entity.equals("Ingredient") || entity.equals("Recipe") || entity.equals("Texture") || entity.equals("Flavor") ||
                entity.equals("Course") || entity.equals("Meal")){
            return wtmRepository.getAllDaoForEntity(entity);
        }
        return new ArrayList<>();
    }

    @Override
    public List<String> getAllPrettyNameForEntity(String entity) {
        if(entity.equals("Ingredient") || entity.equals("Recipe") || entity.equals("Texture") || entity.equals("Flavor") ||
                entity.equals("Course") || entity.equals("Meal")){
            return wtmRepository.getAllPrettyNameForEntity(entity);
        }
        return new ArrayList<>();
    }

    @Override
    public List<Recipe> getAllRecipes() {
        return wtmRepository.getAllRecipes();
    }

    @Override
    public List<Ingredient> getAllIngredients() {
        return wtmRepository.getAllIngredients();
    }

    @Override
    public Map<String, Recipe> getAllNamedRecipes() {
        List<Recipe> recipes = wtmRepository.getAllRecipes();
        return recipes.stream().collect(Collectors.toMap(Recipe::getName, r -> r));
    }

    @Override
    public Map<String, Ingredient> getAllNamedIngredients() {
        List<Ingredient> ingredients = wtmRepository.getAllIngredients();
        return ingredients.stream().collect(Collectors.toMap(Ingredient::getName, i -> i));
    }
}
