package nutrieasy.backend.controller.user;

import nutrieasy.backend.model.vo.UpdateUserRequestVo;
import nutrieasy.backend.model.vo.UpdateUserResponseVo;
import nutrieasy.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

    @PutMapping("/updateProfile")
    public ResponseEntity<UpdateUserResponseVo> updateUser(@RequestBody UpdateUserRequestVo updateUserRequestVo) {
        return ResponseEntity.ok(userService.updateUser(updateUserRequestVo));
    }
}
