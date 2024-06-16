package nutrieasy.backend.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by Resa S.
 * Date: 29-05-2024
 * Created in IntelliJ IDEA.
 */
@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @Column(name = "uid")
    private String uid;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "date_of_birth")
    private String dateOfBirth;

    @Column(name = "gender")
    private String gender;

    @Column(name = "height")
    private int height;

    @Column(name = "weight")
    private int weight;

    @Column(name = "role")
    private String role;

    @Column(name = "activity_level")
    private String activityLevel;
}
