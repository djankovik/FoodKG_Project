package com.dushica.projects.foodkgproject.repository.impl;
import com.dushica.projects.foodkgproject.model.foodkg.IngredientUse;
import com.dushica.projects.foodkgproject.model.foodkg.Recipe;
import com.dushica.projects.foodkgproject.model.foodkg.datavis.CategoryCount;
import com.dushica.projects.foodkgproject.model.foodkg.datavis.CategoryQuantity;
import com.dushica.projects.foodkgproject.model.foodkg.datavis.CategoryQuantityCount;
import com.dushica.projects.foodkgproject.model.foodkg.datavis.CountryCount;
import com.dushica.projects.foodkgproject.repository.FoodKGRepository;
import com.dushica.projects.foodkgproject.repository.helper.BlazegraphUtil;
import com.dushica.projects.foodkgproject.repository.helper.DataFormat;
import org.openrdf.query.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class FoodKGRepositoryImpl implements FoodKGRepository {
    String repoName = "full_foodkg.jnl";

    LinkedList<String> queue = new LinkedList<String>(Arrays.asList("simple_foodkg_copies/sample_1.jnl","simple_foodkg_copies/sample_2.jnl","simple_foodkg_copies/sample_3.jnl","simple_foodkg_copies/sample_4.jnl","simple_foodkg_copies/sample_5.jnl","simple_foodkg_copies/sample_6.jnl"));

    @Override
    public Map<String, Float> getIngredientUnitQuantity() {
        String query = "prefix recipe-kb: <http://idea.rpi.edu/heals/kb/>\n" +
                "select distinct ?ing_name ?label ?ing_unit ?ing_quantity\n" +
                "where {\n" +
                "?ingredient rdf:type recipe-kb:ingredientuse;\n" +
                "            recipe-kb:ing_name ?ing_name;\n" +
                "            recipe-kb:ing_unit ?ing_unit;\n" +
                "            recipe-kb:ing_quantity ?ing_quantity.\n" +
                "?ing_name rdfs:label ?label.\n" +
                "  filter(strlen(?label) >  0)\n" +
                "}order by ?ing_name";

        List<BindingSet> results = BlazegraphUtil.processQueryForRepository(getRepoName(),query);
        Map<String,Float> map = new HashMap<>();

        String currentIngr;
        for(BindingSet bs:results){
            currentIngr = bs.getValue("ing_name").stringValue();
            map.computeIfPresent(currentIngr, (k,v) -> v+DataFormat.getGramsForInput(bs.getValue("ing_unit").stringValue(),bs.getValue("ing_quantity").stringValue()));
            map.computeIfAbsent(currentIngr, k -> DataFormat.getGramsForInput(bs.getValue("ing_unit").stringValue(),bs.getValue("ing_quantity").stringValue()));
        }
        return map;
    }

    @Override
    public Map<String, Integer> getIngredientUsageInRecipes() {
        String query = "prefix recipe-kb: <http://idea.rpi.edu/heals/kb/>\n" +
                "select distinct ?ing_name (count(?recipe) as ?recipe_count)\n" +
                "where {\n" +
                "?recipe rdf:type recipe-kb:recipe;\n" +
                "        recipe-kb:uses ?ingredient.\n" +
                "?ingredient rdf:type recipe-kb:ingredientuse;\n" +
                "            recipe-kb:ing_name ?ing_name.\n" +
                "?ing_name rdfs:label ?label.\n" +
                "filter(strlen(?label) >  0)}\n" +
                "group by ?ing_name\n" +
                "order by desc(?recipe_count)";

        List<BindingSet> results = BlazegraphUtil.processQueryForRepository(getRepoName(),query);
        Map<String,Integer> map = new HashMap<>();

        String currentIngr;
        for(BindingSet bs:results){
            map.put(bs.getValue("ing_name").stringValue(),Integer.valueOf(bs.getValue("recipe_count").stringValue()));
        }
        return map;
    }

    @Override
    public Map<String, Integer> getIngredientUsageCount() {
        String query = "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                "prefix recipe-kb: <http://idea.rpi.edu/heals/kb/>\n" +
                "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "select ?ing_label_name (count(?ingredient) as ?ingredient_uses)\n" +
                "where {\n" +
                "?recipe rdf:type recipe-kb:recipe;\n" +
                "        recipe-kb:uses ?ingredient.\n" +
                "?ingredient rdf:type recipe-kb:ingredientuse;\n" +
                "            recipe-kb:ing_name ?ing_name.\n" +
                "?ing_name rdfs:label ?ing_label_name.\n" +
                "\n" +
                "}\n" +
                "group by ?ing_label_name\n" +
                "limit 50";

        List<BindingSet> results = BlazegraphUtil.processQueryForRepository(getRepoName(),query);
        Map<String, Integer> map = new HashMap<>();

        for(BindingSet bs:results){
            map.put(bs.getValue("ing_label_name").stringValue(),Integer.valueOf(bs.getValue("ingredient_uses").stringValue()));
        }
        return map;
    }

    @Override
    public List<CategoryCount> getWebsiteRecipeCount() {
        String query = "prefix prov: <http://www.w3.org/ns/prov#>\n" +
                "select ?shorter (count(?assertion) as ?assertion_cnt)\n" +
                "where {\n" +
                "?assertion prov:wasDerivedFrom ?website.\n" +
                "bind(str(?website) as ?site).\n" +
                "bind(replace(str(?site),\"https?://(www.)?\",\"\") as ?short).\n" +
                "bind(replace(str(?short),\"\\\\.com/.*$\",\"\") as ?shorter).\n" +
                "}\n" +
                "group by ?shorter";

        List<BindingSet> results = BlazegraphUtil.processQueryForRepository(getRepoName(),query);
        List<CategoryCount> list = new ArrayList<>();
        for(BindingSet bs:results){
            list.add(new CategoryCount(bs.getValue("shorter").stringValue(),Integer.valueOf(bs.getValue("assertion_cnt").stringValue())));
    }
        return list;
    }

    @Override
    public List<CategoryCount> getRecipesCategorizedCount() {
        String query = "prefix recipe-kb: <http://idea.rpi.edu/heals/kb/>\n" +
                "select distinct ?recipeType (count(?recipe) as ?recipeCount)\n" +
                "where {\n" +
                "?recipe rdf:type recipe-kb:recipe;\n" +
                "        rdfs:label ?label.\n" +
                "bind(lcase(?label) as ?lowerLabel)\n" +
                "bind(if(contains(?lowerLabel,\"vegan\"),\"vegan\",\n" +
                "       if(contains(?lowerLabel,\"vegetarian\"),\"vegetarian\",\n" +
                "       if(contains(?lowerLabel,\"gluten free\") || contains(?lowerLabel,\"no gluten\"),\"gluten free\",\n" +
                "       if(contains(?lowerLabel,\"no sugar\") || contains(?lowerLabel,\"sugar free\") ,\"sugar free\",\n" +
                "       if(contains(?lowerLabel,\"no fat\") || contains(?lowerLabel,\"fat free\")|| contains(?lowerLabel,\"no cholesterol\") || contains(?lowerLabel, \"low fat\"),\"fat free\",\n" +
                "       if(contains(?lowerLabel,\"no dairy\") || contains(?lowerLabel,\"dairy free\")|| contains(?lowerLabel,\"lactose free\")|| contains(?lowerLabel,\"no lactose\"),\"dairy free\",\n" +
                "       if(contains(?lowerLabel,\"paleo\"),\"paleo\",if(contains(?lowerLabel,\"keto\"),\"keto\",if(contains(?lowerLabel,\"atkins\"),\"atkins\",if(contains(?lowerLabel,\"hcg\"),\"hcg\",\n" +
                "       if(contains(?lowerLabel,\"health\"),\"healthy\",if(contains(?lowerLabel,\"raw\"),\"raw\",if(contains(?lowerLabel,\"fresh\"),\"fresh\",if(contains(?lowerLabel,\"diet\"),\"diet\",if(contains(?lowerLabel,\"homemade\"),\"homemade\",\n" +
                "       if(contains(?lowerLabel,\"no egg\") || contains(?lowerLabel,\"eggs free\")|| contains(?lowerLabel,\"eggless\") || contains(?lowerLabel,\"egg free\"),\"eggs free\",\n" +
                "       if(contains(?lowerLabel,\"nut\") || contains(?lowerLabel,\"almon\")|| contains(?lowerLabel,\"pecan\")|| contains(?lowerLabel,\"granola\") || contains(?lowerLabel,\"pistachio\"),\"has nuts\",\n" +
                "       if(contains(?lowerLabel,\"egg\") || contains(?lowerLabel,\"omele\"),\"egg based\",if(contains(?lowerLabel,\"mushroom\"),\"mushroom based\",\n" +
                "       if(contains(?lowerLabel,\"tofu\")||contains(?lowerLabel,\"tofy\"),\"tofu\",if(contains(?lowerLabel,\"easy\") || contains(?lowerLabel,\"quick\")|| contains(?lowerLabel,\"easi\")|| contains(?lowerLabel,\"lazy\") ||contains(?lowerLabel,\"fast\"),\"easy, quick/fast\",\n" +
                "       if(contains(?lowerLabel,\"slow\"),\"slow\",if(contains(?lowerLabel,\"exotic\"),\"exotic\",                            \n" +
                "       if(contains(?lowerLabel,\"bbq\") || contains(?lowerLabel,\"barbe\") || contains(?lowerLabel,\"smoked\") || contains(?lowerLabel,\"grill\"),\"grilled\",\n" +
                "       if(contains(?lowerLabel,\"pork\")|| contains(?lowerLabel,\"patty\")||contains(?lowerLabel,\"prosci\")||contains(?lowerLabel,\"schnitz\")|| contains(?lowerLabel,\"cutlet\")|| contains(?lowerLabel,\"rib\")|| contains(?lowerLabel,\"pig\")|| contains(?lowerLabel,\"lamb\")|| contains(?lowerLabel,\"duck\")|| contains(?lowerLabel,\"rabbit\")|| contains(?lowerLabel,\"bunny\")|| contains(?lowerLabel,\"sausag\")|| contains(?lowerLabel,\"kebab\")|| contains(?lowerLabel,\"wings\")|| contains(?lowerLabel,\"burger\")|| contains(?lowerLabel,\"hot dog\") || contains(?lowerLabel,\"steak\") || contains(?lowerLabel,\"meat\") || contains(?lowerLabel,\"beef\") || contains(?lowerLabel,\"chicken\") || contains(?lowerLabel,\"bacon\") || contains(?lowerLabel,\"ham\") || contains(?lowerLabel,\"turkey\"),\"has pork,beef,chicken,ham,turkey,rabbit,duck,lamb\",\n" +
                "       if(contains(?lowerLabel,\"fish\")||contains(?lowerLabel,\"mussels\")||contains(?lowerLabel,\"sushi\")||contains(?lowerLabel,\"sea bass\")|| contains(?lowerLabel,\"caviar\")|| contains(?lowerLabel,\"oyster\") || contains(?lowerLabel,\"salmon\") || contains(?lowerLabel,\"cod\") || contains(?lowerLabel,\"scallops\")|| contains(?lowerLabel,\"clam\") || contains(?lowerLabel,\"seafood\") || contains(?lowerLabel,\"crab\") || contains(?lowerLabel,\"tuna\")|| contains(?lowerLabel,\"shrimp\")|| contains(?lowerLabel,\"lobster\"),\"seafood\",\n" +
                "       if(contains(?lowerLabel,\"vegetabl\") || contains(?lowerLabel,\"cucumber\")||contains(?lowerLabel,\"chickpea\")||contains(?lowerLabel,\"brussel\")||contains(?lowerLabel,\"radish\")||contains(?lowerLabel,\"croutons\") || contains(?lowerLabel,\"bean\") || contains(?lowerLabel,\"peas\") || contains(?lowerLabel,\"yam\")|| contains(?lowerLabel,\"corn\")|| contains(?lowerLabel,\"lentils\") || contains(?lowerLabel,\"beet\")||contains(?lowerLabel,\"green\")||contains(?lowerLabel,\"artichoke\")||contains(?lowerLabel,\"spinach\")||contains(?lowerLabel,\"celery\")|| contains(?lowerLabel,\"asparagus\")|| contains(?lowerLabel,\"lettuc\")|| contains(?lowerLabel,\"avoca\")|| contains(?lowerLabel,\"guac\")|| contains(?lowerLabel,\"cabbage\")|| contains(?lowerLabel,\"pepper\")|| contains(?lowerLabel,\"cauliflower\")|| contains(?lowerLabel,\"tomato\")|| contains(?lowerLabel,\"broccoli\") || contains(?lowerLabel,\"zucchin\")|| contains(?lowerLabel,\"carrot\")|| contains(?lowerLabel,\"cucumber\"),\"vegetable based\",\n" +
                "       if(contains(?lowerLabel,\"chees\")||contains(?lowerLabel,\"cheddar\")||contains(?lowerLabel,\"parm\"),\"cheesy\",\n" +
                "       if(contains(?lowerLabel,\"choco\")||contains(?lowerLabel,\"strudel\")||contains(?lowerLabel,\"cheer\")||contains(?lowerLabel,\"snicker\")||contains(?lowerLabel,\"fudge\")||contains(?lowerLabel,\"biscuit\")||contains(?lowerLabel,\"browni\")||contains(?lowerLabel,\"pastr\")||contains(?lowerLabel,\"cinnamon\")||contains(?lowerLabel,\"glaze\")||contains(?lowerLabel,\"brulee\")||contains(?lowerLabel,\"candy\")||contains(?lowerLabel,\"praline\")||contains(?lowerLabel,\"parfai\")|| contains(?lowerLabel,\"toffee\")|| contains(?lowerLabel,\"bars\")|| contains(?lowerLabel,\"marshm\")|| contains(?lowerLabel,\"sorbet\")|| contains(?lowerLabel,\"truff\")|| contains(?lowerLabel,\"jell\")|| contains(?lowerLabel,\"jam\")||contains(?lowerLabel,\"cobbler\")||contains(?lowerLabel,\"crep\")||contains(?lowerLabel,\"milkshake\") || contains(?lowerLabel,\"pie\")|| contains(?lowerLabel,\"dessert\")|| contains(?lowerLabel,\"milk\")|| contains(?lowerLabel,\"cream\")|| contains(?lowerLabel,\"biscuits\")|| contains(?lowerLabel,\"pudding\")|| contains(?lowerLabel,\"muffi\")|| contains(?lowerLabel,\"waffl\")|| contains(?lowerLabel,\"frosting\")|| contains(?lowerLabel,\"cake\") || contains(?lowerLabel,\"sugar\")|| contains(?lowerLabel,\"donut\")|| contains(?lowerLabel,\"ice cream\")|| contains(?lowerLabel,\"pancake\")|| contains(?lowerLabel,\"cookie\")|| contains(?lowerLabel,\"honey\") || contains(?lowerLabel,\"sweet\") || contains(?lowerLabel,\"caramel\"),\"sweet dessert foods\",\n" +
                "       if(contains(?lowerLabel,\"apple\")||contains(?lowerLabel,\"fig\")||contains(?lowerLabel,\"pinea\")||contains(?lowerLabel,\"jam\")||contains(?lowerLabel,\"marmalade\")||contains(?lowerLabel,\"cherry\")||contains(?lowerLabel,\"peach\")||contains(?lowerLabel,\"watermelon\")||contains(?lowerLabel,\"banan\")||contains(?lowerLabel,\"grape\")||contains(?lowerLabel,\"pumpk\")||contains(?lowerLabel,\"tropic\")||contains(?lowerLabel,\"lemo\")||contains(?lowerLabel,\"berry\")||contains(?lowerLabel,\"pear\")||contains(?lowerLabel,\"orange\")||contains(?lowerLabel,\"fruit\"),\"fruit based\",\n" +
                "       if(contains(?lowerLabel,\"butte\")|| contains(?lowerLabel,\"fried\")|| contains(?lowerLabel,\"oil\")|| contains(?lowerLabel,\"greas\"),\"buttery/oily\",\n" +
                "       if(contains(?lowerLabel,\"tort\")||contains(?lowerLabel,\"panini\")||contains(?lowerLabel,\"wrap\")||contains(?lowerLabel,\"bur\")||contains(?lowerLabel,\"casserole\")||contains(?lowerLabel,\"fettuc\")||contains(?lowerLabel,\"ramen\")||contains(?lowerLabel,\"taco\")||contains(?lowerLabel,\"quesadillas\")||contains(?lowerLabel,\"dough\")||contains(?lowerLabel,\"toast\")||contains(?lowerLabel,\"falafel\")||contains(?lowerLabel,\"dumpling\")||contains(?lowerLabel,\"tria\")||contains(?lowerLabel,\"squa\")||contains(?lowerLabel,\"bagels\")||contains(?lowerLabel,\"pasta\")||contains(?lowerLabel,\"loaf\")||contains(?lowerLabel,\"lasagn\")||contains(?lowerLabel,\"ravioli\")||contains(?lowerLabel,\"italian\")||contains(?lowerLabel,\"macaroni\")||contains(?lowerLabel,\"spaghetti\")||contains(?lowerLabel,\"bread\")||contains(?lowerLabel,\"risott\")||contains(?lowerLabel,\"pizz\")||contains(?lowerLabel,\"sandw\")||contains(?lowerLabel,\"bun\")||contains(?lowerLabel,\"rice\"),\"pasta/bread/rice\",\n" +
                "       if(contains(?lowerLabel,\"sauce\")||contains(?lowerLabel,\"dip\")|| contains(?lowerLabel,\"gravy\")||contains(?lowerLabel,\"dress\"),\"dip/sauce/dressing\",\n" +
                "       if(contains(?lowerLabel,\"salad\"),\"salad\",if(contains(?lowerLabel,\"soup\") ||contains(?lowerLabel,\"goulash\")|| contains(?lowerLabel,\"pho\")|| contains(?lowerLabel,\"stew\") || contains(?lowerLabel,\"pot\"),\"soup,pho,stew\",\n" +
                "       if(contains(?lowerLabel,\"potat\")||contains(?lowerLabel,\"hashbro\")||contains(?lowerLabel,\"fries\")||contains(?lowerLabel,\"chips\"),\"starch dominant\",\n" +
                "       if(contains(?lowerLabel,\"garlic\")||contains(?lowerLabel,\"chil\")||contains(?lowerLabel,\"jalap\")||contains(?lowerLabel,\"salsa\") || contains(?lowerLabel,\"onion\")|| contains(?lowerLabel,\"curry\") || contains(?lowerLabel,\"chili\")||contains(?lowerLabel,\"spic\"),\"garlic/onion/chili/spicy\",\n" +
                "       if(contains(?lowerLabel,\"tea\")||contains(?lowerLabel,\"hot coc\")||contains(?lowerLabel,\"cafe\")||contains(?lowerLabel,\"capucc\")||contains(?lowerLabel,\"frap\")||contains(?lowerLabel,\"cappucc\")||contains(?lowerLabel,\"juic\")||contains(?lowerLabel,\"lemonade\")|| contains(?lowerLabel,\"water\") || contains(?lowerLabel,\"smoothie\") || contains(?lowerLabel,\"espresso\") || contains(?lowerLabel,\"coffee\") || contains(?lowerLabel,\"latte\"),\"tea/coffee/smoothie/drink\",\n" +
                "       if(contains(?lowerLabel,\"drink\")||contains(?lowerLabel,\"bourbon\")||contains(?lowerLabel,\"rum\")||contains(?lowerLabel,\"shot\")||contains(?lowerLabel,\"sangria\")||contains(?lowerLabel,\"gin\")||contains(?lowerLabel,\"bloody mary\") ||contains(?lowerLabel,\"kamikaz\")||contains(?lowerLabel,\"marg\")||contains(?lowerLabel,\"punch\") || contains(?lowerLabel,\"wine\")|| contains(?lowerLabel,\"martini\")|| contains(?lowerLabel,\"colada\") || contains(?lowerLabel,\"cocktail\")||contains(?lowerLabel,\"alcohol\"),\"contains alcohol\",\"other\")\n" +
                "       )))))))))))))))))))))))))))))))))))))) as ?recipeType)\n" +
                "#filter(?recipeType = \"else\")\n" +
                "}\n" +
                "group by ?recipeType\n"
//                +"order by desc(?recipeCount)"
                ;

        List<BindingSet> results = BlazegraphUtil.processQueryForRepository(getRepoName(),query);
        List<CategoryCount> list = new ArrayList<>();

        for(BindingSet bs:results){
            list.add(new CategoryCount(bs.getValue("recipeType").stringValue(),Integer.valueOf(bs.getValue("recipeCount").stringValue())));
        }
        return list;
    }

    @Override
    public List<CategoryCount> getPredicateCount() {
        String query = "select distinct ?predicate (count(?o) as ?cnt_usages)\n" +
                "where {\n" +
                "  ?s ?predicate ?o\n" +
                "}\n" +
                "group by ?predicate\n" +
                "order by desc(?cnt_usages)";

        List<BindingSet> results = BlazegraphUtil.processQueryForRepository(getRepoName(),query);
        List<CategoryCount> list = new ArrayList<>();

        for(BindingSet bs:results){
            list.add(new CategoryCount(bs.getValue("predicate").stringValue(),Integer.valueOf(bs.getValue("cnt_usages").stringValue())));
        }
        return list;
    }

    @Override
    public List<CategoryCount> getRdfTypeCount() {
        String query = "select distinct ?type (count(?type) as ?cnt_types)\n" +
                "where {\n" +
                "  ?s rdf:type ?type\n" +
                "}\n" +
                "group by ?type\n" +
                "order by desc(?cnt_types)";

        List<BindingSet> results = BlazegraphUtil.processQueryForRepository(getRepoName(),query);
        List<CategoryCount> list = new ArrayList<>();

        for(BindingSet bs:results){
            list.add(new CategoryCount(bs.getValue("type").stringValue(),Integer.valueOf(bs.getValue("cnt_types").stringValue())));
        }
        return list;
    }

    @Override
    public List<CategoryCount> getIngredientFoodCategoryCount() {
        String query = "prefix recipe-kb: <http://idea.rpi.edu/heals/kb/>\n" +
                "select distinct ?category (count(?label) as ?ingredient_count)\n" +
                "where {\n" +
                "?ingredient rdf:type recipe-kb:ingredientuse;\n" +
                "            recipe-kb:ing_name ?ing_name.\n" +
                "?ing_name rdfs:label ?l.\n" +
                "bind(lcase(?l) as ?label)\n" +
                "bind(if(contains(?label,\"tortilla\")||contains(?label,\"shel\")||contains(?label,\"sandwi\")||contains(?label,\"cous\")||contains(?label,\"batte\")||contains(?label,\"fusil\")||contains(?label,\"dumpli\")||contains(?label,\"penne\")||contains(?label,\"fettuc\")||contains(?label,\"grits\")||contains(?label,\"shee\")||contains(?label,\"pretz\")||contains(?label,\"lasagn\")||contains(?label,\"bague\")||contains(?label,\"roll\")||contains(?label,\"falaf\")||contains(?label,\"gluten\")||contains(?label,\"crouton\")||contains(?label,\"quin\")||contains(?label,\"bagel\")||contains(?label,\"croiss\")||contains(?label,\"popcorn\")||contains(?label,\"crack\")||contains(?label,\"toast\")||contains(?label,\"waffl\")||contains(?label,\"flake\")||contains(?label,\"wheat\")||contains(?label,\"bun\")||contains(?label,\"strudel\")||contains(?label,\"rye\")||contains(?label,\"green\")||contains(?label,\"mac\")||contains(?label,\"pizz\")||contains(?label,\"pastr\")||contains(?label,\"linguin\")||contains(?label,\"bran\")||contains(?label,\"oat\")||contains(?label,\"barley\")||contains(?label,\"crepe\")||contains(?label,\"cookie\")||contains(?label,\"biscuit\")||contains(?label,\"dough\")||contains(?label,\"cereal\")||contains(?label,\"asparagus\")||contains(?label,\"artichoke\")||contains(?label,\"pie\")||contains(?label,\"crust\")||contains(?label,\"flour\")||contains(?label,\"grain\")||contains(?label,\"granol\")||contains(?label,\"noodl\")||contains(?label,\"bread\")||contains(?label,\"pasta\")||contains(?label,\"spagh\")||contains(?label,\"rice\"),\"breads, cereals, rice, pasta\",\n" +
                "if(contains(?label,\"vegetable\")||contains(?label,\"tomat\")||contains(?label,\"wild\")||contains(?label,\"rocket\")||contains(?label,\"fresh\")||contains(?label,\"bamb\")||contains(?label,\"zucc\")||contains(?label,\"pars\")||contains(?label,\"kim\")||contains(?label,\"ajvar\")||contains(?label,\"stem\")||contains(?label,\"weed\")||contains(?label,\"yam\")||contains(?label,\"rad\")||contains(?label,\"chives\")||contains(?label,\"arugula\")||contains(?label,\"hummus\")||contains(?label,\"salsa\")||contains(?label,\"yeast\")||contains(?label,\"cucum\")||contains(?label,\"squas\")||contains(?label,\"dill\")||contains(?label,\"lettuce\")||contains(?label,\"salad\")||contains(?label,\"turnip\")||contains(?label,\"lent\")||contains(?label,\"chil\")||contains(?label,\"basil\")||contains(?label,\"pest\")||contains(?label,\"pickle\")||contains(?label,\"vegan\")||contains(?label,\"leaf\")||contains(?label,\"fungus\")||contains(?label,\"celery\")||contains(?label,\"cauliflower\")||contains(?label,\"corn\")||contains(?label,\"root\")||contains(?label,\"onion\")||contains(?label,\"garlic\")||contains(?label,\"leaves\")||contains(?label,\"dri\")||contains(?label,\"cabbage\")||contains(?label,\"truffle\")||contains(?label,\"pea\")||contains(?label,\"tomato\")||contains(?label,\"beet\")||contains(?label,\"broc\")||contains(?label,\"jalape\")||contains(?label,\"pepper\")||contains(?label,\"mushroom\")||contains(?label,\"bean\")||contains(?label,\"spina\")||contains(?label,\"carrot\")||contains(?label,\"potato\")||contains(?label,\"legume\")||contains(?label,\"seed\")||contains(?label,\"sprout\")||contains(?label,\"avocad\")||contains(?label,\"guac\"),\"vegetables, legumes\",\n" +
                "if(contains(?label,\"fruit\")||contains(?label,\"fresh\")||contains(?label,\"papay\")||contains(?label,\"lych\")||contains(?label,\"cit\")||contains(?label,\"mango\")||contains(?label,\"rais\")||contains(?label,\"kiwi\")||contains(?label,\"olive\")||contains(?label,\"calamari\")||contains(?label,\"fig\")||contains(?label,\"melon\")||contains(?label,\"cantaloupe\")||contains(?label,\"acai\")||contains(?label,\"plum\")||contains(?label,\"pulp\")||contains(?label,\"cocoa\")||contains(?label,\"orange\")||contains(?label,\"grape\")||contains(?label,\"lemon\")||contains(?label,\"pumpkin\")||contains(?label,\"apricot\")||contains(?label,\"jam\")||contains(?label,\"cherr\")||contains(?label,\"lim\")||contains(?label,\"berr\")||contains(?label,\"apple\")||contains(?label,\"banana\")||contains(?label,\"pear\"),\"fruit\",\n" +
                "if(contains(?label,\"milk\")||contains(?label,\"moz\")||contains(?label,\"pudding\")||contains(?label,\"cream\")||contains(?label,\"cheese\")||contains(?label,\"yog\")||contains(?label,\"gauda\")||contains(?label,\"parmesan\")||contains(?label,\"cheddar\"),\"milk, yoghurt, cheese and/or alternatives\",\n" +
                "if(contains(?label,\"meat\")||contains(?label,\"sea\")||contains(?label,\"rabbit\")||contains(?label,\"veal\")||contains(?label,\"gel\")||contains(?label,\"goos\")||contains(?label,\"eleph\")||contains(?label,\"tofu\")||contains(?label,\"buf\")||contains(?label,\"sard\")||contains(?label,\"deer\")||contains(?label,\"skin\")||contains(?label,\"wing\")||contains(?label,\"hen\")||contains(?label,\"caviar\")||contains(?label,\"clam\")||contains(?label,\"anchov\")||contains(?label,\"sausage\")||contains(?label,\"hot dog\")||contains(?label,\"bone\")||contains(?label,\"leg\")||contains(?label,\"loin\")||contains(?label,\"squid\")||contains(?label,\"salam\")||contains(?label,\"octo\")||contains(?label,\"calf\")||contains(?label,\"liver\")||contains(?label,\"abalone\")||contains(?label,\"shrimp\")||contains(?label,\"oyster\")||contains(?label,\"crab\")||contains(?label,\"snail\")||contains(?label,\"lamb\")||contains(?label,\"duck\")||contains(?label,\"breast\")||contains(?label,\"fillet\")||contains(?label,\"fish\")||contains(?label,\"steak\")||contains(?label,\"turkey\")||contains(?label,\"pork\")||contains(?label,\"pig\")||contains(?label,\"roast\")||contains(?label,\"rib\")||contains(?label,\"tuna\")||contains(?label,\"salmon\")||contains(?label,\"cod\")||contains(?label,\"sea bass\")||contains(?label,\"ham\")||contains(?label,\"chicken\")||contains(?label,\"poultr\")||contains(?label,\"egg\")||contains(?label,\"bacon\")||contains(?label,\"beef\")||contains(?label,\"nut\")||contains(?label,\"almond\")||contains(?label,\"pecan\")||contains(?label,\"cashew\")||contains(?label,\"adobo\")||contains(?label,\"acorn\")||contains(?label,\"soy\")||contains(?label,\"pistachio\"),\"lean meat, fish, poultry, eggs, nuts\",\n" +
                "if(contains(?label,\"water\")||contains(?label,\"jag\")||contains(?label,\"peps\")||contains(?label,\"whisk\")||contains(?label,\"spri\")||contains(?label,\"guin\")||contains(?label,\"espr\")||contains(?label,\"muss\")||contains(?label,\"ice\")||contains(?label,\"rum\")||contains(?label,\"sherry\")||contains(?label,\"bourbon\")||contains(?label,\"cocktail\")||contains(?label,\"amaret\")||contains(?label,\"broth\")||contains(?label,\"mary\")||contains(?label,\"nectar\")||contains(?label,\"liq\")||contains(?label,\"tea\")||contains(?label,\"vodka\")||contains(?label,\"bevera\")||contains(?label,\"cider\")||contains(?label,\"coke\")||contains(?label,\"cola\")||contains(?label,\"gin\")||contains(?label,\"juice\")||contains(?label,\"soup\")||contains(?label,\"pho\")||contains(?label,\"beer\")||contains(?label,\"teq\")||contains(?label,\"coff\")||contains(?label,\"alcohol\")||contains(?label,\"soda\")||contains(?label,\"ale\")||contains(?label,\"wine\")||contains(?label,\"vinegar\"),\"liquids\",\n" +
                "if(contains(?label,\"fat\")||contains(?label,\"red\")||contains(?label,\"white\")||contains(?label,\"bla\")||contains(?label,\"fond\")||contains(?label,\"van\")||contains(?label,\"tza\")||contains(?label,\"rose\")||contains(?label,\"sage\")||contains(?label,\"papr\")||contains(?label,\"min\")||contains(?label,\"kit kat\")||contains(?label,\"hersh\")||contains(?label,\"dip\")||contains(?label,\"bar\")||contains(?label,\"dress\")||contains(?label,\"ranch\")||contains(?label,\"gum\")||contains(?label,\"condim\")||contains(?label,\"groun\")||contains(?label,\"gravy\")||contains(?label,\"oreo\")||contains(?label,\"herb\")||contains(?label,\"color\")||contains(?label,\"cay\")||contains(?label,\"orregano\")||contains(?label,\"icing\")||contains(?label,\"sweet\")||contains(?label,\"marshm\")||contains(?label,\"coriander\")||contains(?label,\"fudge\")||contains(?label,\"glaze\")||contains(?label,\"cayene\")||contains(?label,\"cinnamon\")||contains(?label,\"honey\")||contains(?label,\"twink\")||contains(?label,\"can\")||contains(?label,\"powder\")||contains(?label,\"froz\")||contains(?label,\"season\")||contains(?label,\"mix\")||contains(?label,\"oil\")||contains(?label,\"creme\")||contains(?label,\"whip\")||contains(?label,\"dry\")||contains(?label,\"dried\")||contains(?label,\"sauce\")||contains(?label,\"mayo\")||contains(?label,\"ketch\")||contains(?label,\"mustard\")||contains(?label,\"paste\")||contains(?label,\"cake\")||contains(?label,\"brown\")||contains(?label,\"tea\")||contains(?label,\"margarin\")||contains(?label,\"chip\")||contains(?label,\"conce\")||contains(?label,\"edibl\")||contains(?label,\"butter\")||contains(?label,\"muff\")||contains(?label,\"sprink\")||contains(?label,\"sugar\")||contains(?label,\"jel\")||contains(?label,\"syrup\")||contains(?label,\"choco\")||contains(?label,\"caramel\")||contains(?label,\"spice\")||contains(?label,\"salt\")||contains(?label,\"oregano\")||contains(?label,\"cumin\")||contains(?label,\"curry\")||contains(?label,\"cand\"),\"condiments, spices, sugars, fats and oils\",\"other\"\n" +
                "))))))) as ?category)\n" +
                "}\n" +
                "group by ?category\n" +
                "order by ?ingredient_count";

        List<BindingSet> results = BlazegraphUtil.processQueryForRepository(getRepoName(),query);
        List<CategoryCount> list = new ArrayList<>();

        for(BindingSet bs:results){
            list.add(new CategoryCount(bs.getValue("category").stringValue(),Integer.valueOf(bs.getValue("ingredient_count").stringValue())));
        }
        return list;
    }

    @Override
    public List<CategoryQuantity> getMeasurementUnitCount() {
        String query = "prefix recipe-kb: <http://idea.rpi.edu/heals/kb/>\n" +
                "select distinct ?ing_unit ?ing_quantity\n" +
                "where {\n" +
                "?ingredient rdf:type recipe-kb:ingredientuse;\n" +
                "            recipe-kb:ing_quantity ?ing_quantity ;\n" +
                "        \trecipe-kb:ing_unit ?ing_unit .\n" +
                "}";

        List<BindingSet> results = BlazegraphUtil.processQueryForRepository(getRepoName(),query);
        Map<String,Float> map = new HashMap<>();
        for(String s:DataFormat.unitGramsMap.keySet()){
            map.put(s,0.0f);
        }
        List<CategoryQuantity> list = new ArrayList<>();

        for(BindingSet bs:results){
            String unit = DataFormat.getStandardizedUnit(bs.getValue("ing_unit").stringValue());
            Float quantity = DataFormat.getQuantity(bs.getValue("ing_quantity").stringValue());
            map.computeIfPresent(unit, (k,v)->v+quantity);
        }
        map.keySet().stream().forEach(x->list.add(new CategoryQuantity(x,map.get(x))));
        return list;
    }

    @Override
    public List<CountryCount> getRecipeCountPerCountry() {
        String query = "prefix recipe-kb: <http://idea.rpi.edu/heals/kb/>\n" +
                "select distinct ?recipeType (count(?recipe) as ?recipeCount)\n" +
                "where {\n" +
                "?recipe rdf:type recipe-kb:recipe;\n" +
                "        rdfs:label ?label.\n" +
                "bind(lcase(?label) as ?lowerLabel)\n" +
                "bind(\n" +
                "if(contains(?lowerLabel,\"kazakhstan\")||contains(?lowerLabel,\"kuyrdak\")||contains(?lowerLabel,\"shurpa\")||contains(?lowerLabel,\"horse meat\")||contains(?lowerLabel,\"baursak\")||contains(?lowerLabel,\"besbarmak\"),\"KZ,Kazakhstan\",\n" +
                "if(contains(?lowerLabel,\"shashlik\")||contains(?lowerLabel,\"joshpara\")||contains(?lowerLabel,\"mantije\")||contains(?lowerLabel,\"dimlama\")||contains(?lowerLabel,\"lahnman\"),\"Uzbekistan\",\n" +
                "if(contains(?lowerLabel,\"kabuli palaw\")||contains(?lowerLabel,\"sheer khurma\")||contains(?lowerLabel,\"stuffing\")||contains(?lowerLabel,\"bolani\")||contains(?lowerLabel,\"aushak\")||contains(?lowerLabel,\"lamb and mutton\"),\"Afghanistan\",\n" +
                "if(contains(?lowerLabel,\"dum pukht\")||contains(?lowerLabel,\"biryani\")||contains(?lowerLabel,\"haleem\"),\"Pakistan\",\n" +
                "if(contains(?lowerLabel,\"tajik\")||contains(?lowerLabel,\"zaliony\")||contains(?lowerLabel,\"beshbarmak\")||contains(?lowerLabel,\"lagman\")||contains(?lowerLabel,\"plov\"),\"Tajikistan\",\n" +
                "if(contains(?lowerLabel,\"iraq\")||contains(?lowerLabel,\"quzi\")||contains(?lowerLabel,\"masgouf\")||contains(?lowerLabel,\"tashreeb\")||contains(?lowerLabel,\"timmam ou keema\")||contains(?lowerLabel,\"tashreeb\")||contains(?lowerLabel,\"bamia\"),\"Iraq\",\n" +
                "if(contains(?lowerLabel,\"tea leaf\")||contains(?lowerLabel,\"mohinga\")||contains(?lowerLabel,\"nangyi thoke\")||contains(?lowerLabel,\"tea shop meal\")||contains(?lowerLabel,\"mohinga\")||contains(?lowerLabel,\"shan-style\"),\"Myanmar\",\n" +
                "if(contains(?lowerLabel,\"namibia\")||contains(?lowerLabel,\"melktart\")||contains(?lowerLabel,\"konditoreien\")||contains(?lowerLabel,\"mealie pap\")||contains(?lowerLabel,\"oshiwambo\")||contains(?lowerLabel,\"oshifima\"),\"NA,Namibia\",\n" +
                "if(contains(?lowerLabel,\"nshima\")||contains(?lowerLabel,\"lumanda\")||contains(?lowerLabel,\"katapa\")||contains(?lowerLabel,\"inswa\")||contains(?lowerLabel,\"ifinkubala\")||contains(?lowerLabel,\"special thali\"),\"ZM,Zambia\",\n" +
                "if(contains(?lowerLabel,\"sadza\")||contains(?lowerLabel,\"mapopo candy\")||contains(?lowerLabel,\"dovi\")||contains(?lowerLabel,\"bota\")||contains(?lowerLabel,\"whawha\")||contains(?lowerLabel,\"game meat\")||contains(?lowerLabel,\"nhedzi\"),\"ZW,Zimbabwe\",\n" +
                "if(contains(?lowerLabel,\"dolma\")||contains(?lowerLabel,\"mechou\")||contains(?lowerLabel,\"bagita\")||contains(?lowerLabel,\"jwaz\")||contains(?lowerLabel,\"mechoui\"),\"DZ,Algeria\",\n" +
                "if(contains(?lowerLabel,\"ugali\")||contains(?lowerLabel,\"kenya\")||contains(?lowerLabel,\"cocnut rice\")||contains(?lowerLabel,\"githeri\")||contains(?lowerLabel,\"mushy peas\"),\"KE,Kenya\",\n" +
                "if(contains(?lowerLabel,\"maafe\")||contains(?lowerLabel,\"boulettes de poisson\")||contains(?lowerLabel,\"confiture de papaye\")||contains(?lowerLabel,\"ceebu jen\")||contains(?lowerLabel,\"poulet yassa\")||contains(?lowerLabel,\"senegal\"),\"SN,Senegal\",\n" +
                "if(contains(?lowerLabel,\"sudan\")||contains(?lowerLabel,\"gourrassa\")||contains(?lowerLabel,\"umfitit\")||contains(?lowerLabel,\"aseeda\")||contains(?lowerLabel,\"dura\")||contains(?lowerLabel,\"kisra\")||contains(?lowerLabel,\"moukhbaza\")||contains(?lowerLabel,\"kuindiong\"),\"SD,Sudan\",\n" +
                "if(contains(?lowerLabel,\"daraba\")||contains(?lowerLabel,\"la bouille\")||contains(?lowerLabel,\"kisser\")||contains(?lowerLabel,\"jarret\"),\"TD,Chad\",\n" +
                "if(contains(?lowerLabel,\"laos\")||contains(?lowerLabel,\"nam khao\")||contains(?lowerLabel,\"khao piak sen\")||contains(?lowerLabel,\"pate\")||contains(?lowerLabel,\"steamed fish\")||contains(?lowerLabel,\"green papaya\")||contains(?lowerLabel,\"larb\")||contains(?lowerLabel,\"glutinous rice\"),\"LA,Laos\",\n" +
                "if(contains(?lowerLabel,\"guinea\")||contains(?lowerLabel,\"yams\")||contains(?lowerLabel,\"kaukau\")||contains(?lowerLabel,\"taro\")||contains(?lowerLabel,\"root crop\"),\"PG,Papua New Guinea\",\n" +
                "if(contains(?lowerLabel,\"madagascar\")||contains(?lowerLabel,\"foza sy\")||contains(?lowerLabel,\"malagasy\")||contains(?lowerLabel,\"lasary\")||contains(?lowerLabel,\"koba\")||contains(?lowerLabel,\"plateau de fruits de mer\")||contains(?lowerLabel,\"romazava\"),\"Madagascar\",\n" +
                "if(contains(?lowerLabel,\"pastelitos de carne\")||contains(?lowerLabel,\"hondura\")||contains(?lowerLabel,\"yojoa\")||contains(?lowerLabel,\"sopa de caracol\")||contains(?lowerLabel,\"sopa de frijoles\")||contains(?lowerLabel,\"pupusa\")||contains(?lowerLabel,\"machuca\")||contains(?lowerLabel,\"horchata\")||contains(?lowerLabel,\"enchilada\")||contains(?lowerLabel,\"baleadas\")||contains(?lowerLabel,\"carne asada\"),\"HN,Honduras\",\n" +
                "if(contains(?lowerLabel,\"chorizo\")||contains(?lowerLabel,\"ticucos\")||contains(?lowerLabel,\"pollo\")||contains(?lowerLabel,\"pisques\")||contains(?lowerLabel,\"de elote\")||contains(?lowerLabel,\"loroco\")||contains(?lowerLabel,\"izote\"),\"SV,El Salvador\",\n" +
                "if(contains(?lowerLabel,\"sanchocho\")||contains(?lowerLabel,\"panam\")||contains(?lowerLabel,\"carne guisada\")||contains(?lowerLabel,\"chicheme\")||contains(?lowerLabel,\"carimanolas\")||contains(?lowerLabel,\"tostones\")||contains(?lowerLabel,\"ropa vieja\"),\"PA,Panama\",\n" +
                "if(contains(?lowerLabel,\"gallo pinto\")||contains(?lowerLabel,\"chifrijo\")||contains(?lowerLabel,\"olla de carne\")||contains(?lowerLabel,\"arroz con\")||contains(?lowerLabel,\"casado\")||contains(?lowerLabel,\"sopa negra\"),\"CR,Costa Rica\",\n" +
                "if(contains(?lowerLabel,\"enceboll\")||contains(?lowerLabel,\"caldo\")||contains(?lowerLabel,\"cuy\")||contains(?lowerLabel,\"churrasco\")||contains(?lowerLabel,\"pizza americ\")||contains(?lowerLabel,\"secos\")||contains(?lowerLabel,\"corviche\"),\"EC,Ecuador\",\n" +
                "if(contains(?lowerLabel,\"paraguay\")||contains(?lowerLabel,\"dulce de leche\")||contains(?lowerLabel,\"kosereva\")||contains(?lowerLabel,\"fish soup\")||contains(?lowerLabel,\"pira caldo\")||contains(?lowerLabel,\"lampreado\")||contains(?lowerLabel,\"kieve\")||contains(?lowerLabel,\"chipa\")||contains(?lowerLabel,\"dumplings\"),\"PY,Paraguay\",\n" +
                "if(contains(?lowerLabel,\"bolivia\")||contains(?lowerLabel,\"silpancho\")||contains(?lowerLabel,\"sonso de yuca\")||contains(?lowerLabel,\"humintas\")||contains(?lowerLabel,\"calf tongue\")||contains(?lowerLabel,\"aji de fideos\")||contains(?lowerLabel,\"anticucho\")||contains(?lowerLabel,\"sanduiche de chola\"),\"BO,Bolivia\",\n" +
                "if(contains(?lowerLabel,\"arepa\")||contains(?lowerLabel,\"criollo\")||contains(?lowerLabel,\"empanada\")||contains(?lowerLabel,\"tres leches\")||contains(?lowerLabel,\"tequenos\")||contains(?lowerLabel,\"cachapa\")||contains(?lowerLabel,\"hallaca\")||contains(?lowerLabel,\"huevos pericos\"),\"VE,Venezuela\",\n" +
                "if(contains(?lowerLabel,\"cambodia\")||contains(?lowerLabel,\"choruk\")||contains(?lowerLabel,\"kampot pepper\")||contains(?lowerLabel,\"nom bahn\")||contains(?lowerLabel,\"fish amok\")||contains(?lowerLabel,\"red tree\")||contains(?lowerLabel,\"khmer red curry\")||contains(?lowerLabel,\"bo luc lac\"),\"KH,Cambodia\",\n" +
                "if(contains(?lowerLabel,\"serbia\")||contains(?lowerLabel,\"palm\")||contains(?lowerLabel,\"white elephan\")||contains(?lowerLabel,\"maboke\")|| contains(?lowerLabel,\"schnitzel\"),\"RS,Serbia\",\n" +
                "if(contains(?lowerLabel,\"congo\")||contains(?lowerLabel,\"pili pili\")||contains(?lowerLabel,\"chikwanga\")||contains(?lowerLabel,\"moambe\"),\"CD,Congo\",\n" +
                "if(contains(?lowerLabel,\"nigeria\")||contains(?lowerLabel,\"nkwobi\")||contains(?lowerLabel,\"kosai\")||contains(?lowerLabel,\"akara\")||contains(?lowerLabel,\"afang soup\")||contains(?lowerLabel,\"fufu\")||contains(?lowerLabel,\"suya\")||contains(?lowerLabel,\"jollof rice\")||contains(?lowerLabel,\"chin chin\"),\"NG,Nigeria\",\n" +
                "if(contains(?lowerLabel,\"tanzania\")||contains(?lowerLabel,\"ugali\")||contains(?lowerLabel,\"nyama choma\")||contains(?lowerLabel,\"biryani\"),\"TZ,Tanzania\",\n" +
                "if(contains(?lowerLabel,\"yemen\")||contains(?lowerLabel,\"maraq\")||contains(?lowerLabel,\"qishr\")||contains(?lowerLabel,\"aseeda\")||contains(?lowerLabel,\"masoub\")||contains(?lowerLabel,\"hawaij\")||contains(?lowerLabel,\"areeka\")||contains(?lowerLabel,\"saltah\"),\"YE,Yemen\",\n" +
                "if(contains(?lowerLabel,\"syria\")||contains(?lowerLabel,\"shawarma\")||contains(?lowerLabel,\"fattoush\")||contains(?lowerLabel,\"kebab\")||contains(?lowerLabel,\"kibbeh\"),\"SY,Syria\",\n" +
                "if(contains(?lowerLabel,\"new zealand\")||contains(?lowerLabel,\"whitebait\")||contains(?lowerLabel,\"paeroa\")||contains(?lowerLabel,\"jaffa\")||contains(?lowerLabel,\"kiwiburger\")||contains(?lowerLabel,\"hokey pokey\")||contains(?lowerLabel,\"crayfish\")||contains(?lowerLabel,\"maori\"),\"NZ,New Zealand\",\n" +
                "if(contains(?lowerLabel,\"guatemala\")||contains(?lowerLabel,\"spiced mango\")||contains(?lowerLabel,\"pepian\")||contains(?lowerLabel,\"chiles rellenos\")||contains(?lowerLabel,\"caldo\")||contains(?lowerLabel,\"sopa\")||contains(?lowerLabel,\"papaya\")||contains(?lowerLabel,\"cardamom\"),\"GT,Guatemala\",\n" +
                "if(contains(?lowerLabel,\"cantaloup\")||contains(?lowerLabel,\"gata\")||contains(?lowerLabel,\"dolma\")||contains(?lowerLabel,\"pastirma\")||contains(?lowerLabel,\"lavash\")||contains(?lowerLabel,\"armeni\"),\"AM,Armenia\",\n" +
                "if(contains(?lowerLabel,\"mongol\")||contains(?lowerLabel,\"airag\")||contains(?lowerLabel,\"chanasan\")||contains(?lowerLabel,\"khuurga\")||contains(?lowerLabel,\"budaatai\")||contains(?lowerLabel,\"guriltai shul\")||contains(?lowerLabel,\"tsuivan\")||contains(?lowerLabel,\"boodog\")||contains(?lowerLabel,\"buuz\")||contains(?lowerLabel,\"khorkhog\"),\"MN,Mongolia\",\n" +
                "if(contains(?lowerLabel,\"indonesia\")||contains(?lowerLabel,\"sayur urap\")||contains(?lowerLabel,\"goreng\")||contains(?lowerLabel,\"nasi campur\")||contains(?lowerLabel,\"satay\")||contains(?lowerLabel,\"pig roast\")||contains(?lowerLabel,\"gado-gado\")|| contains(?lowerLabel,\"indones\"),\"ID,Indonesia\",\n" +
                "if(contains(?lowerLabel,\"sri lanka\")||contains(?lowerLabel,\"polos\")||contains(?lowerLabel,\"hoppers\")||contains(?lowerLabel,\"lamprais\")||contains(?lowerLabel,\"kukul mas\")||contains(?lowerLabel,\"chicken curry\")||contains(?lowerLabel,\"kottu\")||contains(?lowerLabel,\"malabar matthi\"),\"LK,Sri Lanka\",\n" +
                "if(contains(?lowerLabel,\"india\")||contains(?lowerLabel,\"chai\")|| contains(?lowerLabel,\"calico\")|| contains(?lowerLabel,\"calcut\")|| contains(?lowerLabel,\"bombay\")|| contains(?lowerLabel,\"mumbai\")||contains(?lowerLabel,\"porcin\")||contains(?lowerLabel,\"chutney\")|| contains(?lowerLabel,\"taro\")|| contains(?lowerLabel,\"tarka\")||contains(?lowerLabel,\"rogan josh\")||contains(?lowerLabel,\"tusca\")||contains(?lowerLabel,\"butter chicken\")||contains(?lowerLabel,\"tandoori\")||contains(?lowerLabel,\"tika masala\")||contains(?lowerLabel,\"vada pav\")||contains(?lowerLabel,\"dhokla\")||contains(?lowerLabel,\"parotta\")||contains(?lowerLabel,\"sarson\")||contains(?lowerLabel,\"XXX\")||contains(?lowerLabel,\"XXX\")||contains(?lowerLabel,\"okra\")||contains(?lowerLabel,\"curry\")||contains(?lowerLabel,\"poulet\"),\"IN,India\",\n" +
                "if(contains(?lowerLabel,\"ital\")||contains(?lowerLabel,\"mascarpone\")||contains(?lowerLabel,\"frittata\")||contains(?lowerLabel,\"focca\")||contains(?lowerLabel,\"focaccia\")||contains(?lowerLabel,\"florentine\")||contains(?lowerLabel,\"cavatappi\")||contains(?lowerLabel,\"cavatelli\")||contains(?lowerLabel,\"caprese\")||contains(?lowerLabel,\"capellini\")|| contains(?lowerLabel,\"cannell\")|| contains(?lowerLabel,\"calzon\")|| contains(?lowerLabel,\"calamari\")|| contains(?lowerLabel,\"cacio o\")|| contains(?lowerLabel,\"cacciator\")|| contains(?lowerLabel,\"bologn\")|| contains(?lowerLabel,\"bocconc\")||contains(?lowerLabel,\"manico\")||contains(?lowerLabel,\"gallo\")||contains(?lowerLabel,\"sicil\")||contains(?lowerLabel,\"raw red prawns\")||contains(?lowerLabel,\"arancini\")||contains(?lowerLabel,\"caponata\")||contains(?lowerLabel,\"pesto alla\")||contains(?lowerLabel,\"pasta alla norma\")||contains(?lowerLabel,\"beccafico\")||contains(?lowerLabel,\"roulade\")||contains(?lowerLabel,\"proschiut\")|| contains(?lowerLabel,\"alfredo\")|| contains(?lowerLabel,\"antipasto\")|| contains(?lowerLabel,\"tagli\")|| contains(?lowerLabel,\"limoncello\")|| contains(?lowerLabel,\"linguine\")|| contains(?lowerLabel,\"tortellini\")|| contains(?lowerLabel,\"raviol\")|| contains(?lowerLabel,\"tagliatel\")|| contains(?lowerLabel,\"pesche\")|| contains(?lowerLabel,\"gnocch\")|| contains(?lowerLabel,\"fusil\")||contains(?lowerLabel,\"fettuc\")||contains(?lowerLabel,\"vongol\")||contains(?lowerLabel,\"bucatini\")|| contains(?lowerLabel,\"tiramisu\")|| contains(?lowerLabel,\"spaghet\")|| contains(?lowerLabel,\"amaretto\")|| contains(?lowerLabel,\"pesto\")|| contains(?lowerLabel,\"truffl\")|| contains(?lowerLabel,\"bruschet\")|| contains(?lowerLabel,\"carbonara\")|| contains(?lowerLabel,\"polenta\")|| contains(?lowerLabel,\"fiorentina\")|| contains(?lowerLabel,\"pasta\")|| contains(?lowerLabel,\"risotto\")|| contains(?lowerLabel,\"pizza\")|| contains(?lowerLabel,\"bottarga\")|| contains(?lowerLabel,\"lasagne\")|| contains(?lowerLabel,\"lasagna\")|| contains(?lowerLabel,\"riboll\")|| contains(?lowerLabel,\"cornmeal\")|| contains(?lowerLabel,\"gorgonz\")|| contains(?lowerLabel,\"ossobuc\"),\"IT,Italy\",\n" +
                "if(contains(?lowerLabel,\"macedon\")||contains(?lowerLabel,\"sarma\")||contains(?lowerLabel,\"cabbage roll\")|| contains(?lowerLabel,\"moussaka\")|| contains(?lowerLabel,\"sarma\")|| contains(?lowerLabel,\"baked bean\")|| contains(?lowerLabel,\"ajvar\")|| contains(?lowerLabel,\"kacamak\")|| contains(?lowerLabel,\"gibanic\")|| contains(?lowerLabel,\"pastrmajl\")|| contains(?lowerLabel,\"mekici\")|| contains(?lowerLabel,\"piftija\")|| contains(?lowerLabel,\"tavc\")|| contains(?lowerLabel,\"veal stew\")|| contains(?lowerLabel,\"gjuvech\")|| contains(?lowerLabel,\"ohrid\")|| contains(?lowerLabel,\"turlitava\")|| contains(?lowerLabel,\"sauerkraut cass\")|| contains(?lowerLabel,\"podvarok\")|| contains(?lowerLabel,\"pogacha\")|| contains(?lowerLabel,\"potato stew\")|| contains(?lowerLabel,\"shirden\")|| contains(?lowerLabel,\"kukur\")|| contains(?lowerLabel,\"fermented milk\")|| contains(?lowerLabel,\"gralic sauc\")|| contains(?lowerLabel,\"turli tava\")|| contains(?lowerLabel,\"zelnik\")|| contains(?lowerLabel,\"pot baked bean\")|| contains(?lowerLabel,\"stuffed pep\"),\"MK,Macedonia\",\n" +
                "if(contains(?lowerLabel,\"tunis\")|| contains(?lowerLabel,\"shakshouka\")|| contains(?lowerLabel,\"asida\")|| contains(?lowerLabel,\"lablabi\")|| contains(?lowerLabel,\"merguez\")|| contains(?lowerLabel,\"couscous\"),\"TN,Tunisia\",\n" +
                "if(contains(?lowerLabel,\"spanish\")|| contains(?lowerLabel,\"torta\")|| contains(?lowerLabel,\"paella\")|| contains(?lowerLabel,\"patatas bravas\")|| contains(?lowerLabel,\"gazpacho\")|| contains(?lowerLabel,\"fideua\")|| contains(?lowerLabel,\"jamon\")|| contains(?lowerLabel,\"tortilla\")|| contains(?lowerLabel,\"negro\")|| contains(?lowerLabel,\"bunuelos\")||contains(?lowerLabel,\"frijoles\")|| contains(?lowerLabel,\"churro\"),\"ES,Spain\",\n" +
                "if(contains(?lowerLabel,\"vietnam\")|| contains(?lowerLabel,\"pho\")|| contains(?lowerLabel,\"cao lau\")|| contains(?lowerLabel,\"bun cha\")|| contains(?lowerLabel,\"banh\")|| contains(?lowerLabel,\"egg coffee\")|| contains(?lowerLabel,\"white rose dumplings\"),\"VN,Vietnam\",\n" +
                "if(contains(?lowerLabel,\"leban\")|| contains(?lowerLabel,\"baba g\")|| contains(?lowerLabel,\"tabbou\")|| contains(?lowerLabel,\"kibbeh\")|| contains(?lowerLabel,\"kofta\")|| contains(?lowerLabel,\"kanafeh\")|| contains(?lowerLabel,\"hummus\")|| contains(?lowerLabel,\"tabbouleh\")|| contains(?lowerLabel,\"manakish\")||contains(?lowerLabel,\"cedar\")|| contains(?lowerLabel,\"fattoush\")|| contains(?lowerLabel,\"pilaf\"),\"LB,Lebanon\",\n" +
                "if(contains(?lowerLabel,\"carribb\")||contains(?lowerLabel,\"caribb\")|| contains(?lowerLabel,\"lime chicken\")|| contains(?lowerLabel,\"yucc\")|| contains(?lowerLabel,\"trini\")|| contains(?lowerLabel,\"jerk pork\")|| contains(?lowerLabel,\"peperot\")|| contains(?lowerLabel,\"flying\")|| contains(?lowerLabel,\"cou cou\")|| contains(?lowerLabel,\"fish stew\")|| contains(?lowerLabel,\"jerk\")|| contains(?lowerLabel,\"roti\")|| contains(?lowerLabel,\"plantin\")|| contains(?lowerLabel,\"breadfruit\"),\"HT,Haiti;DO,Dominican\",\n" +
                "if(contains(?lowerLabel,\"korea\")|| contains(?lowerLabel,\"kochujang\")|| contains(?lowerLabel,\"sweet syrup\")|| contains(?lowerLabel,\"marinated beef panc\")|| contains(?lowerLabel,\"pork strip\")|| contains(?lowerLabel,\"stir-fried noodl\")|| contains(?lowerLabel,\"hoeddeok\")|| contains(?lowerLabel,\"bulgogi\")|| contains(?lowerLabel,\"samgyeopsal\")|| contains(?lowerLabel,\"japchae\")|| contains(?lowerLabel,\"kimch\")|| contains(?lowerLabel,\"dukbokki\"),\"KR,korean\",\n" +
                "if(contains(?lowerLabel,\"turkish\")|| contains(?lowerLabel,\"baklava\")|| contains(?lowerLabel,\"shish kebab\")|| contains(?lowerLabel,\"kofta\")|| contains(?lowerLabel,\"pide\")|| contains(?lowerLabel,\"baked potato\")|| contains(?lowerLabel,\"meze\")|| contains(?lowerLabel,\"kofte\")|| contains(?lowerLabel,\"sutlijash\")|| contains(?lowerLabel,\"tulumba\"),\"TR,Turkey\",\n" +
                "if(contains(?lowerLabel,\"russia\")|| contains(?lowerLabel,\"bishop\")|| contains(?lowerLabel,\"stogano\")|| contains(?lowerLabel,\"tarragon\")|| contains(?lowerLabel,\"blini\")|| contains(?lowerLabel,\"syrniki\")|| contains(?lowerLabel,\"kasha\")|| contains(?lowerLabel,\"pelmeni\")|| contains(?lowerLabel,\"pirozhki\")|| contains(?lowerLabel,\"borscht\")|| contains(?lowerLabel,\"okroshka\"),\"RU,Russia\",\n" +
                "if(contains(?lowerLabel,\"greman\")|| contains(?lowerLabel,\"sausage\")|| contains(?lowerLabel,\"wurst\")|| contains(?lowerLabel,\"potato pancake\")|| contains(?lowerLabel,\"kartoffel\")|| contains(?lowerLabel,\"raspeball\")|| contains(?lowerLabel,\"hamburg\")|| contains(?lowerLabel,\"maine\")|| contains(?lowerLabel,\"bavar\")|| contains(?lowerLabel,\"ham hock\")|| contains(?lowerLabel,\"rouladen\")|| contains(?lowerLabel,\"knorr\")|| contains(?lowerLabel,\"strudel\")||contains(?lowerLabel,\"streusel\")|| contains(?lowerLabel,\"apfel\")||contains(?lowerLabel,\"frankf\")|| contains(?lowerLabel,\"german\")|| contains(?lowerLabel,\"sauerbraten\"),\"DE,Germany\",\n" +
                "if(contains(?lowerLabel,\"morocc\") || contains(?lowerLabel,\"tagine\")|| contains(?lowerLabel,\"tangerin\")|| contains(?lowerLabel,\"spice\"),\"MA,Morocco\",\n" +
                "if(contains(?lowerLabel,\"czech\")||contains(?lowerLabel,\"macedon\")|| contains(?lowerLabel,\"fried cheese\")|| contains(?lowerLabel,\"bean soup\")|| contains(?lowerLabel,\"sauerkraut\")|| contains(?lowerLabel,\"garlic soup\")|| contains(?lowerLabel,\"tomato soup\")|| contains(?lowerLabel,\"cucumber soup\")|| contains(?lowerLabel,\"bigos\")|| contains(?lowerLabel,\"pierogi\") || contains(?lowerLabel,\"polan\")||contains(?lowerLabel,\"ukra\")||contains(?lowerLabel,\"paska\")||contains(?lowerLabel,\"borscht\")||contains(?lowerLabel,\"aspic\")||contains(?lowerLabel,\"potato pancake\")||contains(?lowerLabel,\"chicken kiev\")||contains(?lowerLabel,\"potato salad\")||contains(?lowerLabel,\"holodets\")||contains(?lowerLabel,\"vareny\")||contains(?lowerLabel,\"holubts\")|| contains(?lowerLabel,\"kielbasa\")|| contains(?lowerLabel,\"beet soup\")|| contains(?lowerLabel,\"borsch\")|| contains(?lowerLabel,\"kolac\"),\"BY,Belarus;UA,Ukraine;SK,Slovakia;SI,Slovenia;HR,Croatia\",\n" +
                "if(contains(?lowerLabel,\"fren\")||contains(?lowerLabel,\"coqui\")|| contains(?lowerLabel,\"onion soup\")|| contains(?lowerLabel,\"cassoulet\")|| contains(?lowerLabel,\"casserol\")|| contains(?lowerLabel,\"beef bourguig\")|| contains(?lowerLabel,\"confit de canard\")|| contains(?lowerLabel,\"soupe\")|| contains(?lowerLabel,\"souffle\")|| contains(?lowerLabel,\"flamiche\")|| contains(?lowerLabel,\"jambalay\")||contains(?lowerLabel,\"filet\")||contains(?lowerLabel,\"fillet\")|| contains(?lowerLabel,\"tapenad\")|| contains(?lowerLabel,\"tarta\")|| contains(?lowerLabel,\"goat chee\")|| contains(?lowerLabel,\"haricot\")|| contains(?lowerLabel,\"parfai\")||contains(?lowerLabel,\"pouss\")|| contains(?lowerLabel,\"bleu che\")|| contains(?lowerLabel,\"bouillab\")|| contains(?lowerLabel,\"creme bru\")|| contains(?lowerLabel,\"camember\")|| contains(?lowerLabel,\"saute\")||contains(?lowerLabel,\"galet\")|| contains(?lowerLabel,\"france\")|| contains(?lowerLabel,\"blue chees\")|| contains(?lowerLabel,\"boeuf\")|| contains(?lowerLabel,\"catalina\")||contains(?lowerLabel,\"cabernet\")|| contains(?lowerLabel,\"cajun\")|| contains(?lowerLabel,\"french\")||contains(?lowerLabel,\"sauvignon\")|| contains(?lowerLabel,\"brie\"),\"FR,France\",\n" +
                "if(contains(?lowerLabel,\"portug\")||contains(?lowerLabel,\"molasses\")|| contains(?lowerLabel,\"bacal\")|| contains(?lowerLabel,\"francesinha\")|| contains(?lowerLabel,\"bifanas\")|| contains(?lowerLabel,\"caldo\")|| contains(?lowerLabel,\"sardine\"),\"PT,Portugal\",\n" +
                "if(contains(?lowerLabel,\"malays\")|| contains(?lowerLabel,\"tempeh\")|| contains(?lowerLabel,\"banana bread\")|| contains(?lowerLabel,\"nasi lemak\")|| contains(?lowerLabel,\"ikan bakar\")|| contains(?lowerLabel,\"banana leaf\")|| contains(?lowerLabel,\"nasi kandar\")|| contains(?lowerLabel,\"roti canai\")|| contains(?lowerLabel,\"curry mee\")|| contains(?lowerLabel,\"laksa\"),\"MY,Malaysia\",\n" +
                "if(contains(?lowerLabel,\"cuba\")|| contains(?lowerLabel,\"medianoche\")|| contains(?lowerLabel,\"tamales\")|| contains(?lowerLabel,\"frita\")|| contains(?lowerLabel,\"rice with chicken\")|| contains(?lowerLabel,\"lechon\"),\"CU,Cuba\",\n" +
                "if(contains(?lowerLabel,\"egyp\")||contains(?lowerLabel,\"memphis\")||contains(?lowerLabel,\"molokhia\")||contains(?lowerLabel,\"medames\")||contains(?lowerLabel,\"falafel\")||contains(?lowerLabel,\"kushari\")||contains(?lowerLabel,\"squash\")||contains(?lowerLabel,\"mulukhi\"),\"EG,Egypt\",\n" +
                "if(contains(?lowerLabel,\"jamai\")|| contains(?lowerLabel,\"codfish\")|| contains(?lowerLabel,\"curry goat\")|| contains(?lowerLabel,\"ackee\")|| contains(?lowerLabel,\"rice and pea\")|| contains(?lowerLabel,\"callal\")|| contains(?lowerLabel,\"bammy\"),\"JM,Jamaica\",\n" +
                "if(contains(?lowerLabel,\"iran\")|| contains(?lowerLabel,\"caraway\")|| contains(?lowerLabel,\"teher\")|| contains(?lowerLabel,\"kebab\")||contains(?lowerLabel,\"kabab\")||contains(?lowerLabel,\"stew\")||contains(?lowerLabel,\"persia\")||contains(?lowerLabel,\"rose\")||contains(?lowerLabel,\"cateh\")||contains(?lowerLabel,\"dami\")||contains(?lowerLabel,\"chelow\")||contains(?lowerLabel,\"polow\"),\"IR,Iran\",\n" +
                "if(contains(?lowerLabel,\"taiwan\")||contains(?lowerLabel,\"bubble tea\")||contains(?lowerLabel,\"bobba tea\")||contains(?lowerLabel,\"beef noodle\")||contains(?lowerLabel,\"pineaple cake\")||contains(?lowerLabel,\"oyster omelette\")||contains(?lowerLabel,\"bing\")||contains(?lowerLabel,\"gua bao\")||contains(?lowerLabel,\"fried chicken\")||contains(?lowerLabel,\"braised pork\"),\"TW,Taiwan\",\n" +
                "if(contains(?lowerLabel,\"argent\")||contains(?lowerLabel,\"locro\")||contains(?lowerLabel,\"asado\")||contains(?lowerLabel,\"empanada\")||contains(?lowerLabel,\"chori\")||contains(?lowerLabel,\"dulce\")||contains(?lowerLabel,\"farinata\"),\"AR,Argentina\",\n" +
                "if(contains(?lowerLabel,\"chile\")||contains(?lowerLabel,\"pastel de choclo\")||contains(?lowerLabel,\"empanada\")||contains(?lowerLabel,\"cazuela\")||contains(?lowerLabel,\"maize\")||contains(?lowerLabel,\"locos\")||contains(?lowerLabel,\"reineta\")||contains(?lowerLabel,\"congrio\")||contains(?lowerLabel,\"corvina\"),\"CL,Chile\",\n" +
                "if(contains(?lowerLabel,\"singapore\")|| contains(?lowerLabel,\"chili crab\")|| contains(?lowerLabel,\"hainanese\")|| contains(?lowerLabel,\"char kway\")|| contains(?lowerLabel,\"satay\")|| contains(?lowerLabel,\"stingray\"),\"SG,Singapore\",\n" +
                "if(contains(?lowerLabel,\"georg\")|| contains(?lowerLabel,\"armen\")|| contains(?lowerLabel,\"khinkali\")|| contains(?lowerLabel,\"khachapuri\")|| contains(?lowerLabel,\"shashlik\")|| contains(?lowerLabel,\"stew\")|| contains(?lowerLabel,\"kharcho\")|| contains(?lowerLabel,\"chakapuli\"),\"GE,Georgia\",\n" +
                "if(contains(?lowerLabel,\"filipino\")|| contains(?lowerLabel,\"adobo\")|| contains(?lowerLabel,\"balut\")|| contains(?lowerLabel,\"kare-kare\")|| contains(?lowerLabel,\"kinilaw\")|| contains(?lowerLabel,\"sinigang\")|| contains(?lowerLabel,\"tapa\")|| contains(?lowerLabel,\"halo-halo\"),\"PH,Philipines\",\n" +
                "if(contains(?lowerLabel,\"arab\")|| contains(?lowerLabel,\"tilapia\")|| contains(?lowerLabel,\"mocha\")|| contains(?lowerLabel,\"hummus\")|| contains(?lowerLabel,\"manakish\")|| contains(?lowerLabel,\"halloumi\")|| contains(?lowerLabel,\"medames\")|| contains(?lowerLabel,\"falafel\")|| contains(?lowerLabel,\"tabbouleh\"),\"SA,Saudi Arabia;AE,United Arab Emirates\",\n" +
                "if(contains(?lowerLabel,\"afric\")|| contains(?lowerLabel,\"samoosa\")|| contains(?lowerLabel,\"samosa\")||contains(?lowerLabel,\"porridg\")||contains(?lowerLabel,\"meat\")||contains(?lowerLabel,\"peri\")||contains(?lowerLabel,\"jollof\")||contains(?lowerLabel,\"egusi\")||contains(?lowerLabel,\"ugali\")||contains(?lowerLabel,\"bunny chow\")|| contains(?lowerLabel,\"koeks\"),\"ZA,South Africa;CF,Central African Rep.\",\n" +
                "if(contains(?lowerLabel,\"ethio\")||contains(?lowerLabel,\"sourdough\")||contains(?lowerLabel,\"injera\")||contains(?lowerLabel,\"tibs\")||contains(?lowerLabel,\"legume stew\")||contains(?lowerLabel,\"tej\")||contains(?lowerLabel,\"kitfo\")||contains(?lowerLabel,\"spice blend\"),\"ET,Ethiopia\",\n" +
                "if(contains(?lowerLabel,\"swiss\")||contains(?lowerLabel,\"meringue\")|| contains(?lowerLabel,\"chard\")||contains(?lowerLabel,\"carrot cake\")|| contains(?lowerLabel,\"linzer\")|| contains(?lowerLabel,\"gruy\")||contains(?lowerLabel,\"socker kaka\")||contains(?lowerLabel,\"sockerkaka\")|| contains(?lowerLabel,\"fondue\")|| contains(?lowerLabel,\"rosti\")|| contains(?lowerLabel,\"muesli\")|| contains(?lowerLabel,\"raclette\")|| contains(?lowerLabel,\"bundner\")|| contains(?lowerLabel,\"risotto\")|| contains(?lowerLabel,\"zucher\"),\"CH,Switzerland\",\n" +
                "if(contains(?lowerLabel,\"brazil\")|| contains(?lowerLabel,\"tapioca\")||contains(?lowerLabel,\"feijoada\")|| contains(?lowerLabel,\"moqueca\")|| contains(?lowerLabel,\"cachaca\")|| contains(?lowerLabel,\"brigadeiro\")|| contains(?lowerLabel,\"pao de queijo\")|| contains(?lowerLabel,\"akara\"),\"BR,Brazil\",\n" +
                "if(contains(?lowerLabel,\"liby\")|| contains(?lowerLabel,\"asida\")|| contains(?lowerLabel,\"bureek\")|| contains(?lowerLabel,\"filfel\")|| contains(?lowerLabel,\"chuma\")|| contains(?lowerLabel,\"maseer\"),\"LY,Libya\",\n" +
                "if(contains(?lowerLabel,\"peru\")|| contains(?lowerLabel,\"andes\")|| contains(?lowerLabel,\"aji\")||contains(?lowerLabel,\"tacu\")|| contains(?lowerLabel,\"ceviche\")|| contains(?lowerLabel,\"lomo saltado\")|| contains(?lowerLabel,\"cuy\")|| contains(?lowerLabel,\"causa\")|| contains(?lowerLabel,\"corazon\"),\"PE,Peru\",\n" +
                "if(contains(?lowerLabel,\"hunga\")|| contains(?lowerLabel,\"goulash\")|| contains(?lowerLabel,\"fusherman's soup\")|| contains(?lowerLabel,\"cabbage roll\")|| contains(?lowerLabel,\"dobos torte\"),\"HU,Hungary\",\n" +
                "if(contains(?lowerLabel,\"japan\")|| contains(?lowerLabel,\"scallop\")||contains(?lowerLabel,\"miso\")||contains(?lowerLabel,\"furikake\")|| contains(?lowerLabel,\"tamagoy\")|| contains(?lowerLabel,\"persimm\")||contains(?lowerLabel,\"mochi\")||contains(?lowerLabel,\"soboro\")||contains(?lowerLabel,\"socoloco\")||contains(?lowerLabel,\"sushi\")||contains(?lowerLabel,\"sashimi\")||contains(?lowerLabel,\"unagi\")||contains(?lowerLabel,\"tempura\")||contains(?lowerLabel,\"soba\")||contains(?lowerLabel,\"udon\")||contains(?lowerLabel,\"onigiri\")||contains(?lowerLabel,\"yakitori\"),\"JP,Japan\",\n" +
                "if(contains(?lowerLabel,\"colomb\")|| contains(?lowerLabel,\"guava\")||contains(?lowerLabel,\"bandeja\")||contains(?lowerLabel,\"chicharr\")||contains(?lowerLabel,\"arepa\")||contains(?lowerLabel,\"ajiaco\"),\"CO,Colombia\",\n" +
                "if(contains(?lowerLabel,\"guinea\")|| contains(?lowerLabel,\"fufu\")|| contains(?lowerLabel,\"mango\")|| contains(?lowerLabel,\"plantain\")|| contains(?lowerLabel,\"fried sweet potato\")|| contains(?lowerLabel,\"tamarind\")|| contains(?lowerLabel,\"sesame cookie\")|| contains(?lowerLabel,\"pompkin pie\"),\"GN,Guinean\",\n" +
                "if(contains(?lowerLabel,\"azerb\")||contains(?lowerLabel,\"halva\")||contains(?lowerLabel,\"lavangi\")||contains(?lowerLabel,\"dolma\")||contains(?lowerLabel,\"lyulya\"),\"AZ,Azerbaijan\",\n" +
                "if(contains(?lowerLabel,\"greek\")|| contains(?lowerLabel,\"grecian\")|| contains(?lowerLabel,\"tarato\")||contains(?lowerLabel,\"taramasalata\")||contains(?lowerLabel,\"octopus\")||contains(?lowerLabel,\"olive\")||contains(?lowerLabel,\"dolmades\")||contains(?lowerLabel,\"moussaka\")||contains(?lowerLabel,\"grilled meat\")||contains(?lowerLabel,\"tzatziki\")||contains(?lowerLabel,\"gyro\")||contains(?lowerLabel,\"souflaki\"),\"GR,Greece\",\n" +
                "if(contains(?lowerLabel,\"mediter\")||contains(?lowerLabel,\"caper\")|| contains(?lowerLabel,\"bulgur\")|| contains(?lowerLabel,\"anchovy\")|| contains(?lowerLabel,\"tahini\")||contains(?lowerLabel,\"fiadone\")|| contains(?lowerLabel,\"lamb\")|| contains(?lowerLabel,\"sheep\")||contains(?lowerLabel,\"feta\")||contains(?lowerLabel,\"lentils\")||contains(?lowerLabel,\"spanak\")||contains(?lowerLabel,\"ghanoush\")||contains(?lowerLabel,\"paella\"),\"Mediterranean countries\",\n" +
                "if(contains(?lowerLabel,\"belgium\")||contains(?lowerLabel,\"pralin\")||contains(?lowerLabel,\"brussel\")||contains(?lowerLabel,\"mussl\")||contains(?lowerLabel,\"moules\")||contains(?lowerLabel,\"meatbal\")||contains(?lowerLabel,\"eel\")||contains(?lowerLabel,\"rabbit\")||contains(?lowerLabel,\"prune\")||contains(?lowerLabel,\"grey shrimp\"),\"BE,Belgium\",\n" +
                "if(contains(?lowerLabel,\"ireland\")||contains(?lowerLabel,\"irish\")||contains(?lowerLabel,\"colcannon\")||contains(?lowerLabel,\"coddle\")||contains(?lowerLabel,\"boxty\")||contains(?lowerLabel,\"bacon\")||contains(?lowerLabel,\"cabbage\"),\"IE,Ireland\",\n" +
                "if(contains(?lowerLabel,\"puerto\")|| contains(?lowerLabel,\"spanish\")|| contains(?lowerLabel,\"arroz\")||contains(?lowerLabel,\"tostones\")||contains(?lowerLabel,\"grandules\")||contains(?lowerLabel,\"alcapurria\")||contains(?lowerLabel,\"alcapurria\")||contains(?lowerLabel,\"cooking banana\")||contains(?lowerLabel,\"empanada\")||contains(?lowerLabel,\"mofongo\"),\"PR,Puerto Rico\",\n" +
                "if(contains(?lowerLabel,\"chin\")||contains(?lowerLabel,\"macau\")|| contains(?lowerLabel,\"egg roll\")|| contains(?lowerLabel,\"egg tart\")|| contains(?lowerLabel,\"mashed potato\")|| contains(?lowerLabel,\"pork\")|| contains(?lowerLabel,\"seafood rice\")|| contains(?lowerLabel,\"serradura\")||contains(?lowerLabel,\"ramen\") || contains(?lowerLabel,\"yuz\")|| contains(?lowerLabel,\"lo mein\")||contains(?lowerLabel,\"buckwheat\") ||contains(?lowerLabel,\"fennel\")|| contains(?lowerLabel,\"sweet and sour\")|| contains(?lowerLabel,\"kung pao\")|| contains(?lowerLabel,\"spring roll\")|| contains(?lowerLabel,\"mapo doufu\")|| contains(?lowerLabel,\"dumpling\")|| contains(?lowerLabel,\"wonton\")|| contains(?lowerLabel,\"fried rice\")|| contains(?lowerLabel,\"tofu\")|| contains(?lowerLabel,\"brown rice\")|| contains(?lowerLabel,\"chow mein\")|| contains(?lowerLabel,\"hoisin\")|| contains(?lowerLabel,\"bok choy\")||contains(?lowerLabel,\"shangh\")|| contains(?lowerLabel,\"steamed crab\")|| contains(?lowerLabel,\"smoked fish\")|| contains(?lowerLabel,\"peking\")|| contains(?lowerLabel,\"pork bun\")||contains(?lowerLabel,\"canton\")||contains(?lowerLabel,\"mandarin\")|| contains(?lowerLabel,\"seitan\")||contains(?lowerLabel,\"lyche\")|| contains(?lowerLabel,\"kong bao\"),\"CN,China\",\n" +
                "if(contains(?lowerLabel,\"finnish\") || contains(?lowerLabel,\"finland\")||contains(?lowerLabel,\"reindeer\")||contains(?lowerLabel,\"herring\")||contains(?lowerLabel,\"karelian\")||contains(?lowerLabel,\"cinnamon roll\")||contains(?lowerLabel,\"rye bread\")||contains(?lowerLabel,\"salty liquorice\"),\"FI,Finland\",\n" +
                "if(contains(?lowerLabel,\"england\")|| contains(?lowerLabel,\"full breakfast\")|| contains(?lowerLabel,\"sunday roast\")|| contains(?lowerLabel,\"shepherds pie\")|| contains(?lowerLabel,\"baked beans\")|| contains(?lowerLabel,\"bangers and mash\")||contains(?lowerLabel,\"trifle\")||contains(?lowerLabel,\"british\")||contains(?lowerLabel,\"cottage\")|| contains(?lowerLabel,\"blanque\")||contains(?lowerLabel,\"custard\")||contains(?lowerLabel,\"kingdom\")|| contains(?lowerLabel,\"bloody mar\")||contains(?lowerLabel,\"chowder\")||contains(?lowerLabel,\"scott\")|| contains(?lowerLabel,\"oatcak\")|| contains(?lowerLabel,\"scotch broth\")|| contains(?lowerLabel,\"haggis\")|| contains(?lowerLabel,\"colcannon\")|| contains(?lowerLabel,\"whicky\")|| contains(?lowerLabel,\"beer\")|| contains(?lowerLabel,\"scotch\")|| contains(?lowerLabel,\"shortbread\")|| contains(?lowerLabel,\"muffi\")|| contains(?lowerLabel,\"ham\")||contains(?lowerLabel,\"welsh\")|| contains(?lowerLabel,\"rarebit\")|| contains(?lowerLabel,\"cawl\")|| contains(?lowerLabel,\"laverbread\")|| contains(?lowerLabel,\"bara\")|| contains(?lowerLabel,\"glamorgan\")||contains(?lowerLabel,\"english\"),\"GB,United Kingdom\",\n" +
                "if(contains(?lowerLabel,\"mexi\")|| contains(?lowerLabel,\"santa fe\")||contains(?lowerLabel,\"mahi\")|| contains(?lowerLabel,\"chipotl\")|| contains(?lowerLabel,\"tamale\")|| contains(?lowerLabel,\"guacamol\")|| contains(?lowerLabel,\"jalapen\")|| contains(?lowerLabel,\"queso\")||contains(?lowerLabel,\"fiesta\")||contains(?lowerLabel,\"soapaipil\")||contains(?lowerLabel,\"enchilad\")||contains(?lowerLabel,\"burrit\")||contains(?lowerLabel,\"quesadil\")||contains(?lowerLabel,\"nacho\")||contains(?lowerLabel,\"taco\")||contains(?lowerLabel,\"tortil\")|| contains(?lowerLabel,\"tijuana\")|| contains(?lowerLabel,\"jicama\")|| contains(?lowerLabel,\"butternut\")|| contains(?lowerLabel,\"kahlua\"),\"MX,Mexico\",\n" +
                "if(contains(?lowerLabel,\"dutch\")|| contains(?lowerLabel,\"cole slaw\")|| contains(?lowerLabel,\"butterhorn\")|| contains(?lowerLabel,\"tangy\")|| contains(?lowerLabel,\"danish\")|| contains(?lowerLabel,\"ligonberry\")|| contains(?lowerLabel,\"holland\")||contains(?lowerLabel,\"foie gras\")||contains(?lowerLabel,\"fromage\")||contains(?lowerLabel,\"migonette\")|| contains(?lowerLabel,\"french fr\")|| contains(?lowerLabel,\"croquette\")|| contains(?lowerLabel,\"kerring\")|| contains(?lowerLabel,\"soused\")|| contains(?lowerLabel,\"patat\")|| contains(?lowerLabel,\"bitterball\")|| contains(?lowerLabel,\"liquorice\"),\"NL,Netherlands\",\n" +
                "if(contains(?lowerLabel,\"european\")||contains(?lowerLabel,\"pound cake\")|| contains(?lowerLabel,\"red cabbage\")|| contains(?lowerLabel,\"raisin bread\") || contains(?lowerLabel,\"western\"),\"European\",\n" +
                "if(contains(?lowerLabel,\"scandinav\")|| contains(?lowerLabel,\"bour\")|| contains(?lowerLabel,\"kohlrabi\")|| contains(?lowerLabel,\"haddock\")|| contains(?lowerLabel,\"blueberr\")|| contains(?lowerLabel,\"gravlax\")|| contains(?lowerLabel,\"flank steak\"),\"NO,Norway;SE,Sweden;IS,Iceland\",\n" +
                "if(contains(?lowerLabel,\"canad\")|| contains(?lowerLabel,\"caravel\")|| contains(?lowerLabel,\"peanut butter\")|| contains(?lowerLabel,\"peanutbutter\")|| contains(?lowerLabel,\"lentil\")|| contains(?lowerLabel,\"maple\"),\"CA,Canada\",\n" +
                "if(contains(?lowerLabel,\"america\")|| contains(?lowerLabel,\"savannah\")|| contains(?lowerLabel,\"sarasota\")|| contains(?lowerLabel,\"san fran\")||contains(?lowerLabel,\"monterey\")||contains(?lowerLabel,\"mississi\")||contains(?lowerLabel,\"michigan\")||contains(?lowerLabel,\"meyer\")||contains(?lowerLabel,\"maui\")||contains(?lowerLabel,\"massachuss\")||contains(?lowerLabel,\"maryland\")||contains(?lowerLabel,\"luau\")||contains(?lowerLabel,\"louisian\")||contains(?lowerLabel,\"frito\")||contains(?lowerLabel,\"florida\")||contains(?lowerLabel,\"carne\")|| contains(?lowerLabel,\"caramel appl\")|| contains(?lowerLabel,\"crystal light\")||contains(?lowerLabel,\"cool whip\")|| contains(?lowerLabel,\"cheez whiz\")|| contains(?lowerLabel,\"butterfinger\")|| contains(?lowerLabel,\"buckey\")|| contains(?lowerLabel,\"boston\")|| contains(?lowerLabel,\"blacken\")||contains(?lowerLabel,\"7-up\")||contains(?lowerLabel,\"7 up\") || contains(?lowerLabel,\"snack\")||contains(?lowerLabel,\"stuffing\")||contains(?lowerLabel,\"halibut\")|| contains(?lowerLabel,\"mattak\")|| contains(?lowerLabel,\"kittiwake\")|| contains(?lowerLabel,\"smoked salmon\")|| contains(?lowerLabel,\"chocolate bread\")|| contains(?lowerLabel,\"gumbo\")|| contains(?lowerLabel,\"eskimo\")|| contains(?lowerLabel,\"black cod\")|| contains(?lowerLabel,\"reindeer\")|| contains(?lowerLabel,\"caribou\")|| contains(?lowerLabel,\"musk\")|| contains(?lowerLabel,\"alask\")|| contains(?lowerLabel,\"greenland\")||contains(?lowerLabel,\"hawa\")||contains(?lowerLabel,\"poi\")||contains(?lowerLabel,\"laulau\")||contains(?lowerLabel,\"kalua\")||contains(?lowerLabel,\"poke\")||contains(?lowerLabel,\"lomi\")||contains(?lowerLabel,\"long rice\")||contains(?lowerLabel,\"saimin\")|| contains(?lowerLabel,\"teriyak\")|| contains(?lowerLabel,\"kona\")||contains(?lowerLabel,\"tex-mex\")|| contains(?lowerLabel,\"tex mex\")|| contains(?lowerLabel,\"chili\")|| contains(?lowerLabel,\"chilli\")|| contains(?lowerLabel,\"fajitas\")|| contains(?lowerLabel,\"salsa\")|| contains(?lowerLabel,\"beef\")|| contains(?lowerLabel,\"corn\")|| contains(?lowerLabel,\"tortilla\")||contains(?lowerLabel,\"ranch\")|| contains(?lowerLabel,\"bbq\")|| contains(?lowerLabel,\"blt\")|| contains(?lowerLabel,\"armadillo\")|| contains(?lowerLabel,\"appalach\")|| contains(?lowerLabel,\"alabama\")|| contains(?lowerLabel,\"macaroni\")|| contains(?lowerLabel,\"yum yum\")|| contains(?lowerLabel,\"tailgate\")|| contains(?lowerLabel,\"kozinaki\")|| contains(?lowerLabel,\"krispy kreme\")|| contains(?lowerLabel,\"kraft\")|| contains(?lowerLabel,\"kfc\")|| contains(?lowerLabel,\"kool-aid\")|| contains(?lowerLabel,\"hashbrown\")|| contains(?lowerLabel,\"jello\")|| contains(?lowerLabel,\"jell-o\")|| contains(?lowerLabel,\"hash brown\")|| contains(?lowerLabel,\"groundhog\")|| contains(?lowerLabel,\"grit\")|| contains(?lowerLabel,\"granola\")|| contains(?lowerLabel,\"fudge\")|| contains(?lowerLabel,\"tnaknsg\")|| contains(?lowerLabel,\"turkey\")|| contains(?lowerLabel,\"thyme\")|| contains(?lowerLabel,\"texas\")||contains(?lowerLabel,\"californ\")||contains(?lowerLabel,\"buffalo\")||contains(?lowerLabel,\"kansas\")||contains(?lowerLabel,\"midwest\")|| contains(?lowerLabel,\"brownie\")|| contains(?lowerLabel,\"taffy\")|| contains(?lowerLabel,\"philly\")|| contains(?lowerLabel,\"alamaba\")|| contains(?lowerLabel,\"halloween\")||contains(?lowerLabel,\"country\")||contains(?lowerLabel,\"finadene\")|| contains(?lowerLabel,\"pie\")|| contains(?lowerLabel,\"pancake\")|| contains(?lowerLabel,\"burger\"),\"US,United States\",   \n" +
                "if(contains(?lowerLabel,\"thai\")|| contains(?lowerLabel,\"gaeng\")|| contains(?lowerLabel,\"khao pad\")|| contains(?lowerLabel,\"kha kai\")|| contains(?lowerLabel,\"papaya salad\")|| contains(?lowerLabel,\"tom yum\")|| contains(?lowerLabel,\"red curry\")|| contains(?lowerLabel,\"kaphrao\")|| contains(?lowerLabel,\"phat\"),\"TH,Thailand\",\n" +
                "if(contains(?lowerLabel,\"australia\")|| contains(?lowerLabel,\"aussie\")|| contains(?lowerLabel,\"cherry ripe\")|| contains(?lowerLabel,\"kangaroo\")|| contains(?lowerLabel,\"sydney\")|| contains(?lowerLabel,\"granny smith\")|| contains(?lowerLabel,\"pumpkin soup\")|| contains(?lowerLabel,\"the lot\")|| contains(?lowerLabel,\"tomato sauce\")|| contains(?lowerLabel,\"lamington\")|| contains(?lowerLabel,\"snag\")|| contains(?lowerLabel,\"meat pie\")|| contains(?lowerLabel,\"pavlova\")|| contains(?lowerLabel,\"barramundi\")|| contains(?lowerLabel,\"vegemite\"),\"AU,Australia\",\n" +
                "if(contains(?lowerLabel,\"amish\"),\"amish\",\n" +
                "if(contains(?lowerLabel,\"middle east\"),\"middle east\",\n" +
                "if(contains(?lowerLabel,\"roman\")||contains(?lowerLabel,\"mici\")||contains(?lowerLabel,\"mamaliga\")||contains(?lowerLabel,\"ciorba\")||contains(?lowerLabel,\"mititei\")||contains(?lowerLabel,\"pork rind\")||contains(?lowerLabel,\"tripe soup\")||contains(?lowerLabel,\"cozonac\"),\"RO,Romania\",\n" +
                "if(contains(?lowerLabel,\"alban\")||contains(?lowerLabel,\"kakllaasarem\")||contains(?lowerLabel,\"byrek\")||contains(?lowerLabel,\"sataras\")||contains(?lowerLabel,\"tarator\")||contains(?lowerLabel,\"sheep pluck\"),\"AL,Albania\",\n" +
                "if(contains(?lowerLabel,\"jew\")|| contains(?lowerLabel,\"kwanzaa\")|| contains(?lowerLabel,\"challah\")|| contains(?lowerLabel,\"kosher\"),\"IL,Israel\",\n" +
                "if(contains(?lowerLabel,\"asian\")|| contains(?lowerLabel,\"tegelese\")|| contains(?lowerLabel,\"rice\"),\"Asian\"\n" +
                ",\"other\")))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))))) as ?recipeType)\n" +
                "#filter(?recipeType = \"other\")\n" +
                "}\n" +
                "group by ?recipeType\n" +
                "order by desc(?recipeCount)\n" +
                "#order by ?label";
        List<BindingSet> results = BlazegraphUtil.processQueryForRepository(getRepoName(),query);
        Map<String,CountryCount> map = new HashMap<>();

        for(BindingSet bs:results){
            int count = Integer.valueOf(bs.getValue("recipeCount").stringValue());
            if(bs.getValue("recipeType").stringValue().indexOf(",") < 0) continue;
            String [] parts = bs.getValue("recipeType").stringValue().split(";");
            for(String country:parts){
                String [] p= country.split(",");
                map.computeIfPresent(p[0],(k,v)->v.incrementCount(count));
                map.computeIfAbsent(p[0],k -> new CountryCount(p[0],p[1],count));
            }
        }
        return new ArrayList<>(map.values());
    }

    @Override
    public List<CategoryCount> getMealsOfTheDayCount() {
        String query = "prefix recipe-kb: <http://idea.rpi.edu/heals/kb/>\n" +
                "select distinct ?meal (count(?recipe) as ?recipeCount)\n" +
                "where {\n" +
                "?recipe rdf:type recipe-kb:recipe;\n" +
                "        rdfs:label ?label.\n" +
                "bind(lcase(?label) as ?lbl)\n" +
                "bind(\n" +
                "if(contains(?lbl,\"to go\")||contains(?lbl,\"takeout\")||contains(?lbl,\"fast food\")||contains(?lbl,\"drivethrough\")||contains(?lbl,\"delivery\"),\"takeout\",\n" +
                "if(contains(?lbl,\"pastries\")||contains(?lbl,\"iced buns\")||contains(?lbl,\"katsu sandos\")||contains(?lbl,\"petits\"),\"tea,brunch,elvenses\",\n" +
                "if(contains(?lbl,\"waffle\")||contains(?lbl,\"bacon\")||contains(?lbl,\"scrambled\")||contains(?lbl,\"bagel\")||contains(?lbl,\"breakfast sausage\")||contains(?lbl,\"cereal\")||contains(?lbl,\"crepe\")||contains(?lbl,\"croissant\")||contains(?lbl,\"egg\")||contains(?lbl,\"english muffin\")||contains(?lbl,\"hashbrown\")||contains(?lbl,\"muesli\")||contains(?lbl,\"french toast\")||contains(?lbl,\"gravy\"),\"breakfast,brunch\",\n" +
                "if(contains(?lbl,\"biscuit\")||contains(?lbl,\"toast\")||contains(?lbl,\"muffin\"),\"breakfast,brunch,elvenses\",\n" +
                "if(contains(?lbl,\"breakfast\")||contains(?lbl,\"doughnut\")||contains(?lbl,\"quiche\")||contains(?lbl,\"porridge\")||contains(?lbl,\"oatmeal\")||contains(?lbl,\"rancheros\")||contains(?lbl,\"huevos\")||contains(?lbl,\"omelette\")||contains(?lbl,\"sausage and egg\")||contains(?lbl,\"griddlecake\")||contains(?lbl,\"acai bowl\")||contains(?lbl,\"parfait\")||contains(?lbl,\"baked potato\")||contains(?lbl,\"cinnamon roll\")||contains(?lbl,\"benedict\"),\"breakfast\",\n" +
                "if(contains(?lbl,\"brunch\")||contains(?lbl,\"avocado\")||contains(?lbl,\"thinisian tajine\")||contains(?lbl,\"touton\")||contains(?lbl,\"terrine\")||contains(?lbl,\"tea sandwitch\")||contains(?lbl,\"tea cake\")||contains(?lbl,\"tartines\")||contains(?lbl,\"strata\")||contains(?lbl,\"souffle\")||contains(?lbl,\"smoked salmon\")||contains(?lbl,\"smoked fish\")||contains(?lbl,\"meeshay\")||contains(?lbl,\"ham\")||contains(?lbl,\"grits\")||contains(?lbl,\"grillades\")||contains(?lbl,\"fruit\")||contains(?lbl,\"fritata\")||contains(?lbl,\"facturas\")||contains(?lbl,\"coffee cake\")||contains(?lbl,\"chilaquiles\")||contains(?lbl,\"cheese\")||contains(?lbl,\"casserole\")||contains(?lbl,\"bizchoco\"),\"brunch\",\n" +
                "if(contains(?lbl,\"elvenses\")||contains(?lbl,\"muffin\")||contains(?lbl,\"snack\")||contains(?lbl,\"scone\")||contains(?lbl,\"bar\")||contains(?lbl,\"chocolate\"),\"elvenses\",\n" +
                "if(contains(?lbl,\"noodle\")||contains(?lbl,\"steak\")||contains(?lbl,\"beef\")||contains(?lbl,\"chicken\"),\"lunch,dinner\",\n" +
                "if(contains(?lbl,\"lunch\")||contains(?lbl,\"kidney\")||contains(?lbl,\"jacket potato\")||contains(?lbl,\"burrito\")||contains(?lbl,\"fish and chips\")||contains(?lbl,\"chicken wrap\")||contains(?lbl,\"pot roast\")||contains(?lbl,\"cobb\")||contains(?lbl,\"tater tot\")||contains(?lbl,\"quesadil\")||contains(?lbl,\"salad\")||contains(?lbl,\"tomato soup\")||contains(?lbl,\"grilled cheese\")||contains(?lbl,\"sandwich\")||contains(?lbl,\"cold cut\"),\"lunch\",\n" +
                "if(contains(?lbl,\"dinner\")||contains(?lbl,\"stroganoff\")||contains(?lbl,\"gyro\")||contains(?lbl,\"enchilada\")||contains(?lbl,\"burger\")||contains(?lbl,\"macaroni\")||contains(?lbl,\"mac and cheese\")||contains(?lbl,\"lasagne\")||contains(?lbl,\"sloppy joe\")||contains(?lbl,\"stuffed hells\")||contains(?lbl,\"turkey\")||contains(?lbl,\"calzone\")||contains(?lbl,\"fettuccine\")||contains(?lbl,\"sweet potato\")||contains(?lbl,\"pizz\")||contains(?lbl,\"spaghet\")||contains(?lbl,\"chili\")||contains(?lbl,\"chilli\")||contains(?lbl,\"chowder\")||contains(?lbl,\"fajita\")||contains(?lbl,\"taco\")||contains(?lbl,\"meatloaf\"),\"dinner,supper\",\n" +
                "if(contains(?lbl,\"snack\")||contains(?lbl,\"pecan\")||contains(?lbl,\"beech\")||contains(?lbl,\"hornbeam\")||contains(?lbl,\"hazel\")||contains(?lbl,\"walnut\")||contains(?lbl,\"pistachio\")||contains(?lbl,\"popcorn\")||contains(?lbl,\"nut\"),\"snacks\",\n" +
                "if(contains(?lbl,\"dessert\")||contains(?lbl,\"cream\")||contains(?lbl,\"delight\")||contains(?lbl,\"shaved ice\")||contains(?lbl,\"red velvet\")||contains(?lbl,\"pastry\")||contains(?lbl,\"jell\")||contains(?lbl,\"fruit\")||contains(?lbl,\"fudge\")||contains(?lbl,\"cookie\")||contains(?lbl,\"cake\")||contains(?lbl,\"caramel\")||contains(?lbl,\"pudding\")||contains(?lbl,\"cake\")||contains(?lbl,\"icecream\")||contains(?lbl,\"ice cream\"),\"dessert\",\n" +
                "if(contains(?lbl,\"fruit\")||contains(?lbl,\"nectarine\")||contains(?lbl,\"apricot\")||contains(?lbl,\"plum\")||contains(?lbl,\"grape\")||contains(?lbl,\"honeydew\")||contains(?lbl,\"kiwi\")||contains(?lbl,\"beries\")||contains(?lbl,\"berry\")||contains(?lbl,\"banan\")||contains(?lbl,\"mango\")||contains(?lbl,\"melon\")||contains(?lbl,\"pear\")||contains(?lbl,\"apple\"),\"fruit\",\n" +
                "if(contains(?lbl,\"tea\")||contains(?lbl,\"chai\"),\"tea-drinks,breakfast-drinks,brunch-drinks,tea-drinks,elvenses-drinks\",\n" +
                "if(contains(?lbl,\"coffee\")||contains(?lbl,\"decaff\")||contains(?lbl,\"freddo\")||contains(?lbl,\"frapp\")||contains(?lbl,\"mocha\")||contains(?lbl,\"carajillo\")||contains(?lbl,\"barraquito\")||contains(?lbl,\"kaff\")||contains(?lbl,\"caff\")||contains(?lbl,\"doppio\")||contains(?lbl,\"manilo\")||contains(?lbl,\"lungo\")||contains(?lbl,\"americano\")||contains(?lbl,\"moka\")||contains(?lbl,\"latte\")||contains(?lbl,\"cafe\")||contains(?lbl,\"espresso\")||contains(?lbl,\"macchiato\")||contains(?lbl,\"cappuc\"),\"coffee-drinks\",\n" +
                "if(contains(?lbl,\"juice\"),\"breakfast-drinks,brunch-drinks\",\n" +
                "if(contains(?lbl,\"bloody mary\")||contains(?lbl,\"sparkling water\")||contains(?lbl,\"yum cha\")||contains(?lbl,\"morning pint\")||contains(?lbl,\"spritzer\")||contains(?lbl,\"mimosa\")||contains(?lbl,\"irish coffee\")||contains(?lbl,\"champagne\"),\"brunch-drinks\",\n" +
                "if(contains(?lbl,\"smoothie\")||contains(?lbl,\"water\")||contains(?lbl,\"tomato juice\")||contains(?lbl,\"orange juice\")||contains(?lbl,\"hot chocolate\")||contains(?lbl,\"agua dulce\")||contains(?lbl,\"powder drink\")||contains(?lbl,\"shake\")||contains(?lbl,\"broth\"),\"breakfast-drinks\",\n" +
                "if(contains(?lbl,\"afra\")||contains(?lbl,\"liqueur\")||contains(?lbl,\"brandy\")||contains(?lbl,\"digesti\")||contains(?lbl,\"rakija\")||contains(?lbl,\"jeger\")||contains(?lbl,\"jager\")||contains(?lbl,\"domoda\")||contains(?lbl,\"soda\")||contains(?lbl,\"wine\")||contains(?lbl,\"benachin\")||contains(?lbl,\"baoab\")||contains(?lbl,\"akara\"),\"lunch-drinks\",\n" +
                "if(contains(?lbl,\"happy hour\")||contains(?lbl,\"tom collins\")||contains(?lbl,\"basil cocktail\")||contains(?lbl,\"manhattan\")||contains(?lbl,\"amaretto\")||contains(?lbl,\"vodka and grapefruit\")||contains(?lbl,\"greyhound\")||contains(?lbl,\"splash of cranberry\")||contains(?lbl,\"vodka soda\")||contains(?lbl,\"gin and tonic\"),\"happyhour-drinks\",\n" +
                "if(contains(?lbl,\"late night\")||contains(?lbl,\"gin\")||contains(?lbl,\"tequil\")||contains(?lbl,\"mohito\")||contains(?lbl,\"bourbon\")||contains(?lbl,\"whiskey\")||contains(?lbl,\"shots\")||contains(?lbl,\"vodka\")||contains(?lbl,\"beer\")||contains(?lbl,\"cocktail\")||contains(?lbl,\"martini\"),\"latenight-drinks\",\n" +
                "if(contains(?lbl,\"valentines\")||contains(?lbl,\"love\")||contains(?lbl,\"special\")||contains(?lbl,\"romantic\"),\"romantic\",\n" +
                "if(contains(?lbl,\"homemade\"),\"homemade\",\n" +
                "if(contains(?lbl,\"fast\")||contains(?lbl,\"instant\"),\"instant\",  \n" +
                "\"other\")))))))))))))))))))))))) as ?meal)\n" +
                "filter(?meal != \"other\")\n" +
                "}\n" +
                "group by ?meal\n";

        List<BindingSet> results = BlazegraphUtil.processQueryForRepository(getRepoName(),query);
        Map<String,CategoryCount> map = new HashMap<>();

        for(BindingSet bs:results){
            String []meals = bs.getValue("meal").stringValue().split(",");
            for(String meal:meals) {
                int count = Integer.valueOf(bs.getValue("recipeCount").stringValue());
                map.computeIfPresent(meal,(k,v)->new CategoryCount(k,v.getCount()+count));
                map.computeIfAbsent(meal,k -> new CategoryCount(meal,count));
            }

        }
        return new ArrayList<>(map.values());
    }



    @Override
    public List<Recipe> getRecipesWhichContainFoodItem(String foodItem) {
        String item = foodItem.toLowerCase();
        String query = "prefix recipe-kb: <http://idea.rpi.edu/heals/kb/>\n" +
                "select distinct ?recipe_label ?ing_label ?ing_quantity ?ing_unit\n" +
                "where {\n" +
                "?recipe rdf:type recipe-kb:recipe;\n" +
                "        rdfs:label ?recipe_label ;\n" +
                "        recipe-kb:uses ?ingredient.\n" +
                "?ingredient rdf:type recipe-kb:ingredientuse;\n" +
                "            recipe-kb:ing_name ?ing_name;\n" +
                "            recipe-kb:ing_quantity ?ing_quantity;\n" +
                "            recipe-kb:ing_unit ?ing_unit.\n" +
                "?ing_name rdfs:label ?ing_label.\n" +
                "{\n" +
                "    select distinct ?recipe_label\n" +
                "    where {\n" +
                "      ?recipe_temp rdf:type recipe-kb:recipe;\n" +
                "        rdfs:label ?recipe_label ;\n" +
                "        recipe-kb:uses ?ingredient_temp.\n" +
                "?ingredient_temp rdf:type recipe-kb:ingredientuse;\n" +
                "            recipe-kb:ing_name ?ing_name_temp.\n" +
                "?ing_name_temp rdfs:label ?ing_label_temp.\n" +
                "filter(contains(lcase(str(?ing_label_temp)),\""+item+"\") || contains(lcase(str(?recipe_label)),\""+item+"\")) \n" +
                "    }\n" +
                "}\n" +
                "filter(strlen(?ing_label)>0).  \n" +
                "}\n" +
                "order by ?recipe_label";

        List<BindingSet> results = BlazegraphUtil.processQueryForRepository(getRepoName(),query);
        List<Recipe> list = new ArrayList<>();

        Recipe currentRecipe = null;
        IngredientUse ingredientUse = null;
        for(BindingSet bs:results){
            if(currentRecipe == null){
                currentRecipe = new Recipe(bs.getValue("recipe_label").stringValue());
            }
            //if the previous and the current arent the same
            if(currentRecipe != null && !currentRecipe.getLabel().equals(bs.getValue("recipe_label").stringValue())){
                list.add(currentRecipe);
                currentRecipe = new Recipe(bs.getValue("recipe_label").stringValue());
            }
            ingredientUse = new IngredientUse(bs.getValue("ing_label").stringValue(),bs.getValue("ing_quantity").stringValue(),bs.getValue("ing_unit").stringValue());
            currentRecipe.addIngredientUse(ingredientUse);
        }
        return list;
    }

    @Override
    public Integer getNumberOfRecipesWhichContainFoodItem(String foodItem) {
        String item = foodItem.toLowerCase();
        String query = "prefix recipe-kb: <http://idea.rpi.edu/heals/kb/>\n" +
                "select distinct (count(?recipe_label) as ?recipe_cnt)\n" +
                "where {\n" +
                "?recipe rdf:type recipe-kb:recipe;\n" +
                "        rdfs:label ?recipe_label ;\n" +
                "        recipe-kb:uses ?ingredient.\n" +
                "?ingredient rdf:type recipe-kb:ingredientuse;\n" +
                "            recipe-kb:ing_name ?ing_name.\n" +
                "filter(contains(lcase(str(?ing_name)),\""+item+"\") || contains(lcase(str(?recipe_label)),\""+item+"\"))\n" +
                "}";
        List<BindingSet> results = BlazegraphUtil.processQueryForRepository(getRepoName(),query);
        Integer i=null;
        for(BindingSet bs:results){
            i = Integer.valueOf(bs.getValue("recipe_cnt").stringValue());
        }
        return i;
    }

    @Override
    public Integer getNumberOfIngredientsWhichContainFoodItem(String foodItem) {
        String item = foodItem.toLowerCase();
        String query = "prefix recipe-kb: <http://idea.rpi.edu/heals/kb/>\n" +
                "select distinct (count(?ing_name) as ?ingredient_cnt)\n" +
                "where {\n" +
                "?ingredient rdf:type recipe-kb:ingredientuse;\n" +
                "            recipe-kb:ing_name ?ing_name.\n" +
                "?ing_name rdfs:label ?ing_label.\n" +
                "filter(contains(lcase(str(?ing_label)),\""+item+"\"))\n" +
                "}";
        List<BindingSet> results = BlazegraphUtil.processQueryForRepository(getRepoName(),query);
        Integer i=null;
        for(BindingSet bs:results){
            i = Integer.valueOf(bs.getValue("ingredient_cnt").stringValue());
        }
        return i;
    }

    @Override
    public List<Recipe> searchRecipeByKeyword(String keyword){
        String item = keyword.toLowerCase();
        String query = "prefix recipe-kb: <http://idea.rpi.edu/heals/kb/>\n" +
                "select distinct ?recipe_label ?ing_label ?ing_quantity ?ing_unit\n" +
                "where {\n" +
                "?recipe rdf:type recipe-kb:recipe;\n" +
                "        rdfs:label ?recipe_label ;\n" +
                "        recipe-kb:uses ?ingredient.\n" +
                "?ingredient rdf:type recipe-kb:ingredientuse;\n" +
                "            recipe-kb:ing_name ?ing_name;\n" +
                "            recipe-kb:ing_quantity ?ing_quantity;\n" +
                "            recipe-kb:ing_unit ?ing_unit.\n" +
                "?ing_name rdfs:label ?ing_label.\n" +
                "filter(contains(?recipe_label,\""+item+"\"))\n" +
                "}\n" +
                "order by ?recipe_label\n";

        List<BindingSet> results = BlazegraphUtil.processQueryForRepository(getRepoName(),query);
        List<Recipe> list = new ArrayList<>();

        Recipe currentRecipe = null;
        IngredientUse ingredientUse = null;
        for(BindingSet bs:results){
            if(currentRecipe == null){
                currentRecipe = new Recipe(bs.getValue("recipe_label").stringValue());
            }
            //if the previous and the current arent the same
            if(currentRecipe != null && !currentRecipe.getLabel().equals(bs.getValue("recipe_label").stringValue())){
                list.add(currentRecipe);
                currentRecipe = new Recipe(bs.getValue("recipe_label").stringValue());
            }
            ingredientUse = new IngredientUse(bs.getValue("ing_label").stringValue(),bs.getValue("ing_quantity").stringValue(),bs.getValue("ing_unit").stringValue());
            currentRecipe.addIngredientUse(ingredientUse);
        }
        return list;
    }

    @Override
    public List<String> searchIngredientByKeyword(String keyword) {
        String item = keyword.toLowerCase();
        String query = "prefix recipe-kb: <http://idea.rpi.edu/heals/kb/>\n" +
                "select distinct ?ing_label\n" +
                "where {\n" +
                "?ingredient rdf:type recipe-kb:ingredientuse;\n" +
                "            recipe-kb:ing_name ?ing_name.\n" +
                "?ing_name rdfs:label ?ing_label\n" +
                "filter(contains(lcase(str(?ing_label)),\""+item+"\"))\n" +
                "}\n";

        List<BindingSet> results = BlazegraphUtil.processQueryForRepository(getRepoName(),query);
        List<String> list = new ArrayList<>();

        for(BindingSet bs:results){
            list.add(bs.getValue("ing_label").stringValue());
        }
        return list;
    }

    @Override
    public List<Recipe> getAllRecipes() {
        String query = "prefix recipe-kb: <http://idea.rpi.edu/heals/kb/>\n" +
                "select distinct ?recipe_label ?ing_label ?ing_quantity ?ing_unit\n" +
                "where {\n" +
                "?recipe rdf:type recipe-kb:recipe;\n" +
                "        rdfs:label ?recipe_label ;\n" +
                "        recipe-kb:uses ?ingredient.\n" +
                "?ingredient rdf:type recipe-kb:ingredientuse;\n" +
                "            recipe-kb:ing_name ?ing_name;\n" +
                "            recipe-kb:ing_quantity ?ing_quantity;\n" +
                "            recipe-kb:ing_unit ?ing_unit.\n" +
                "?ing_name rdfs:label ?ing_label.\n" +
                "filter(strlen(?ing_label)>0)\n" +
                "}\n" +
                "order by ?recipe_label";

        List<BindingSet> results = BlazegraphUtil.processQueryForRepository(getRepoName(),query);
        List<Recipe> list = new ArrayList<>();

        Recipe currentRecipe = null;
        IngredientUse ingredientUse = null;
        for(BindingSet bs:results){
            if(currentRecipe == null){
                currentRecipe = new Recipe(bs.getValue("recipe_label").stringValue());
            }
            //if the previous and the current arent the same
            if(currentRecipe != null && !currentRecipe.getLabel().equals(bs.getValue("recipe_label").stringValue())){
                list.add(currentRecipe);
                currentRecipe = new Recipe(bs.getValue("recipe_label").stringValue());
            }
            ingredientUse = new IngredientUse(bs.getValue("ing_label").stringValue(),bs.getValue("ing_quantity").stringValue(),bs.getValue("ing_unit").stringValue());
            currentRecipe.addIngredientUse(ingredientUse);
        }
        return list;
    }

    @Override
    public List<String> getAllIngredients() {
        String query = "prefix recipe-kb: <http://idea.rpi.edu/heals/kb/>\n" +
                "select distinct ?ing_label\n" +
                "where {\n" +
                "?ingredient rdf:type recipe-kb:ingredientuse;\n" +
                "            recipe-kb:ing_name ?ing_name.\n" +
                "?ing_name rdfs:label ?ing_label.\n" +
                "filter(strlen(?ing_label) > 0)\n" +
                "}\n" +
                "order by ?ing_label";
        List<BindingSet> results = BlazegraphUtil.processQueryForRepository(getRepoName(),query);
        List<String> list = new ArrayList<>();

        for(BindingSet bs:results){
            list.add(bs.getValue("ing_label").stringValue());
        }
        return list;
    }

    public String getRepoName(){
        String lastRepo = this.queue.removeLast();
        this.queue.addFirst(lastRepo);
        return lastRepo;
    }
}
