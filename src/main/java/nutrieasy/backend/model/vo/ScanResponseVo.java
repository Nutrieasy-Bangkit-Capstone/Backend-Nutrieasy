package nutrieasy.backend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nutrieasy.backend.model.FoodDetails;
import nutrieasy.backend.model.NutrientsDetail;

import java.util.List;

/**
 * Created by Resa S.
 * Date: 31-05-2024
 * Created in IntelliJ IDEA.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanResponseVo {
    private Boolean success;
    private String message;
    private FoodDetails data;
}
