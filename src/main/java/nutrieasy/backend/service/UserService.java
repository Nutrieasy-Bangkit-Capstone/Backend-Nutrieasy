package nutrieasy.backend.service;

import nutrieasy.backend.entity.User;
import nutrieasy.backend.model.vo.UpdateUserRequestVo;
import nutrieasy.backend.model.vo.UpdateUserResponseVo;
import nutrieasy.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Created by Resa S.
 * Date: 30-05-2024
 * Created in IntelliJ IDEA.
 */
@Service
public class UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UpdateUserResponseVo updateUser(UpdateUserRequestVo updateUserRequestVo) {

        User userFromDb = userRepository.findByEmail(updateUserRequestVo.getEmail());
        if (userFromDb == null) {
            return new UpdateUserResponseVo(false, "User not found", null);
        }

        userFromDb.setFullName(updateUserRequestVo.getFullName());
        userFromDb.setPassword(passwordEncoder.encode(updateUserRequestVo.getPassword()));
        userFromDb.setGender(updateUserRequestVo.getGender());
        userFromDb.setDateOfBirth(updateUserRequestVo.getDateOfBirth());
        userFromDb.setWeight(updateUserRequestVo.getWeight());
        userFromDb.setHeight(updateUserRequestVo.getHeight());

        userRepository.save(userFromDb);
        return new UpdateUserResponseVo(true, "User updated successfully", userFromDb);
    }

}
