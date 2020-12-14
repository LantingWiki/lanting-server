package wiki.lanting.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import wiki.lanting.common.LantingResponse;
import wiki.lanting.models.ArchiveBasicInfoEntity;
import wiki.lanting.models.ArchiveTributeInfoEntity;
import wiki.lanting.models.UserEntity;
import wiki.lanting.services.ArchiveService;
import wiki.lanting.utils.GetIP;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wang.boyang
 */
@Slf4j
@RestController
@RequestMapping("/api/archive")
public class ArchiveController {

    final ArchiveService archiveService;

    public ArchiveController(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    @PostMapping("/read")
    public LantingResponse<UserEntity> readUser(@RequestBody ReadUserRequestBody requestBody) {
        UserEntity user = archiveService.readUser(requestBody.id);
        return new LantingResponse<UserEntity>().data(user);
    }

    @PostMapping("/create")
    public LantingResponse<UserEntity> createUser(@RequestBody CreateUserRequestBody requestBody) {
        UserEntity userEntity = new UserEntity();
        userEntity.nickname = requestBody.nickname;
        UserEntity user = archiveService.createUser(userEntity);
        return new LantingResponse<UserEntity>().data(user);
    }

    @PostMapping("/update")
    public LantingResponse<Integer> updateUser(@RequestBody UpdateUserRequestBody requestBody) {
        UserEntity userEntity = new UserEntity();
        userEntity.id = requestBody.id;
        userEntity.nickname = requestBody.nickname;
        int result = archiveService.updateUser(userEntity);
        return new LantingResponse<Integer>().data(result);

    }

    @PostMapping("/delete")
    public LantingResponse<Integer> deleteUser(@RequestBody DeleteUserRequestBody requestBody) {
        int result = archiveService.deleteUser(requestBody.id);
        return new LantingResponse<Integer>().data(result);
    }

    @PostMapping("/count")
    public LantingResponse<Integer> countUser() {
        int result = archiveService.countUser();
        return new LantingResponse<Integer>().data(result);
    }

    @PostMapping("/like/create")
    public LantingResponse<LikeRequestBody> createLikeArticle(HttpServletRequest request, @RequestBody LikeRequestBody likeRequestBody) {
        log.info("the request is: {}", request);
        String clientAddress = GetIP.getClientIp(request);
        log.info("the clientAddress is: {}", clientAddress);
        LikeRequestBody result = archiveService.likeArticle(likeRequestBody, clientAddress);
        return new LantingResponse<LikeRequestBody>().data(result);
    }

    @GetMapping("/like/read")
    public LantingResponse<Map<Long, Integer>> readLikeArticle(@RequestParam long articleId) {
        Map<Long, Integer> result = archiveService.readLikeArticle(articleId);
        return new LantingResponse<Map<Long, Integer>>().data(result);
    }

    @PostMapping("/search")
    public LantingResponse<List<UserEntity>> searchUser(@RequestBody SearchUserRequestBody requestBody) {
        UserEntity userEntity = new UserEntity(requestBody.nickname);
        List<UserEntity> result = archiveService.searchUser(userEntity);
        return new LantingResponse<List<UserEntity>>().data(result);
    }

    @PostMapping("/create/mass")
    public LantingResponse<Boolean> massCreateUser(@RequestBody MassCreateRequestBody requestBody) throws JsonProcessingException {
        List<UserEntity> userEntities = requestBody.createUserRequestBodies.stream().map(createUserRequestBody -> {
            UserEntity userEntity = new UserEntity();
            userEntity.nickname = createUserRequestBody.nickname;
            return userEntity;
        }).collect(Collectors.toList());

        Boolean status = archiveService.massCreateUser(userEntities);
        return new LantingResponse<Boolean>().data(status);
    }

    @PostMapping("/pendingCreation")
    public LantingResponse<Integer> checkPendingCreation() {
        int result = archiveService.checkPendingCreation();
        return new LantingResponse<Integer>().data(result);
    }

    @PostMapping("/tribute/info")
    public LantingResponse<ArchiveBasicInfoEntity> tributeArchiveInfo(@RequestBody String link) throws IOException {
        ArchiveBasicInfoEntity archiveBasicInfoEntity = archiveService.tributeArchiveInfo(link);
        return new LantingResponse<ArchiveBasicInfoEntity>().data(archiveBasicInfoEntity);
    }

    @PostMapping("/tribute/save")
    public LantingResponse<Boolean> tributeArchiveSave(@RequestBody ArchiveTributeInfoEntity archiveTributeInfoEntity){
        return archiveService.tributeArchiveSave(archiveTributeInfoEntity);
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

    @Data
    public static class LikeRequestBody {
        public long articleId;
        public boolean like;
    }
}
