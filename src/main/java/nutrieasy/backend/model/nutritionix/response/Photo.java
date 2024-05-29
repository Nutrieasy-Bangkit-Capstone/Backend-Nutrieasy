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
class Photo {
    private String thumb;
    private String highres;
    private boolean is_user_uploaded;
}
