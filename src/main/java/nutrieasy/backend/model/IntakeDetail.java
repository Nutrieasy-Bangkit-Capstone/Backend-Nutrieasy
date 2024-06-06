package nutrieasy.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Resa S.
 * Date: 06-06-2024
 * Created in IntelliJ IDEA.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntakeDetail {
    private int totalVitaminA;
    private int totalVitaminC;
    private int totalVitaminD;
    private int totalVitaminE;
    private int totalVitaminB;
    private int totalVitaminB6;
    private int totalVitaminB12;
    private int totalCalcium;
    private int totalIron;
    private int totalMagnesium;
    private int totalPotassium;
    private int totalSodium;
    private int totalZinc;
    private int totalFiber;
    private int totalSugar;
}
