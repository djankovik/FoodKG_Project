package com.dushica.projects.foodkgproject.model.foodkg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Recipe {
    String label;
    List<IngredientUse> ingredientUses;

    public Recipe(String label){
        this.label = label;
        this.ingredientUses = new ArrayList<>();
    }

    public void addIngredientUse(IngredientUse iu){
        this.ingredientUses.add(iu);
    }
}
