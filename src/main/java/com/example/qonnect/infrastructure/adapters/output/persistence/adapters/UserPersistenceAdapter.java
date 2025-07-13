package com.example.qonnect.infrastructure.adapters.output.persistence.adapters;

import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.exceptions.UserNotFoundException;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.UserEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.mappers.UserPersistenceMapper;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.example.qonnect.domain.validators.InputValidator.validateInput;

@Slf4j
@Component
@AllArgsConstructor
public class UserPersistenceAdapter implements UserOutputPort {
    private final UserRepository userRepository;
    private final UserPersistenceMapper userPersistenceMapper;


    @Transactional(readOnly = true)
    @Override
    public User getUserByEmail(String email) {
        log.info("getUserByEmail ----{}", email);
        Optional<UserEntity> foundUser =  userRepository.findByEmail(email);
        if (foundUser.isEmpty()) {
            log.info("-----> user not found");
            throw new UserNotFoundException(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        return userPersistenceMapper.toUser(foundUser.get());
    }


    @Override
    public User saveUser(User user) {
        log.info("Saving user: {}", user);

        UserEntity entity = userPersistenceMapper.toUserEntity(user);
        log.info("Mapped to entity: {}", entity);

        entity = userRepository.save(entity);
        log.info("Saved entity: {}", entity);

        User savedUser = userPersistenceMapper.toUser(entity);
        log.info("Mapped back to domain user: {}", savedUser);

        return savedUser;
    }

    @Override
    public boolean userExistsByEmail(String email) {
        validateInput(email);
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existById(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    public User getUserByInviteToken(String inviteToken) {
        log.info("Searching for user with inviteToken: {}", inviteToken);

        Optional<UserEntity> entity = userRepository.findByInviteToken(inviteToken);

        if (entity.isEmpty()) {
            log.warn("No user found with inviteToken: {}", inviteToken);
            throw new UserNotFoundException(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }

        return userPersistenceMapper.toUser(entity.get());
    }

    @Override
    public User getUserById(Long userId) {
        UserEntity entity = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND));
        return userPersistenceMapper.toUser(entity);
    }

    @Override
    public Page<User> findAllByOrganizationId(Long organizationId, Pageable pageable) {
        Page<UserEntity> userEntities = userRepository.findUserEntitiesByOrganizationId(organizationId, pageable);
        return userEntities.map(userPersistenceMapper::toUser);
    }

    @Override
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

}
