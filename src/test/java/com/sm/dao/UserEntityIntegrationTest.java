package com.sm.dao;

import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@TestPropertySource(locations = "/application-test.properties")
public class UserEntityIntegrationTest {

    @Autowired
    private TestEntityManager testEntityManager;

    User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setUserId(UUID.randomUUID().toString());
        user.setFirstName("ali");
        user.setLastName("ahmed");
        user.setEmail("test@test.com");
        user.setEncryptedPassword("12345678");
    }

    @Test
    void testUserEntity_whenValidUserDetailsProvided_shouldReturnStoredUserDetails() {
        // Act
        User storedUser = testEntityManager.persistAndFlush(user);

        // Assert
        Assertions.assertTrue(storedUser.getId() > 0);
        Assertions.assertEquals(user.getUserId(), storedUser.getUserId());
        Assertions.assertEquals(user.getFirstName(), storedUser.getFirstName());
        Assertions.assertEquals(user.getLastName(), storedUser.getLastName());
        Assertions.assertEquals(user.getEmail(), storedUser.getEmail());
        Assertions.assertEquals(user.getEncryptedPassword(), storedUser.getEncryptedPassword());
    }

    @Test
    void testUserEntity_whenFirstNameIsTooLong_shouldThrowException() {
        // Arrange
        user.setFirstName("123456789012345678901234567890123456789012345678901234567890");

        // Assert & Act
        Assertions.assertThrows(PersistenceException.class, ()->{
            testEntityManager.persistAndFlush(user);
        }, "Was expecting a PersistenceException to be thrown.");
    }

    @Test
    void testUserEntity_whenExistingUserIdProvided_shouldThrowException() {
        // Arrange
        // Create and Persist a new User Entity
        User newEntity = new User();
        newEntity.setUserId("1");
        newEntity.setEmail("test2@test.com");
        newEntity.setFirstName("test");
        newEntity.setLastName("test");
        newEntity.setEncryptedPassword("test");
        testEntityManager.persistAndFlush(newEntity);

        // Update existing user entity with the same user id
        user.setUserId("1");

        // Act & Assert
        assertThrows(PersistenceException.class, ()-> {
            testEntityManager.persistAndFlush(user);
        }, "Expected PersistenceException to be thrown here");
    }
}
