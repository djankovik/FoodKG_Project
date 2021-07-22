package com.dushica.projects.foodkgproject.model.wtm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Ingredient {
    String url;
    String name;
    boolean hasGluten;
    Integer glycemicIndex;
    String dcSource;
    String label;
    String skosDefinition;
    Set<String> textures;
    Set<String> flavors;
    Set<String> substitutesFor;

    public Ingredient(String url){
        this.url = url;
        this.name = url.replaceAll("^\\S*\\/","")
                .replaceAll(">","")
                .replaceAll("(.)([A-Z])", "$1 $2");
        this.textures = new HashSet<>();
        this.flavors = new HashSet<>();
        this.substitutesFor = new HashSet<>();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return url.equals(that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    public void addTexture(String object) {
        this.textures.add(object);
    }

    public void addFlavor(String object) {
        this.flavors.add(object);
    }

    public void addCanSubstituteFor(String object) {
        this.substitutesFor.add(object);
    }
}
