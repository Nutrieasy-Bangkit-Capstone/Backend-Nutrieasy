package nutrieasy.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Resa S.
 * Date: 31-05-2024
 * Created in IntelliJ IDEA.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NutrientsIntakeDetail {
    private int attrId;
    private String name;
    private double value;
    private String unit;
    private double minValue;
    private double maxValue;
}
