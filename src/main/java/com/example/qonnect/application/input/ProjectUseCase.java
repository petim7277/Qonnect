package com.example.qonnect.application.input;

import com.example.qonnect.domain.models.Project;
import com.example.qonnect.domain.models.User;

public interface ProjectUseCase {

    Project createProject(User user, Project project);

}
