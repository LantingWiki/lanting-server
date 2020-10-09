package wiki.lanting.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import wiki.lanting.models.UserEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;

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


    public static List<String> readFileInList(String fileName)
    {

        List<String> lines = Collections.emptyList();
        try
        {
            lines = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
        }

        catch (IOException e)
        {

            // do something
            e.printStackTrace();
        }
        return lines;
    }

    @Test
    void userServiceTest() {
        UserEntity origUserEntity = new UserEntity(-1L, "test_nickname");
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

        List<String> first_name = readFileInList("src\\test\\java\\wiki\\lanting\\constants\\first_name.txt");

        List<String> last_name = readFileInList("src\\test\\java\\wiki\\lanting\\constants\\last_name.txt");

        log.info(" first-name file {}.",first_name.get(0) );

        log.info(" last-name file {} with length {}.", last_name.get(0).split(" ")[0], last_name.size());
        int userLength = userService.getUserRecordLength();
        log.info("Current user length: {}", userLength);
        long startTime = System.nanoTime();
        if (userLength < 1000000){
            int counter = 0;
            for (int j = 0; j < 100000000; j++) {
                counter += 1;
            }
            log.info("{} operations done.",counter );
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        log.info("Time spend on alter action {} ms", duration/1000000 );
        int userLengthAfter = userService.getUserRecordLength();
        log.info("Current user length after alter: {}", userLengthAfter);

    }

    @Test
    void searchUserServiceTest() {
        List<UserEntity> results = userService.searchUser(new UserEntity("Jack"));
        assertEquals(1, results.size());
        assertEquals(new UserEntity(10000L, "Jack"), results.get(0));
    }
}