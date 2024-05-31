package nutrieasy.backend.model.vo;

import lombok.Data;

/**
 * Created by Resa S.
 * Date: 30-05-2024
 * Created in IntelliJ IDEA.
 */
@Data
public class LoginByGoogleRequestVo {
    private String email;
    private String uid;
    private String fullName;
}
