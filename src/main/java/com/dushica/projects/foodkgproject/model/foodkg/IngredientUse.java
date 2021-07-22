package com.dushica.projects.foodkgproject.model.foodkg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class IngredientUse {
    String ingredient_name;
    String ing_quantity;
    String ing_unit;
}
