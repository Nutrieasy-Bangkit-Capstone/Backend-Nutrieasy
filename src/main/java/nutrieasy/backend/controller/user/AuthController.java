package nutrieasy.backend.controller.user;

import nutrieasy.backend.model.vo.LoginRequestVO;
import nutrieasy.backend.utils.JwtUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Resa S.
 * Date: 08-05-2024
 * Created in IntelliJ IDEA.
 */

@RestController
public class AuthController {
    @PostMapping("/login")
    public String login(@RequestBody LoginRequestVO request) {
        // Authenticate the user (e.g., using Spring Security's authentication manager)


        // If authentication is successful, generate a JWT
        
        String token = null;
        try {
            token = JwtUtils.generateToken(request.getUsername());
            System.out.println("Token: " + token);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return token;
    }
}
