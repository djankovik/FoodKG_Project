package com.dushica.projects.foodkgproject.bootstrap;

import com.dushica.projects.foodkgproject.model.foodkg.Recipe;
import com.dushica.projects.foodkgproject.repository.FoodKGRepository;
import org.springframework.stereotype.Component;
import lombok.Getter;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
@Getter
public class DataHolder {
    public static final List<Recipe> recipes = new ArrayList<>();


    public final FoodKGRepository foodKGRepository;

    public DataHolder(FoodKGRepository foodKGRepository) {
        this.foodKGRepository = foodKGRepository;
    }


    @PostConstruct
    public void init() {
        List<Recipe> r= foodKGRepository.getAllRecipes();
        recipes.addAll(r);
    }
}

