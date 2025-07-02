package com.example.qonnect.infrastructure.adapters.output.persistence.repositories;

import com.example.qonnect.domain.models.Organization;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.OrganizationEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {


    @Query("""
    SELECT COUNT(p) > 0
    FROM ProjectEntity p
    WHERE p.name = :name
      AND p.organization.id = :orgId
""")
    boolean existsProjectNameInOrganization(@Param("name") String name,
                                            @Param("orgId") Long organizationId);

    Page<ProjectEntity> findByOrganizationId(Long organizationId, Pageable pageable);
}
