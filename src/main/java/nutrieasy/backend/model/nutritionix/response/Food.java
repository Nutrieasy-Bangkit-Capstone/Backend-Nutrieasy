package nutrieasy.backend.model.nutritionix.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Created by Resa S.
 * Date: 28-05-2024
 * Created in IntelliJ IDEA.
 */
@Data
public class Food {
    private String food_name;
    private String brand_name;
    private int serving_qty;
    private String serving_unit;
    private int serving_weight_grams;
    private double nf_calories;
    private double nf_total_fat;
    private double nf_saturated_fat;
    private int nf_cholesterol;
    private double nf_sodium;
    private double nf_total_carbohydrate;
    private double nf_dietary_fiber;
    private double nf_sugars;
    private double nf_protein;
    private double nf_potassium;
    private double nf_p;
    private List<FullNutrient> full_nutrients;
    private String nix_brand_name;
    private String nix_brand_id;
    private String nix_item_name;
    private String nix_item_id;
    private String upc;
    private String consumed_at;
    private Map<String, Object> metadata;
    private int source;
    private int ndb_no;
    private Tags tags;
    private List<AltMeasure> alt_measures;
    private Double lat;
    private Double lng;
    private int meal_type;
    private Photo photo;
    private Object sub_recipe;
    private Object class_code;
    private Object brick_code;
    private Object tag_id;
}
