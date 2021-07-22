package com.dushica.projects.foodkgproject.model.foodkg.datavis;

import lombok.Data;

@Data
public class IngredientStats implements Comparable<IngredientStats> {
    String ingredientUrl;
    String ingredientName;
    Integer timesUsedInRecipes;
    Float kilogramsUsedInRecipes;

    public IngredientStats(String ingredientUrl, String ingredientName, Integer timesUsedInRecipes, Float kilogramsUsedInRecipes) {
        this.ingredientUrl = ingredientUrl;
        this.ingredientName = ingredientName;
        this.timesUsedInRecipes = timesUsedInRecipes;
        this.kilogramsUsedInRecipes = kilogramsUsedInRecipes/1000.0f;
    }

    @Override
    public int compareTo(IngredientStats o) {
        if(this.ingredientUrl.compareTo(o.ingredientUrl) == 0) return 0;
        return this.timesUsedInRecipes.compareTo(o.timesUsedInRecipes);
    }
}
