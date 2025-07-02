package com.example.qonnect.infrastructure.adapters.output.persistence.adapters;

import com.example.qonnect.domain.exceptions.UserNotFoundException;
import com.example.qonnect.domain.models.Role;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.UserEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.mappers.UserPersistenceMapper;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserPersistenceAdapterTest {

    @Mock  private UserRepository userRepository;
    @Mock  private UserPersistenceMapper userPersistenceMapper;

    @InjectMocks
    private UserPersistenceAdapter userPersistenceAdapter;

    private static final String TEST_EMAIL = "test@example.com";

    private UserEntity userEntity;
    private User domainUser;

    @BeforeEach
    void init() {

        userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setFirstName("Test");
        userEntity.setLastName("User");
        userEntity.setEmail(TEST_EMAIL);
        userEntity.setRole(Role.DEVELOPER);
        userEntity.setInviteToken("INV123");
        userEntity.setTokenExpiresAt(LocalDateTime.now().plusDays(7));


        domainUser = new User();
        domainUser.setId(1L);
        domainUser.setFirstName("Test");
        domainUser.setLastName("User");
        domainUser.setEmail(TEST_EMAIL);
        domainUser.setRole(Role.DEVELOPER);
        domainUser.setInviteToken("INV123");
        domainUser.setTokenExpiresAt(userEntity.getTokenExpiresAt());
    }

    // ───────────────────────────────────────────────────────────────────────────────
    // getUserByEmail()
    // ───────────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("getUserByEmail(): returns user when email exists")
    void getUserByEmail_success() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(userEntity));
        when(userPersistenceMapper.toUser(userEntity)).thenReturn(domainUser);

        User result = userPersistenceAdapter.getUserByEmail(TEST_EMAIL);

        assertThat(result).isNotNull()
                .extracting(User::getEmail, User::getRole)
                .containsExactly(TEST_EMAIL, Role.DEVELOPER);
    }

    @Test
    @DisplayName("getUserByEmail(): throws when email not found")
    void getUserByEmail_notFound() {
        when(userRepository.findByEmail("missing@qonnect.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userPersistenceAdapter.getUserByEmail("missing@qonnect.com"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");
    }


    @Test
    @DisplayName("saveUser(): maps → persists → maps back")
    void saveUser_success() {
        when(userPersistenceMapper.toUserEntity(domainUser)).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userPersistenceMapper.toUser(userEntity)).thenReturn(domainUser);

        User saved = userPersistenceAdapter.saveUser(domainUser);

        verify(userRepository).save(userEntity);
        assertThat(saved).isEqualTo(domainUser);
    }


    @Test
    void userExistsByEmail_true() {
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        assertThat(userPersistenceAdapter.userExistsByEmail(TEST_EMAIL))
                .isTrue();
    }


    @Test
    void getUserByInviteToken_found() {
        when(userRepository.findByInviteToken("INV123")).thenReturn(Optional.of(userEntity));
        when(userPersistenceMapper.toUser(userEntity)).thenReturn(domainUser);

        User res = userPersistenceAdapter.getUserByInviteToken("INV123");

        assertThat(res.getInviteToken()).isEqualTo("INV123");
    }

    @Test
    void getUserByInviteToken_notFound() {
        when(userRepository.findByInviteToken("BAD")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userPersistenceAdapter.getUserByInviteToken("BAD"))
                .isInstanceOf(UserNotFoundException.class);
    }
}
