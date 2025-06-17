package com.example.qonnect.infrastructure.adapters.output.persistence.adapters;

import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.exceptions.UserNotFoundException;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.UserEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.mappers.UserPersistenceMapper;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class UserPersistenceAdapter implements UserOutputPort {
    private final UserRepository userRepository;
    private final UserPersistenceMapper userPersistenceMapper;
    @Override
    public User getUserByEmail(String email) {
        Optional<UserEntity> foundUser =  userRepository.findByEmail(email);
        if (foundUser.isEmpty()) {throw new UserNotFoundException(ErrorMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
        return userPersistenceMapper.toUser(foundUser.get());
    }
}
