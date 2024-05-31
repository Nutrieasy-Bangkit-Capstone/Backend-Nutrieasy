package nutrieasy.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by Resa S.
 * Date: 31-05-2024
 * Created in IntelliJ IDEA.
 */
@Entity
@Data
@Table(name = "foods")
@AllArgsConstructor
@NoArgsConstructor
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "serving_unit")
    private String servingUnit;
    @Column(name = "serving_qty")
    private int servingQty;
    @Column(name = "serving_weight_grams")
    private int servingWeightGrams;
    @Column(name = "nutrients_json")
    private String nutrientsJson;
    @Column(name = "created_at")
    private Timestamp createdAt;
}
