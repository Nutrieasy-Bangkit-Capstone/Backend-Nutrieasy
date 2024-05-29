package nutrieasy.backend.model.nutritionix.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Resa S.
 * Date: 28-05-2024
 * Created in IntelliJ IDEA.
 */
@Data
class AltMeasure {
    private int serving_weight;
    private String measure;
    private int seq;
    private int qty;
}