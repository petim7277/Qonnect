package com.example.qonnect.application.input;

import com.example.qonnect.domain.exceptions.AuthenticationException;
import com.example.qonnect.domain.exceptions.UserNotFoundException;
import com.example.qonnect.domain.models.User;
import org.apache.http.auth.InvalidCredentialsException;

public interface LoginUseCase {

    User login(User user) throws UserNotFoundException, AuthenticationException, InvalidCredentialsException;

}
