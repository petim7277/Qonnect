package com.example.qonnect.infrastructure.adapters.output.persistence.adapters;
import com.example.qonnect.domain.exceptions.UserNotFoundException;
import com.example.qonnect.domain.models.Role;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.UserEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.mappers.UserPersistenceMapper;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserPersistenceAdapterTest {

    @Autowired
    private UserRepository userRepository;

//    @Autowired(required = false)
//    private UserPersistenceMapper userPersistenceMapper;

    @Autowired
    private UserPersistenceAdapter userPersistenceAdapter;

    private UserEntity testUserEntity;

    private final String TEST_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        testUserEntity = new UserEntity();
        testUserEntity.setFirstName("Test");
        testUserEntity.setLastName("User");
        testUserEntity.setEmail(TEST_EMAIL);
        testUserEntity.setKeycloakId("test-keycloak-id");
        testUserEntity.setRole(Role.DEVELOPER);

        userRepository.save(testUserEntity);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should return user when email exists")
    void getUserByEmail_WhenEmailExists_ReturnsUser() {
        User foundUser = userPersistenceAdapter.getUserByEmail(TEST_EMAIL);
        assertNotNull(foundUser);
        assertEquals(TEST_EMAIL, foundUser.getEmail());
        assertEquals("Test", foundUser.getFirstName());
        assertEquals("User", foundUser.getLastName());
        assertEquals(Role.DEVELOPER, foundUser.getRole());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when email does not exist")
    void getUserByEmail_WhenEmailDoesNotExist_ThrowsUserNotFoundException() {
        String nonExistentEmail = "nonexistent@example.com";

        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> userPersistenceAdapter.getUserByEmail(nonExistentEmail)
        );

        assertNotNull(exception);
        assertEquals("User not found", exception.getMessage());
    }
}