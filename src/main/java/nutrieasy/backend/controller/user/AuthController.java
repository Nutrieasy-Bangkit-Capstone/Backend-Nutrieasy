package nutrieasy.backend.controller.user;

import nutrieasy.backend.model.vo.LoginRequestVO;
import nutrieasy.backend.model.vo.RegisterRequestVo;
import nutrieasy.backend.repository.UserRepository;
import nutrieasy.backend.service.NutrieasyUserDetailsService;
import nutrieasy.backend.service.RegisterService;
import nutrieasy.backend.utils.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final AuthenticationManager authenticationManager;
    private final NutrieasyUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final RegisterService registerService;

    public AuthController(AuthenticationManager authenticationManager, NutrieasyUserDetailsService userDetailsService, JwtUtil jwtUtil, RegisterService registerService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.registerService = registerService;
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequestVo registerRequestVo) {
        System.out.println("user = " + registerRequestVo);

        try {
           registerService.register(registerRequestVo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "User registered successfully";
    }

    @PostMapping("/login")
    public String createAuthenticationToken(@RequestBody LoginRequestVO loginRequestVO) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequestVO.getEmail(), loginRequestVO.getPassword()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Incorrect username or password", e);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequestVO.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails);

        return jwt;
    }
}
