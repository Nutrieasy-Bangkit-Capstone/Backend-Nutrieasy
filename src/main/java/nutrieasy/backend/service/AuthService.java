package nutrieasy.backend.service;

import nutrieasy.backend.entity.User;
import nutrieasy.backend.model.vo.LoginByGoogleRequestVo;
import nutrieasy.backend.model.vo.LoginRequestVO;
import nutrieasy.backend.model.vo.LoginResponseVo;
import nutrieasy.backend.repository.UserRepository;
import nutrieasy.backend.utils.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Created by Resa S.
 * Date: 30-05-2024
 * Created in IntelliJ IDEA.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final NutrieasyUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, NutrieasyUserDetailsService userDetailsService, JwtUtil jwtUtil, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }


public LoginResponseVo login(LoginRequestVO loginRequestVO) {
        LoginResponseVo loginResponseVo = new LoginResponseVo();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestVO.getEmail(), loginRequestVO.getPassword())
            );
        } catch (Exception e) {
            e.printStackTrace();
            loginResponseVo.setSuccess(false);
            loginResponseVo.setMessage("Incorrect username or password");
            return loginResponseVo;
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequestVO.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails);

        loginResponseVo.setSuccess(true);
        loginResponseVo.setMessage("Login successful");
        loginResponseVo.setToken(jwt);
        return loginResponseVo;
    }

    public LoginResponseVo loginByGoogle(LoginByGoogleRequestVo loginByGoogleRequestVo) {
        LoginResponseVo loginResponseVo = new LoginResponseVo();

        User user = userRepository.findByEmail(loginByGoogleRequestVo.getEmail());
        if (user == null) {
            User newUser = new User();
            newUser.setEmail(loginByGoogleRequestVo.getEmail());
            newUser.setFullName(loginByGoogleRequestVo.getFullName());
            newUser.setPassword(passwordEncoder.encode("default"));
            newUser.setUid(loginByGoogleRequestVo.getUid());
            newUser.setRole("ROLE_USER");
            userRepository.save(newUser);
        }

        System.out.println("loginByGoogleRequestVo.getEmail() = " + loginByGoogleRequestVo.getEmail());

        try {

            final UserDetails userDetails = userDetailsService.loadUserByUsername(loginByGoogleRequestVo.getEmail());
            final String jwt = jwtUtil.generateToken(userDetails);

            loginResponseVo.setSuccess(true);
            loginResponseVo.setMessage("Login successful");
            loginResponseVo.setToken(jwt);
            return loginResponseVo;

        } catch (Exception e) {
            e.printStackTrace();
            loginResponseVo.setSuccess(false);
            loginResponseVo.setMessage("Incorrect username or password");
            return loginResponseVo;
        }

    }
}
