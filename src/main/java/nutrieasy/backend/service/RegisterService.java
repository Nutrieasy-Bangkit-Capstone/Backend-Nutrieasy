package nutrieasy.backend.service;

import nutrieasy.backend.entity.User;
import nutrieasy.backend.model.vo.RegisterRequestVo;
import nutrieasy.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Created by Resa S.
 * Date: 29-05-2024
 * Created in IntelliJ IDEA.
 */
@Service
public class RegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequestVo registerRequestVo) {

        User user = new User();
        user.setEmail(registerRequestVo.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequestVo.getPassword()));
        user.setFullName(registerRequestVo.getFullName());
        user.setPhoneNumber(registerRequestVo.getPhoneNumber());
        user.setRole("ROLE_USER");

        try {
           // userRepository.findByEmail(registerRequestVo.getEmail());
            userRepository.save(user);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
