package nutrieasy.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by Resa S.
 * Date: 04-06-2024
 * Created in IntelliJ IDEA.
 */

@Entity
@Table(name = "user_meals_history")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserMealHistory {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "food_id", insertable = false, updatable = false)
    private Food food;

    @Lob
    @Column(name = "image_url", columnDefinition = "CLOB")
    private String imageUrl;

    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;
}
