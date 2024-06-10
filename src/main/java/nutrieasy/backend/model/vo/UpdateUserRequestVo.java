package nutrieasy.backend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by Resa S.
 * Date: 30-05-2024
 * Created in IntelliJ IDEA.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequestVo {
    private String uid;
    private String fullName;
    private String gender;
    private String dateOfBirth;
    private Integer height;
    private Integer weight;
}
