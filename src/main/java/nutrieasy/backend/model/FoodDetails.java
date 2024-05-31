package nutrieasy.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by Resa S.
 * Date: 31-05-2024
 * Created in IntelliJ IDEA.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodDetails {
    private String foodName;
    private String imageUrl;
    private int servingWeightGrams;
    private int servingQty;
    private String servingUnit;
    private List<NutrientsDetail> nutrientsDetailList;
}
