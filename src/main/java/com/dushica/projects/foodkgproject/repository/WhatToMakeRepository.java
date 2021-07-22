package com.dushica.projects.foodkgproject.repository;

import com.dushica.projects.foodkgproject.model.wtm.FoodThingDao;
import com.dushica.projects.foodkgproject.model.wtm.Ingredient;
import com.dushica.projects.foodkgproject.model.wtm.Recipe;

import java.util.List;
import java.util.Map;

public interface WhatToMakeRepository {
    Map<String,String> getAllUrlNamePairsForEntity(String entity); // Entry<Url,Label>
    List<FoodThingDao> getAllDaoForEntity(String entity);
    List<String> getAllPrettyNameForEntity(String entity);
    List<Recipe> getAllRecipes();
    List<Ingredient> getAllIngredients();
}
