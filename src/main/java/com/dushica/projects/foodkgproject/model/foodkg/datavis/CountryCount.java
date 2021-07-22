package com.dushica.projects.foodkgproject.model.foodkg.datavis;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CountryCount {
    String id;
    String name;
    Integer value;

    public CountryCount incrementCount(int count) {
        this.value = this.value + count;
        return this;
    }
}
