package wiki.lanting.services;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import wiki.lanting.models.UserEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    }
}