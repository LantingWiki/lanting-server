package wiki.lanting.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import wiki.lanting.models.UserEntity;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class UserServiceTest {

    @Autowired
    UserService userService;

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
        UserEntity actual = userService.createUser(origUserEntity);
        assertEquals("test_nickname", actual.nickname);

        UserEntity readUser = userService.readUser(actual.id);
        assertEquals(actual, readUser);

        actual.nickname = "test_nickname2";
        int i = userService.updateUser(actual);
        assertEquals(1, i);

        readUser = userService.readUser(actual.id);
        assertEquals("test_nickname2", readUser.nickname);

        i = userService.deleteUser(actual.id);
        assertEquals(1, i);

        readUser = userService.readUser(actual.id);
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

        int userLengthStart = userService.countUser();
        log.info("Current user length: {}", userLengthStart);
        long startTime = System.nanoTime();


        List<Long> toDeleteIds = new ArrayList<>();
        log.info("start adding");

        for (int i = 0; i < 1000; i++) {
            int firstNameIndex = rand.nextInt(firstNameLen);
            int secondNameIndex = rand.nextInt(lastNameLen);
            String firstName = first_name.get(firstNameIndex);
            String lastName = last_name.get(secondNameIndex).split(" ")[0];
            log.info("Combine Name is {} {}", firstName, lastName);
            UserEntity origUserEntity = new UserEntity(null, firstName + " " + lastName);
            UserEntity actual = userService.createUser(origUserEntity);
            toDeleteIds.add(actual.id);
        }

        long alteredTime = System.nanoTime();
        int userLengthAltered = userService.countUser();
        log.info("Current user length: {}", userLengthAltered);
        log.info("start deleting");

        while (!toDeleteIds.isEmpty()) {
            long tobedeletedId = toDeleteIds.remove(toDeleteIds.size() - 1);
            userService.deleteUser(tobedeletedId);
        }

        log.info("action done");

        long endTime = System.nanoTime();
        int userLengthEnd = userService.countUser();
        log.info("Current user length: {}", userLengthEnd);

        assertEquals(userLengthStart, userLengthEnd);

        long durationAdd = (alteredTime - startTime);
        long durationRemove = (endTime - alteredTime);
        log.info("Time spend on adding action {} ms", durationAdd / 1000000);
        log.info("Time spend on removing action {} ms", durationRemove / 1000000);
    }

    @Test
    void searchUserServiceTest() {
        // Crete blank list to collect to delete user IDs when created
        List<Long> toDeleteIds = new ArrayList<>();
        String testName = "UniqueDingDongQiangJohn";
        UserEntity origUserEntity1 = new UserEntity(null, testName);
        UserEntity john1 = userService.createUser(origUserEntity1);
        toDeleteIds.add(john1.id);
        UserEntity origUserEntity2 = new UserEntity(null, testName);
        UserEntity john2 = userService.createUser(origUserEntity2);
        toDeleteIds.add(john1.id);

        List<UserEntity> results = userService.searchUser(new UserEntity(testName));

        assertNotEquals(0, results.size());
        log.info("result is {}", results);
        assertEquals(2, results.size());

        //thinking about changes
        //assertEquals(new UserEntity(502L, testName), results.get(0));
        assertEquals(testName, results.get(0).nickname);

        while (!toDeleteIds.isEmpty()) {
            long tobedeletedId = toDeleteIds.remove(toDeleteIds.size() - 1);
            userService.deleteUser(tobedeletedId);
        }
    }

    @Test
    void searchUserWithCacheTest() {
        UserEntity origUserEntity = new UserEntity(null, "test_nickname");
        UserEntity created = userService.createUser(origUserEntity);
        List<UserEntity> found = userService.searchUser(origUserEntity);

        assertEquals(1, found.size());
        assertEquals("test_nickname", found.get(0).nickname);

        found = userService.searchUser(origUserEntity);

        assertEquals(1, found.size());
        assertEquals("test_nickname", found.get(0).nickname);
    }
}