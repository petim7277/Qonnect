package com.example.qonnect.infrastructure.adapters.output.persistence.adapters;

import com.example.qonnect.application.output.OrganizationOutputPort;
import com.example.qonnect.domain.exceptions.ProjectNotFoundException;
import com.example.qonnect.domain.models.Organization;
import com.example.qonnect.domain.models.Project;
import com.example.qonnect.domain.models.enums.Role;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.OrganizationEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.UserEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.mappers.OrganizationPersistenceMapper;
import com.example.qonnect.infrastructure.adapters.output.persistence.mappers.UserPersistenceMapper;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.OrganizationRepository;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserPersistenceAdapterTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserPersistenceAdapter userPersistenceAdapter;
    @Autowired
    private OrganizationOutputPort organizationOutputPort;
    @Autowired
    private OrganizationPersistenceMapper organizationPersistenceMapper;

    private User savedUser;

    @BeforeEach
    void setup() {
        User userEntity = new User();
        userEntity.setFirstName("Test");
        userEntity.setLastName("User");
        userEntity.setEmail("test@example.com");
        userEntity.setRole(Role.QA_ENGINEER);

        savedUser = userPersistenceAdapter.saveUser(userEntity);
    }
    @AfterEach
    void tearDown() {
        userPersistenceAdapter.deleteUserById(savedUser.getId());
    }

    @Test
    void getUserByEmail_shouldReturnUser() {
        User found = userPersistenceAdapter.getUserByEmail(savedUser.getEmail());

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
        userPersistenceAdapter.deleteUserById(saved.getId());
    }

    @Test
    void userExistsByEmail_shouldReturnTrue() {
        assertTrue(userPersistenceAdapter.userExistsByEmail(savedUser.getEmail()));
    }

    @Test
    void getUserById_shouldReturnUser() {
        User result = userPersistenceAdapter.getUserById(savedUser.getId());

        assertNotNull(result);
        assertEquals(savedUser.getId(), result.getId());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void getUserById_shouldThrowIfNotFound() {
        Long invalidId = 999999L;

        assertThrows(RuntimeException.class, () -> userPersistenceAdapter.getUserById(invalidId));
    }


    @Test
    void findAllByOrganizationId_shouldReturnPagedUsers() {
        Pageable pageable = PageRequest.of(0, 10);

        Organization orgEntity = Organization.builder()
                .name("Test Org")
                .build();

        Organization savedOrg = organizationOutputPort.saveOrganization(orgEntity);

        UserEntity user1 = new UserEntity();
        user1.setFirstName("A");
        user1.setLastName("B");
        user1.setEmail("a@example.com");
        user1.setOrganization(organizationPersistenceMapper.toOrganizationEntity( savedOrg));
        user1.setRole(Role.ADMIN);

        UserEntity user2 = new UserEntity();
        user2.setFirstName("C");
        user2.setLastName("D");
        user2.setEmail("c@example.com");
        user2.setOrganization(organizationPersistenceMapper.toOrganizationEntity( savedOrg));
        user2.setRole(Role.DEVELOPER);

        userRepository.save(user1);
        userRepository.save(user2);

        Page<User> result = userPersistenceAdapter.findAllByOrganizationId(savedOrg.getId(), pageable);

        assertNotNull(result);
        assertTrue(result.getTotalElements() >= 2);
        assertTrue(result.getContent().stream().anyMatch(u -> u.getEmail().equals("a@example.com")));
        assertTrue(result.getContent().stream().anyMatch(u -> u.getEmail().equals("c@example.com")));
        userPersistenceAdapter.deleteUserById(user1.getId());
        userPersistenceAdapter.deleteUserById(user2.getId());
    }

}
