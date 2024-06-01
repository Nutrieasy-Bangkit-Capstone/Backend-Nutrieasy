package nutrieasy.backend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import nutrieasy.backend.entity.User;

/**
 * Created by Resa S.
 * Date: 30-05-2024
 * Created in IntelliJ IDEA.
 */
@Data
@AllArgsConstructor
public class UserResponseVo {
    private Boolean success;
    private String message;
    private User user;
}
