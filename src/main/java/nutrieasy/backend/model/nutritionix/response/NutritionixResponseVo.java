package nutrieasy.backend.model.nutritionix.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by Resa S.
 * Date: 28-05-2024
 * Created in IntelliJ IDEA.
 */
@Data
public class NutritionixResponseVo {
    List<Food> foods;
}
