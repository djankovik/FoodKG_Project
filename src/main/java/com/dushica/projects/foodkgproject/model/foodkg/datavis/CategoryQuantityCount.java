package com.dushica.projects.foodkgproject.model.foodkg.datavis;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CategoryQuantityCount implements Comparable<CategoryQuantityCount> {
    String category;
    Integer count;
    Float quantity;

    @Override
    public int compareTo(CategoryQuantityCount o) {
        return this.count.compareTo(o.count) != 0?this.count.compareTo(o.count):this.quantity.compareTo(o.quantity);
    }
}
