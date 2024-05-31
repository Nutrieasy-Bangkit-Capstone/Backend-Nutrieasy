package nutrieasy.backend.controller.user;

import nutrieasy.backend.model.vo.LoginByGoogleRequestVo;
import nutrieasy.backend.model.vo.LoginRequestVO;
import nutrieasy.backend.model.vo.LoginResponseVo;
import nutrieasy.backend.service.AuthService;
import org.springframework.http.ResponseEntity;
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

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseVo> createAuthenticationToken(@RequestBody LoginRequestVO loginRequestVO) {
        return ResponseEntity.ok(authService.login(loginRequestVO));
    }

    @PostMapping("/loginByGoogle")
    public ResponseEntity<LoginResponseVo> loginByGoogle(@RequestBody LoginByGoogleRequestVo loginRequestVO) {
        return ResponseEntity.ok(authService.loginByGoogle(loginRequestVO));
    }
}
