package com.example.qonnect.application.input;

import com.example.qonnect.domain.models.User;

public interface LogoutUseCase {

//    void logout(User user, String refreshToken);

    void logout(User user, String refreshToken, String accessToken);
}
