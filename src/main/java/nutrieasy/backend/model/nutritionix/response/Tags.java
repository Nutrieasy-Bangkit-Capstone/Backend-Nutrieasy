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
class Tags {
    private String item;
    private Object measure;
    private String quantity;
    private int food_group;
    private int tag_id;
}

