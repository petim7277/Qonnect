package com.example.qonnect.infrastructure.adapters.output.persistence.adapters;


import com.example.qonnect.domain.exceptions.OrganizationNotFoundException;
import com.example.qonnect.domain.models.Organization;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.domain.models.enums.Role;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.OrganizationEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.UserEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.mappers.UserPersistenceMapper;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.OrganizationRepository;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrganizationPersistenceAdapterTest {

    @Autowired
    private OrganizationPersistenceAdapter adapter;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private UserPersistenceAdapter userAdapter;

    @Autowired
    private UserPersistenceMapper userPersistenceMapper;

    @Autowired
    private UserRepository userRepository;

    private Organization organization;
    private User user;
    private OrganizationEntity savedOrg;

    @BeforeEach
    void setUp() {
        OrganizationEntity orgEntity = OrganizationEntity.builder()
                .name("Test Organization")
                .users(new ArrayList<>())
                .build();

      savedOrg = organizationRepository.save(orgEntity);

        organization = Organization.builder()
                .id(savedOrg.getId())
                .name(savedOrg.getName())
                .build();


        user = new User();
        user.setEmail("member@example.com");
        user.setFirstName("Member");
        user.setLastName("User");
        user.setPassword("password123");
        user.setRole(Role.QA_ENGINEER);
        user.setOrganization(organization);

        user = userAdapter.saveUser(user);

        UserEntity userEntity = userPersistenceMapper.toUserEntity(user);
        savedOrg.setUsers(List.of(userEntity));
        organizationRepository.save(savedOrg);
    }


    @AfterEach
    void tearDown() {
        organizationRepository.deleteById(savedOrg.getId());
        organizationRepository.deleteById(organization.getId());
        userRepository.deleteById(user.getId());
    }

    @Test
    void shouldSaveOrganizationSuccessfully() {
        Organization org = Organization.builder()
                .name("Another Org")
                .build();


        Organization saved = adapter.saveOrganization(org);

        assertNotNull(saved.getId());
        assertEquals("Another Org", saved.getName());
        adapter.deleteById(saved.getId());
    }

    @Test
    void shouldReturnTrueWhenOrganizationExistsByName() {
        assertTrue(adapter.existsByName("Test Organization"));
    }

    @Test
    void shouldReturnFalseWhenOrganizationDoesNotExistByName() {
        assertFalse(adapter.existsByName("Nonexistent Org"));
    }

    @Test
    void shouldFindOrganizationById() {
        Organization found = adapter.getOrganizationById(organization.getId());

        assertEquals(organization.getId(), found.getId());
        assertEquals("Test Organization", found.getName());
    }

    @Test
    void shouldThrowExceptionWhenOrganizationNotFoundById() {
        Long invalidId = 999999L;

        OrganizationNotFoundException ex = assertThrows(OrganizationNotFoundException.class,
                () -> adapter.getOrganizationById(invalidId));

        assertEquals(ErrorMessages.ORGANIZATION_NOT_FOUND, ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void shouldGetOrganizationByName() {
        Organization found = adapter.getOrganizationByName("Test Organization");

        assertEquals(organization.getId(), found.getId());
        assertEquals("Test Organization", found.getName());
    }

    @Test
    void shouldRemoveUserFromOrganization() {
        assertEquals(organization.getId(), user.getOrganization().getId());
        adapter.removeUserFromOrganization(user,organization);
        User updated = userAdapter.getUserById(user.getId());
        assertNull(updated.getOrganization());
    }
}

