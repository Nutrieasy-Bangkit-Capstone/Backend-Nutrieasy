package nutrieasy.backend.service;

import nutrieasy.backend.entity.User;
import nutrieasy.backend.model.vo.UpdateUserRequestVo;
import nutrieasy.backend.model.vo.UserResponseVo;
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

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponseVo updateUser(UpdateUserRequestVo updateUserRequestVo) {

        User userFromDb = userRepository.findByUid(updateUserRequestVo.getUid());
        if (userFromDb == null) {
            return new UserResponseVo(false, "User not found", null);
        }

        userFromDb.setFullName(updateUserRequestVo.getFullName());
        userFromDb.setGender(updateUserRequestVo.getGender());
        userFromDb.setDateOfBirth(updateUserRequestVo.getDateOfBirth());
        userFromDb.setWeight(updateUserRequestVo.getWeight());
        userFromDb.setHeight(updateUserRequestVo.getHeight());

        userRepository.save(userFromDb);
        userFromDb.setPassword("");
        return new UserResponseVo(true, "User updated successfully", userFromDb);
    }


    public UserResponseVo getUserByUid(String uid) {
        User user = userRepository.findByUid(uid);
        if (user == null) {
            return new UserResponseVo(false, "User not found", null);
        }
        return new UserResponseVo(true, "User found", user);
    }

}
