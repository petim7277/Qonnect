package com.example.qonnect.application.input;

import com.example.qonnect.domain.models.User;

public interface ViewUserProfileUseCase {

    User viewUserProfile(String email);

}
