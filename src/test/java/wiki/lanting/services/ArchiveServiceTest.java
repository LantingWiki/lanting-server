package wiki.lanting.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import wiki.lanting.models.ArchiveBasicInfoEntity;
import wiki.lanting.models.ArchiveTributeInfoEntity;
import wiki.lanting.models.SearchKeywordEntity;
import wiki.lanting.models.UserEntity;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class ArchiveServiceTest {

    @Autowired
    ArchiveService archiveService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeAll
    static void setUp() {

    }

    @AfterAll
    static void tearDown() {

    }


    public static List<String> readFileInList(String fileName) {

        List<String> lines = Collections.emptyList();
        try {
            lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
        } catch (IOException e) {

            // do something
            e.printStackTrace();
        }
        return lines;
    }

    @Test
    void userServiceTest() {
        UserEntity origUserEntity = new UserEntity(null, "test_nickname");
        UserEntity actual = archiveService.createUser(origUserEntity);
        assertEquals("test_nickname", actual.nickname);

        UserEntity readUser = archiveService.readUser(actual.id);
        assertEquals(actual, readUser);

        actual.nickname = "test_nickname2";
        int i = archiveService.updateUser(actual);
        assertEquals(1, i);

        readUser = archiveService.readUser(actual.id);
        assertEquals("test_nickname2", readUser.nickname);

        i = archiveService.deleteUser(actual.id);
        assertEquals(1, i);

        readUser = archiveService.readUser(actual.id);
        assertNull(readUser);

    }

    @Test
    @Disabled
    void massAddRemoveUserServiceTest() {

        String separator = File.separator;
        String filename_first_name = "src/test/resources/first_name.txt";
        String filename_last_name = "src/test/resources/last_name.txt";

        filename_first_name = filename_first_name.replaceAll("/", Matcher.quoteReplacement(separator));
        filename_last_name = filename_last_name.replaceAll("/", Matcher.quoteReplacement(separator));

        List<String> first_name = readFileInList(filename_first_name);
        List<String> last_name = readFileInList(filename_last_name);

        int firstNameLen = first_name.size();
        int lastNameLen = last_name.size();
        Random rand = new Random();

        int userLengthStart = archiveService.countUser();
        log.info("Current user length: {}", userLengthStart);
        long startTime = System.nanoTime();


        List<Long> toDeleteIds = new ArrayList<>();
        log.info("start adding");

        for (int i = 0; i < 100000; i++) {
            int firstNameIndex = rand.nextInt(firstNameLen);
            int secondNameIndex = rand.nextInt(lastNameLen);
            String firstName = first_name.get(firstNameIndex);
            String lastName = last_name.get(secondNameIndex).split(" ")[0];
            log.info("Combine Name is {} {}", firstName, lastName);
            UserEntity origUserEntity = new UserEntity(null, firstName + " " + lastName);
            UserEntity actual = archiveService.createUser(origUserEntity);
            toDeleteIds.add(actual.id);
        }

        long alteredTime = System.nanoTime();
        int userLengthAltered = archiveService.countUser();
        log.info("Current user length: {}", userLengthAltered);
        log.info("start deleting");

        while (!toDeleteIds.isEmpty()) {
            long tobedeletedId = toDeleteIds.remove(toDeleteIds.size() - 1);
            archiveService.deleteUser(tobedeletedId);
        }

        log.info("action done");

        long endTime = System.nanoTime();
        int userLengthEnd = archiveService.countUser();
        log.info("Current user length: {}", userLengthEnd);

        assertEquals(userLengthStart, userLengthEnd);

        long durationAdd = (alteredTime - startTime);
        long durationRemove = (endTime - alteredTime);
        log.info("Time spend on adding action {} ms", durationAdd / 1000000);
        log.info("Time spend on removing action {} ms", durationRemove / 1000000);
    }


    @Test
//    @Disabled
    void massCreateUserAPIServiceTest() throws JsonProcessingException, InterruptedException {

        String separator = File.separator;
        String filename_first_name = "src/test/resources/first_name.txt";
        String filename_last_name = "src/test/resources/last_name.txt";

        filename_first_name = filename_first_name.replaceAll("/", Matcher.quoteReplacement(separator));
        filename_last_name = filename_last_name.replaceAll("/", Matcher.quoteReplacement(separator));

        List<String> first_name = readFileInList(filename_first_name);
        List<String> last_name = readFileInList(filename_last_name);

        int firstNameLen = first_name.size();
        int lastNameLen = last_name.size();
        Random rand = new Random();

        int userLengthStart = archiveService.countUser();
        log.info("Current user length: {}", userLengthStart);
        long startTime = System.nanoTime();


        List<Long> toDeleteIds = new ArrayList<>();
        log.info("start adding");

        for (int i = 0; i < 1000; i++) {
            List<UserEntity> userEntities = new ArrayList<>();
            for (int j = 0; j < 100; j++) {
                int firstNameIndex = rand.nextInt(firstNameLen);
                int secondNameIndex = rand.nextInt(lastNameLen);
                String firstName = first_name.get(firstNameIndex);
                String lastName = last_name.get(secondNameIndex).split(" ")[0];
                UserEntity origUserEntity = new UserEntity(null, firstName + " " + lastName);
                userEntities.add(origUserEntity);
            }
            archiveService.massCreateUser(userEntities);
        }

        int pendingCreations;
        do {
            pendingCreations = archiveService.checkPendingCreation();
            log.error("Current user length: {}", pendingCreations);
            Thread.sleep(2000);
        } while (pendingCreations > 0);

        long alteredTime = System.nanoTime();
        long durationAdd = (alteredTime - startTime);
        log.info("Time spend on adding action {} ms", durationAdd / 1000000);
    }

    @Test
    void searchUserServiceTest() {
        // Crete blank list to collect to delete user IDs when created
        List<Long> toDeleteIds = new ArrayList<>();
        String testName = "UniqueDingDongQiangJohn";
        UserEntity origUserEntity1 = new UserEntity(null, testName);
        UserEntity john1 = archiveService.createUser(origUserEntity1);
        toDeleteIds.add(john1.id);
        UserEntity origUserEntity2 = new UserEntity(null, testName);
        UserEntity john2 = archiveService.createUser(origUserEntity2);
        toDeleteIds.add(john1.id);

        List<UserEntity> results = archiveService.searchUser(new UserEntity(testName));

        assertNotEquals(0, results.size());
        log.info("result is {}", results);
        assertEquals(2, results.size());

        //thinking about changes
        //assertEquals(new UserEntity(502L, testName), results.get(0));
        assertEquals(testName, results.get(0).nickname);

        while (!toDeleteIds.isEmpty()) {
            long tobedeletedId = toDeleteIds.remove(toDeleteIds.size() - 1);
            archiveService.deleteUser(tobedeletedId);
        }
    }

    @Test
    void searchUserWithCacheTest() {
        UserEntity origUserEntity = new UserEntity(null, "test_nickname");
        UserEntity created = archiveService.createUser(origUserEntity);
        List<UserEntity> found = archiveService.searchUser(origUserEntity);

        assertEquals(1, found.size());
        assertEquals("test_nickname", found.get(0).nickname);

        found = archiveService.searchUser(origUserEntity);

        assertEquals(1, found.size());
        assertEquals("test_nickname", found.get(0).nickname);
    }

    @Test
    void insertTest() {
        //TODO
        // article id从 10000 到 99999, 一共9万个article
        // 每篇article, 给100个动作. 是like还是dislike, 随机
        // 插入好之后, 再read某个article的like数. 注意不要读第一篇或最后一篇, 随机读
        // 计算一下耗时

        for (long articleId = 10000; articleId < 99999; articleId++) {
            for (int round = 0; round < 10; round++){
                if ((articleId-10000)%1000==0){
                    log.info("updating {}th record",articleId);
                }
                boolean like = false;
                if(Math.random() < 0.5) {
                    like = true;
                }
                String clientId = "127.0.0.1";
                Long createdAt = System.currentTimeMillis();
                jdbcTemplate.update("INSERT INTO article_likes(article_id, is_like,client_id,created_at) VALUES(?,?,?,?)", articleId, like,clientId,createdAt);
            }
        }
    }

    @Test
    void likeArticleTest() {
        long startTime = System.nanoTime();

        long articleId = 42536;
        Map<Long, Integer> result = archiveService.readLikeArticle(articleId);
        long endTime = System.nanoTime();
        log.info("time spend {} ms",(endTime-startTime)/1000000);
    }

    @Test
    void tributeArchiveInfoTest() throws IOException {
        String url = "https://mp.weixin.qq.com/s?__biz=MzIwMDkwNDc5Mg==&mid=2247483688&idx=2&sn=b51cda71bc4ca16f2b8bc5fca9322a50&chksm=96f7581fa180d109aeffe827abdd5f9e0bde4e611b8b701b62d08f5ea4e074bbfd1c0a4f8707&scene=21";
        ArchiveBasicInfoEntity archiveBasicInfoEntity = archiveService.tributeArchiveInfo(url);
        assertEquals("浅析「老道消息」写作方法论", archiveBasicInfoEntity.title);
        assertEquals("三表", archiveBasicInfoEntity.author);
        assertEquals("三表蛇门阵", archiveBasicInfoEntity.publisher);
        assertEquals("2017-03", archiveBasicInfoEntity.date);

//        String html = Files.readString(Paths.get("/Users/wang.boyang/Projects/mine/lanting-server/temp.html"));
//        String regex = ",i=\"(\\d\\d\\d\\d-\\d\\d-\\d\\d)\";";
//        Matcher matcher = Pattern.compile(regex).matcher(html);
//        if (matcher.find()) {
//            log.error(matcher.group(1));
//        }

        url = "https://mp.weixin.qq.com/s/vWekfTVQEafa6-uMvx9ylw";
        archiveBasicInfoEntity = archiveService.tributeArchiveInfo(url);
        assertEquals("摸着中国过河：越南改革开放简史", archiveBasicInfoEntity.title);
        assertEquals("陈畅, 李健华", archiveBasicInfoEntity.author);
        assertEquals("远川研究所", archiveBasicInfoEntity.publisher);
        assertEquals("2020-12", archiveBasicInfoEntity.date);

        url = "https://www.youtube.com/watch?v=2WhLkLD9P9w";
        archiveBasicInfoEntity = archiveService.tributeArchiveInfo(url);
        assertEquals("Speedrunning to have sex in the Fallout series (SPEEDRUN EXPLAINED - World Record) - YouTube", archiveBasicInfoEntity.title);
    }

    private static ArchiveTributeInfoEntity getArchiveTributeInfoEntity() {
        ArchiveTributeInfoEntity archiveTributeInfoEntity = new ArchiveTributeInfoEntity();
        archiveTributeInfoEntity.author = "1.1, 1.2";
        archiveTributeInfoEntity.chapter = "2";
        archiveTributeInfoEntity.date = "3";
        archiveTributeInfoEntity.link = "https://mp.weixin.qq.com/s/9huki5pnc8VmHxp1RJSQDQ";
        archiveTributeInfoEntity.publisher = "4";
        archiveTributeInfoEntity.remarks = "5";
        archiveTributeInfoEntity.tag = "6.1, 6.2";
        archiveTributeInfoEntity.remarks = "7";
        return archiveTributeInfoEntity;
    }

    @Test
    void tributeTempScriptTest() {
        ArchiveTributeInfoEntity archiveTributeInfoEntity = getArchiveTributeInfoEntity();
        String s = archiveService.generateTempTributeScriptContent(archiveTributeInfoEntity);
        System.out.println(s);
    }

    @Test
    void tributeArchiveSaveTest() {
        ArchiveTributeInfoEntity archiveTributeInfoEntity = getArchiveTributeInfoEntity();
        archiveService.tributeArchiveSave(archiveTributeInfoEntity);
    }

    @Test
    void searchKeywordTest() {
        assertEquals(1, archiveService.searchKeywordCreate("test1"));
        List<SearchKeywordEntity> searchKeywordEntities = archiveService.searchKeywordRead();
        log.info("{}", searchKeywordEntities);
        assertEquals("test1", searchKeywordEntities.get(0).keyword);

        assertEquals(1, archiveService.searchKeywordCreate("test2"));
        searchKeywordEntities = archiveService.searchKeywordRead();
        log.info("{}", searchKeywordEntities);
    }

    @Test
    void archivesReadTest() {
        archiveService.archivesRead();
    }
}