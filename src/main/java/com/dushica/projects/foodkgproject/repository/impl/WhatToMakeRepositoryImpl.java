package com.dushica.projects.foodkgproject.repository.impl;

import com.dushica.projects.foodkgproject.model.wtm.FoodThingDao;
import com.dushica.projects.foodkgproject.model.wtm.Ingredient;
import com.dushica.projects.foodkgproject.model.wtm.Recipe;
import com.dushica.projects.foodkgproject.repository.WhatToMakeRepository;
import com.dushica.projects.foodkgproject.repository.helper.BlazegraphUtil;
import org.openrdf.query.BindingSet;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class WhatToMakeRepositoryImpl implements WhatToMakeRepository {
//    String repoName = "what_to_make.jnl";

    LinkedList<String> queue = new LinkedList<String>(Arrays.asList("sample_wtm_copies/what_to_make_1.jnl","sample_wtm_copies/what_to_make_2.jnl","sample_wtm_copies/what_to_make_3.jnl","sample_wtm_copies/what_to_make_4.jnl","sample_wtm_copies/what_to_make_5.jnl","sample_wtm_copies/what_to_make_6.jnl"));

    @Override
    public Map<String, String> getAllUrlNamePairsForEntity(String entity) {
        String query = "select distinct ?url ?name\n" +
                "where {\n" +
                "?url rdf:type <http://purl.org/heals/food/"+entity+">;\n" +
                "rdfs:label ?name.}\n";
        List<BindingSet> results = BlazegraphUtil.processQueryForRepository(getRepoName(),query);
        Map<String,String> map = new HashMap<>();

        for(BindingSet bs:results){
            map.put(bs.getValue("url").stringValue(),bs.getValue("name").stringValue());
        }
        System.out.println(map);
        return map;
    }

    @Override
    public List<FoodThingDao> getAllDaoForEntity(String entity) {
        String query = "select distinct ?url ?name\n" +
                "where {\n" +
                "?url rdf:type <http://purl.org/heals/food/"+entity+">;\n" +
                "rdfs:label ?name.}\n";
        List<BindingSet> results = BlazegraphUtil.processQueryForRepository(getRepoName(),query);
        List<FoodThingDao> list = new ArrayList<>();

        for(BindingSet bs:results){
            list.add(new FoodThingDao(bs.getValue("url").stringValue(),bs.getValue("name").stringValue()));
        }
        System.out.println(list);
        return list;
    }

    @Override
    public List<String> getAllPrettyNameForEntity(String entity) {
        String query = "select distinct ?name\n" +
                "where {\n" +
                "?name rdf:type <http://purl.org/heals/food/"+entity+">.\n" +
                "}";
        List<BindingSet> results = BlazegraphUtil.processQueryForRepository(getRepoName(),query);
        List<String> prettyNames = new ArrayList<>();

        String name="";
        for(BindingSet bs:results){
            name = bs.getValue("name").stringValue()
                    .replaceAll("^\\S*\\/","")
                    .replaceAll(">","")
                    .replaceAll("(.)([A-Z])", "$1 $2");
            prettyNames.add(name);
        }
        System.out.println(prettyNames);
        return prettyNames;
    }

    @Override
    public List<Recipe> getAllRecipes() {
        String query = "prefix food: <http://purl.org/heals/food/>\n" +
                "select distinct ?recipe ?predicate ?object ?substitute\n" +
                "where {\n" +
                "?recipe rdf:type food:Recipe;\n" +
                "        ?predicate ?object.\n" +
                "  optional\n" +
                "  {select ?object ?substitute\n" +
                "    where {\n" +
                "      ?object rdf:type food:Ingredient;\n" +
                "              food:substitutesFor ?substitute.\n" +
                "    }}}\n" +
                "order by ?recipe ?predicate\n";
        List<BindingSet> results = BlazegraphUtil.processQueryForRepository(getRepoName(),query);

        List<Recipe> recipes = new ArrayList<>();

        Recipe recipe = null;
        String recipeUrl = "";
        String predicate = "", object = "", substitute = "";
        for(BindingSet bs:results){
            if(recipeUrl.length()>0
                    && recipe != null
                    && !recipeUrl.equals(bs.getValue("recipe").stringValue())){
                recipes.add(recipe);
                recipe = null;
            }
            if(recipe == null){
                recipeUrl = bs.getValue("recipe").stringValue();
                recipe = new Recipe(recipeUrl);
            }
            predicate = bs.getValue("predicate").stringValue();
            object = bs.getValue("object").stringValue();
            if(predicate.equals("http://purl.org/heals/food/hasIngredient")) {
                String prettyName = getPrettyNameFromString(object);
                recipe.addIngredient(prettyName);
                if(bs.getValue("substitute") != null){
                    recipe.addSubstituteForIngredient(prettyName,getPrettyNameFromString(bs.getValue("substitute").stringValue()));
                }
            } else if (predicate.equals("http://purl.org/heals/food/hasCookingTemperature")){
                recipe.setCookingTemperature(Integer.valueOf(object));
            } else if (predicate.equals("http://purl.org/heals/food/hasCookTime")){
                recipe.setCookTime(Integer.valueOf(object));
            }else if (predicate.equals("http://purl.org/heals/food/isRecommendedForCourse")){
                recipe.addCourse(getPrettyNameFromString(object));
            } else if (predicate.equals("http://purl.org/heals/food/isRecommendedForMeal")){
                recipe.addMeal(getPrettyNameFromString(object));
            } else if (predicate.equals("http://purl.org/heals/food/serves")){
                recipe.setServes(Integer.valueOf(object));
            } else if (predicate.equals("http://www.w3.org/ns/prov#wasDerivedFrom")){
                recipe.setDerivedFromUrl(object);
            } else if (predicate.equals("http://purl.org/dc/terms/source")){
                recipe.setDcSource(object);
            } else if (predicate.equals("http://www.w3.org/2000/01/rdf-schema#label")){
                recipe.setLabel(object);
            } else if (predicate.equals("http://www.w3.org/2004/02/skos/core#definition")){
                recipe.setSkosDefinition(object);
            }
            else {
                System.out.println("Err: No match for "+predicate);
            }
        }
        System.out.println(recipes);
        return recipes;
    }
    @Override
    public List<Ingredient> getAllIngredients() {
        String query = "select distinct ?ingredient ?predicate ?object\n" +
                "where {\n" +
                "?ingredient rdf:type <http://purl.org/heals/food/Ingredient>;\n" +
                "            ?predicate ?object.\n" +
                "}\n" +
                "order by ?ingredient";
        List<BindingSet> results = BlazegraphUtil.processQueryForRepository(getRepoName(),query);
        List<Ingredient> ingredients = new ArrayList<>();

        Ingredient ingredient = null;
        String ingredientUrl = "";
        String predicate = "", object = "";
        for(BindingSet bs:results){
            if(ingredientUrl.length()>0
                    && ingredient != null
                    && !ingredientUrl.equals(bs.getValue("ingredient").stringValue())){
                ingredients.add(ingredient);
                ingredient = null;
            }
            if(ingredient == null){
                ingredientUrl = bs.getValue("ingredient").stringValue();
                ingredient = new Ingredient(ingredientUrl);
            }

            predicate = bs.getValue("predicate").stringValue();
            object = bs.getValue("object").stringValue();
            if (predicate.equals("http://purl.org/heals/food/hasGluten")){
                ingredient.setHasGluten(Boolean.parseBoolean(object));
            } else if (predicate.equals("http://purl.org/heals/food/hasGlycemicIndex")){
                if(object.replaceAll("[^0-9]","").length()>0) {
                    ingredient.setGlycemicIndex(Integer.valueOf(object));
                }
            } else if (predicate.equals("http://purl.org/heals/food/hasTexture")){
                ingredient.addTexture(getPrettyNameFromString(object));
            } else if (predicate.equals("http://purl.org/heals/food/hasFlavor")){
                ingredient.addFlavor(getPrettyNameFromString(object));
            } else if (predicate.equals("http://purl.org/heals/food/substitutesFor")){
                ingredient.addCanSubstituteFor(getPrettyNameFromString(object));
            } else if (predicate.equals("http://purl.org/dc/terms/source")){
                ingredient.setDcSource(object);
            } else if (predicate.equals("http://www.w3.org/2000/01/rdf-schema#label")){
                ingredient.setLabel(object);
            } else if (predicate.equals("http://www.w3.org/2004/02/skos/core#definition")){
                ingredient.setSkosDefinition(object);
            }
            else {
                System.out.println("Err: No match for "+predicate);
            }
        }
        System.out.println(ingredients);
        return ingredients;
    }

    public static String getPrettyNameFromString(String object){
        System.out.println(object);
        String result = object.replaceAll("^\\S*\\/","")
                .replaceAll(">","")
                .replaceAll("(.)([A-Z])", "$1 $2");
        System.out.println(result);
        return result;
    }

    public String getRepoName(){
        String lastRepo = this.queue.removeLast();
        this.queue.addFirst(lastRepo);
        return lastRepo;
    }
}
