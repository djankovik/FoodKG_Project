package com.dushica.projects.foodkgproject.service;

import com.dushica.projects.foodkgproject.model.wtm.FoodThingDao;
import com.dushica.projects.foodkgproject.model.wtm.Ingredient;
import com.dushica.projects.foodkgproject.model.wtm.Recipe;

import java.util.List;
import java.util.Map;

public interface WhatToMakeService {
    Map<String,String> getAllUrlNamePairsForEntity(String entity); // Entry<Url,Label>
    List<FoodThingDao> getAllDaoForEntity(String entity);
    List<String> getAllPrettyNameForEntity(String entity);
    List<Recipe> getAllRecipes();
    List<Ingredient> getAllIngredients();
    Map<String,Recipe> getAllNamedRecipes();
    Map<String,Ingredient> getAllNamedIngredients();
}
