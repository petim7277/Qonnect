package com.example.qonnect.application.input;

import com.example.qonnect.domain.models.Role;
import com.example.qonnect.domain.models.User;


public interface InviteUserUseCase {
    void inviteUser(User inviter, String inviteeEmail, Role role);
}
