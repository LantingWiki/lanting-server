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

import java.util.List;

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
        UserEntity user = userService.readUser(requestBody.id);
        return new LantingResponse<UserEntity>().data(user);
    }

    @PostMapping("/create")
    public LantingResponse<UserEntity> createUser(@RequestBody CreateUserRequestBody requestBody) {
        UserEntity userEntity = new UserEntity();
        userEntity.nickname = requestBody.nickname;
        UserEntity user = userService.createUser(userEntity);
        return new LantingResponse<UserEntity>().data(user);
    }

    @PostMapping("/update")
    public LantingResponse<Integer> updateUser(@RequestBody UpdateUserRequestBody requestBody) {
        UserEntity userEntity = new UserEntity();
        userEntity.id = requestBody.id;
        userEntity.nickname = requestBody.nickname;
        int result = userService.updateUser(userEntity);
        return new LantingResponse<Integer>().data(result);

    }

    @PostMapping("/delete")
    public LantingResponse<Integer> deleteUser(@RequestBody DeleteUserRequestBody requestBody) {
        int result = userService.deleteUser(requestBody.id);
        return new LantingResponse<Integer>().data(result);
    }

    @PostMapping("/count")
    public LantingResponse<Integer> countUser(@RequestBody CountUserRequestBody requestBody) {
        int result = userService.countUser();
        return new LantingResponse<Integer>().data(result);
    }

    @PostMapping("/search")
    public LantingResponse<List<UserEntity>> searchUser(@RequestBody SearchUserRequestBody requestBody) {
        UserEntity userEntity = new UserEntity(requestBody.nickname);
        List<UserEntity> result = userService.searchUser(userEntity);
        return new LantingResponse<List<UserEntity>>().data(result);
    }

    @PostMapping("/create/mass")
    public LantingResponse<Boolean> massCreateUser(@RequestBody Integer count) {
        Boolean status = userService.massCreateUser(count);
        return new LantingResponse<Boolean>().data(status);
    }

    @Data
    private static class ReadUserRequestBody {
        public long id;
    }

    @Data
    public static class CreateUserRequestBody {
        public String nickname;
    }

    @Data
    public static class UpdateUserRequestBody {
        public long id;
        public String nickname;
    }

    @Data
    public static class DeleteUserRequestBody {
        public long id;
    }

    @Data
    public static class CountUserRequestBody {
    }

    @Data
    private static class SearchUserRequestBody {
        public String nickname;
    }
}
