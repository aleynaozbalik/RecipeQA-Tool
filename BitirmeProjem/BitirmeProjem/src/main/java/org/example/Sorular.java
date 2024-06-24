package org.example;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Sorular {
    public static String[][] sorularMenu = {
            {"1", "Yemek yapmak için kaç dakika vaktiniz var?"},
            {"2", "Kaç kişilik bir yemek yapmak istiyorsunuz?"},
            {"3", "Elinizdeki malzemeleri giriniz :"},
            {"4", "Yemeğin yapılışını adım adım anlatır mısın?"}
    };

    public static String getSorular(Document doc, int id, String recipeName) {
        StringBuilder allData = new StringBuilder();
        for (String[] soru : sorularMenu) {
            String question = soru[1];
            String answer = "";
            switch (soru[0]) {
                case "1":
                    answer = getCookTime(doc);
                    break;
                case "2":
                    answer = getServingSize(doc);
                    break;
                case "3":
                    answer = getIngredients(doc);
                    break;
                case "4":
                    answer = getInstructions(doc);
                    break;
            }
            allData.append(id).append(",").append(recipeName).append(",").append(question).append(",").append(answer).append("\n");
        }
        return allData.toString();
    }

    public static String getCookTime(Document doc) {
        Element cookTimeElement = doc.selectFirst("span[itemprop=cookTime]");
        return cookTimeElement != null ? cookTimeElement.text() : "Süre bulunamadı";
    }

    public static String getServingSize(Document doc) {
        Element servingSizeElement = doc.selectFirst("span.text > span");
        return servingSizeElement != null ? servingSizeElement.text() : "Porsiyon bilgisi bulunamadı";
    }

    public static String getIngredients(Document doc) {
        Elements ingredientsList = doc.select("ul.recipe-materials > li[itemprop=recipeIngredient]");
        StringBuilder ingredients = new StringBuilder();
        for (Element ingredient : ingredientsList) {
            ingredients.append(ingredient.text()).append(", ");
        }
        return ingredients.toString();
    }

    public static String getInstructions(Document doc) {
        Elements instructionsList = doc.select("ol.recipe-instructions > li");
        StringBuilder instructions = new StringBuilder();
        for (Element instruction : instructionsList) {
            instructions.append(instruction.text()).append(", ");
        }
        return instructions.toString();
    }
}
