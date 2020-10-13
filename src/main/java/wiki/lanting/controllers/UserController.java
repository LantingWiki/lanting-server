package wiki.lanting.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.stream.Collectors;

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
    public LantingResponse<Integer> countUser() {
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
    public LantingResponse<Boolean> massCreateUser(@RequestBody MassCreateRequestBody requestBody) throws JsonProcessingException {
        List<UserEntity> userEntities = requestBody.createUserRequestBodies.stream().map(createUserRequestBody -> {
            UserEntity userEntity = new UserEntity();
            userEntity.nickname = createUserRequestBody.nickname;
            return userEntity;
        }).collect(Collectors.toList());

        Boolean status = userService.massCreateUser(userEntities);
        return new LantingResponse<Boolean>().data(status);
    }

    @PostMapping("/pendingCreation")
    public LantingResponse<Integer> checkPendingCreation() {
        int result = userService.checkPendingCreation();
        return new LantingResponse<Integer>().data(result);
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
    private static class SearchUserRequestBody {
        public String nickname;
    }

    @Data
    private static class MassCreateRequestBody {
        List<CreateUserRequestBody> createUserRequestBodies;
    }
}
