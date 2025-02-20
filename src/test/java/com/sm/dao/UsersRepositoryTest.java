package com.sm.dao;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.UUID;

@DataJpaTest
@TestPropertySource(locations = "/application-test.properties")
public class UsersRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private UsersRepository usersRepository;

    private final String userId1 = UUID.randomUUID().toString();
    private final String userId2 = UUID.randomUUID().toString();
    private final String email1 = "test@test.com";
    private final String email2 = "test2@test.com";

    @BeforeEach
    void setup() {
        // Creating first user
        User user = new User();
        user.setUserId(userId1);
        user.setEmail(email1);
        user.setEncryptedPassword("12345678");
        user.setFirstName("ali");
        user.setLastName("ahmed");
        testEntityManager.persistAndFlush(user);

        // Creating second user
        User user2 = new User();
        user2.setUserId(userId2);
        user2.setEmail(email2);
        user2.setEncryptedPassword("abcdefg1");
        user2.setFirstName("John");
        user2.setLastName("Sears");
        testEntityManager.persistAndFlush(user2);
    }

    @Test
    void testFindByEmail_whenGivenCorrectEmail_returnsUserEntity() {
        // Act
        User storedUser = usersRepository.findByEmail(email1);

        // Assert
        Assertions.assertEquals(email1, storedUser.getEmail(),
                "The returned email address does not match the expected value");
    }

    @Test
    void testFindByUserId_whenGivenCorrectUserId_returnsUserEntity() {
        // Act
        User storedUser = usersRepository.findByUserId(userId2);

        // Assert
        Assertions.assertNotNull(storedUser,
                "UserEntity object should not be null");
        Assertions.assertEquals(userId2, storedUser.getUserId(),
                "Returned userId does not much expected value");
    }

    @Test
    void testFindUsersWithEmailEndsWith_whenGiveEmailDomain_returnsUsersWithGivenDomain() {
        // Arrange
        User user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setEmail("test@gmail.com");
        user.setEncryptedPassword("123456789");
        user.setFirstName("Sergey");
        user.setLastName("Kargopolov");
        testEntityManager.persistAndFlush(user);

        String emailDomainName = "@gmail.com";

        // Act
        List<User> users = usersRepository.findUsersWithEmailEndingWith(emailDomainName);

        // Assert
        Assertions.assertEquals(1, users.size(),
                "There should be one user in the list");
        Assertions.assertTrue(users.get(0).getEmail().endsWith(emailDomainName),
                "User's email does not end with target domain name");
    }
}
