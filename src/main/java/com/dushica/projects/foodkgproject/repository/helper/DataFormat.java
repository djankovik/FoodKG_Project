package com.dushica.projects.foodkgproject.repository.helper;

import java.util.HashMap;
import java.util.Map;

public class DataFormat {
    public static Map<String,Float> unitGramsMap = new HashMap<String, Float>() {{
        put("cup", 128.0f);
        put("lb", 453.592f);
        put("tablespoon", 15.0f);
        put("slice", 28.0f);
        put("medium", 179.0f);
        put("teaspoon", 4.2f);
        put("clove", 0.0036f);
        put("can", 400.0f);
        put("package", 100.0f);
        put("ounce", 28.3495f);
        put("head", 600.0f);
        put("bunch", 250.0f);
        put("whole", 50.0f);
        put("dash", 0.72f);
        put("jar", 350.0f);
        put("box", 900.0f);
        put("container", 200.0f);
        put("pinch", 0.36f);
        put("pint", 473.0f);
        put("inch", 7.0f);
        put("stalk", 114.0f);
        put("quart", 950.0f);
        put("envelope", 7.0f);
        put("ml", 1.0f);
        put("gram", 1.0f);
        put("litre", 1000.0f);
        put("gallon", 3785.41f);
        put("bag", 700.0f);
        put("unknown", 200.0f);
    }};

    public static float getGramsForInput(String unit, String quantity){
        //System.out.println("getGramsForInput "+unit+", "+quantity);
        try {
            String sunit = getStandardizedUnit(unit);
            float squant = getQuantity(quantity);
            //System.out.println(unit+", "+quantity+" = "+sunit+", "+squant);
            return squant*unitGramsMap.get(sunit);
        } catch(Exception e){
            //System.out.println("Error in getGramsForInput("+unit+","+quantity+")");
        }
        return 1.0f;
    }

    public static String getStandardizedUnit(String unit){
//        //System.out.println("getStandardizedUnit "+unit);
        if(unit == null || unit.length() == 0 || unit.replace(" ","").length() == 0)
            return "unknown"; //TODO

        String un = unit.toLowerCase().replace("\\.","");

        if(un.contains("bag")){
            return "bag";
        } else if(un.contains("gal")){
            return "gallon";
        } else if(un.contains("lit")){
            return "litre";
        } else if(un.contains("envelope")){
            return "envelope";
        } else if(un.contains("q")){
            return "quart";
        } else if(un.contains("stalk")){
            return "stalk";
        } else if(un.contains("jar")){
            return "jar";
        } else if(un.contains("box")){
            return "box";
        } else if(un.contains("inch")){
            return "inch";
        } else if(un.contains("whole")){
            return "whole";
        } else if(un.contains("pinch")){
            return "pinch";
        } else if(un.contains("contain")){
            return "container";
        } else if(un.contains("dash")){
            return "dash";
        } else if(un.contains("bunch")){
            return "bunch";
        } else if(un.contains("pint")){
            return "pint";
        } else if(un.contains("head")){
            return "head";
        } else if(un.contains("clove")){
            return "cloves";
        } else if(un.contains("slice")){
            return "slice";
        } else if(un.contains("medium")){
            return "medium";
        } else if(un.contains("package")){
            return "package";
        } else if(un.contains("can")){
            return "can";
        } else if(un.contains("z") || un.contains("ounc")){
            return "ounce";
        } else if(un.contains("lb")){
            return "lb";
        } else if(un.contains("ml")){
            return "ml";
        } else if(un.contains("g")){
            return "gram";
        } else if(un.contains("c")){
            return "cup";
        } else if(un.contains("tsp") || un.contains("teasp")){
        return "teaspoon";
        } else if(un.contains("tbs") || un.contains("tables")){
            return "tablespoon";
        } else return "unknown";
    }

    public static float getQuantity(String quantity){
       //System.out.println("getQuantity "+quantity);

        if(quantity == null || quantity.length() == 0 || quantity.replace("[^0-9]","").length() == 0) return 1.0f;

        //remove tailing and preceding '-' signs that dont seem to do anything. Leave the rest inbetween untouched
        quantity = quantity.replace("\\s*-\\s*","-").replace("-$","").replace("^-","");

        String [] alts = quantity.split("-");

        //if there aren't any to-to alternatives stated, just quantities
        if(alts.length ==1){
           return getNumberFromString(alts[0]);
        }
        else if(alts.length > 1){
            float avg=0.0f;
            for(String alt:alts){
                avg+=getNumberFromString(alt);
            }
            avg /= alts.length;
            return avg;
        }
        try {
            return Float.valueOf(quantity);
        } catch (Exception e){
            //System.out.println("das nicht good "+Float.valueOf(quantity));
        }

        return 1.0f;
    }

    public static float getNumberFromString(String s){
        //System.out.println("getNumberFromString "+s);

        if(s.length() == 0 || s.replace("[^0-9]","").length() == 0) return 0.0f;

        String [] parts = s.split("\\s+");
        float sum = 0.0f;
        for(String p:parts){
            if(p.matches("^[0-9]+\\.[0-9]+$]")){
                try {
                    sum += Float.valueOf(p);
                } catch (Exception e){
                    //System.out.println("das nicht good"+p);
                    }
            } else if(p.matches("^[0-9]+/[0-9]+$")){
                String [] dropka = p.split("/");
                try {
                    if(Float.valueOf(dropka[0]) == 0.0f){
                        continue;
                    }
                    else if(Float.valueOf(dropka[1]) == 0.0f){
                        sum+= Float.valueOf(dropka[0]);
                        continue;
                    }
                    sum += Float.valueOf(dropka[0])/Float.valueOf(dropka[1]);
                } catch (Exception e){//System.out.println("das nicht good "+p);
                    }
            } else if(p.matches("^[0-9]+$]")){
                try {
                    sum += Float.valueOf(p);
                } catch (Exception e){//System.out.println("das nicht good "+p);
                    }
            } else {
                try {
                    sum += Float.valueOf(p);
                } catch (Exception e){//System.out.println("das nicht good "+p);
                    }
            }
        }
        return sum;
    }
}
