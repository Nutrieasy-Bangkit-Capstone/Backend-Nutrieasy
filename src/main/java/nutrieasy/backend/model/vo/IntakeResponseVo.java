package nutrieasy.backend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nutrieasy.backend.model.NutrientsIntakeDetail;

import java.util.List;

/**
 * Created by Resa S.
 * Date: 05-06-2024
 * Created in IntelliJ IDEA.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntakeResponseVo {
    private Boolean success;
    private String message;
    private List<NutrientsIntakeDetail> totalIntakeList;
}
