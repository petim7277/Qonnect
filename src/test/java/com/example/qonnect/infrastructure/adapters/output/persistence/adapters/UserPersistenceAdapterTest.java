package com.example.qonnect.infrastructure.adapters.output.persistence.adapters;

import com.example.qonnect.domain.models.enums.Role;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.UserEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.mappers.UserPersistenceMapper;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
class UserPersistenceAdapterTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPersistenceMapper userPersistenceMapper;

    @Autowired
    private UserPersistenceAdapter userPersistenceAdapter;

    private User testUser;

    @BeforeEach
    void setup() {
        UserEntity userEntity = new UserEntity();
        userEntity.setFirstName("Test");
        userEntity.setLastName("User");
        userEntity.setEmail("test@example.com");
        userEntity.setRole(Role.QA_ENGINEER);

        userRepository.save(userEntity);

        testUser = userPersistenceMapper.toUser(userEntity);
    }

    @Test
    void getUserByEmail_shouldReturnUser() {
        User found = userPersistenceAdapter.getUserByEmail(testUser.getEmail());

        assertNotNull(found);
        assertEquals("test@example.com", found.getEmail());
    }

    @Test
    void saveUser_shouldPersistUser() {
        User newUser = User.builder()
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .role(Role.DEVELOPER)
                .build();

        User saved = userPersistenceAdapter.saveUser(newUser);

        assertNotNull(saved.getId());
        assertEquals("jane@example.com", saved.getEmail());
    }

    @Test
    void userExistsByEmail_shouldReturnTrue() {
        assertTrue(userPersistenceAdapter.userExistsByEmail(testUser.getEmail()));
    }
}


