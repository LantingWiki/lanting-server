package wiki.lanting.controllers;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wiki.lanting.common.LantingResponse;
import wiki.lanting.models.UserEntity;
import wiki.lanting.services.UserService;

/**
 * @author wang.boyang
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/read")
    public LantingResponse<UserEntity> readUser(@RequestBody ReadUserRequestBody requestBody) {
        UserEntity user = userService.getUserById(requestBody.id);
        return new LantingResponse<UserEntity>().data(user);
    }

    @PostMapping("/create")
    public LantingResponse<UserEntity> createUser(@RequestBody CreateUserRequestBody requestBody) {
        UserEntity user = userService.createUser(requestBody);
        return new LantingResponse<UserEntity>().data(user);
    }

    @Data
    private static class ReadUserRequestBody {
        public long id;
    }

    @Data
    public static class CreateUserRequestBody {
        public String nickname;
    }
}
