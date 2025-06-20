package com.example.qonnect.application.output;

import com.example.qonnect.domain.models.User;

public interface UserOutputPort {
    User getUserByEmail(String email);


    User saveUser(User user);

    boolean userExistsByEmail(String email);

}
