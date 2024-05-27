package nutrieasy.backend.model.vo;

/**
 * Created by Resa S.
 * Date: 08-05-2024
 * Created in IntelliJ IDEA.
 */

public class LoginRequestVO {
    private String username;
    private String password;

    public LoginRequestVO() {
    }

    public LoginRequestVO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
