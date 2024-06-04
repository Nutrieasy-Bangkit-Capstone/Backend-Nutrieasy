package nutrieasy.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by Resa S.
 * Date: 04-06-2024
 * Created in IntelliJ IDEA.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HistoryModel {
    private Long id;
    private String userId;
    private Long foodId;
    private String foodName;
    private String servingUnit;
    private int servingQty;
    private int servingWeightGrams;
    private String imageUrl;
    private String createdAt;
    private List<NutrientsDetail> nutrientsDetailList;
}
