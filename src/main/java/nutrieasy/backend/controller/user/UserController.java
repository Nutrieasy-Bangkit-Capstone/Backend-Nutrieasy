package nutrieasy.backend.controller.user;

import nutrieasy.backend.model.vo.UpdateUserRequestVo;
import nutrieasy.backend.model.vo.UserResponseVo;
import nutrieasy.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by Resa S.
 * Date: 30-05-2024
 * Created in IntelliJ IDEA.
 */
@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user")
    public ResponseEntity<UserResponseVo> getUserByUid(@RequestParam(value = "uid", required = true) String uid) {
        UserResponseVo userResponseVo = userService.getUserByUid(uid);
        if (Boolean.FALSE.equals(userResponseVo.getSuccess())) {
            return ResponseEntity.status(404).body(userResponseVo);
        }
        return ResponseEntity.ok(userResponseVo);
    }

    @PutMapping("/user/updateProfile")
    public ResponseEntity<UserResponseVo> updateUser(@RequestBody UpdateUserRequestVo updateUserRequestVo) {
        return ResponseEntity.ok(userService.updateUser(updateUserRequestVo));
    }

}
