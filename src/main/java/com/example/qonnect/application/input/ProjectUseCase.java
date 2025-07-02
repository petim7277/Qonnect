package com.example.qonnect.application.input;

import com.example.qonnect.domain.models.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectUseCase {


    Page<Project> getAllProjects(Pageable pageable);
}
