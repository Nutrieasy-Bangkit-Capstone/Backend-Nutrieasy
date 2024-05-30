package nutrieasy.backend.model.vo;

import lombok.Data;

/**
 * Created by Resa S.
 * Date: 29-05-2024
 * Created in IntelliJ IDEA.
 */
@Data
public class RegisterRequestVo {
    private String email;
    private String password;
    private String fullName;
    private String phoneNumber;
}
