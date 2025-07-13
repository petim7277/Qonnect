package com.example.qonnect.application.output;

import com.example.qonnect.domain.exceptions.UserNotFoundException;
import com.example.qonnect.domain.models.User;

import java.util.Optional;

public interface IdentityManagementOutputPort {

    User createUser(User user);

    boolean doesUserExist(String email);

    User login(User userIdentity);

    void deleteUser(User user);

    Optional<User> getUserByEmail(String email) throws UserNotFoundException;

    void changePassword(User user);

    void resetPassword(User user);

    void logout(User user, String token);


    void activateUser(User user);

}
