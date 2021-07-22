package com.dushica.projects.foodkgproject.model.wtm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FoodThingDao {      //Flavor, Texture, Course, Meal, Ingredient
    String url;
    String label;

    //Extract FoodThing label given just its url
    public FoodThingDao(String thingURI){ //<http://purl.org/heals/ingredient/FoodThing>
        //replace first half of url
        this.url = thingURI;
        this.label = thingURI.replaceAll("^\\S*\\/","").replaceAll(">","");
    }
}
