package nutrieasy.backend.model.vo;

import lombok.Data;

/**
 * Created by Resa S.
 * Date: 30-05-2024
 * Created in IntelliJ IDEA.
 */
@Data
public class LoginResponseVo {
    private Boolean success;
    private String message;
    private String token;
}
