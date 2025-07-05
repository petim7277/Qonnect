package com.example.qonnect.infrastructure.adapters.output.persistence.adapters;

import com.example.qonnect.application.output.OrganizationOutputPort;
import com.example.qonnect.application.output.UserOutputPort;
import com.example.qonnect.domain.exceptions.OrganizationNotFoundException;
import com.example.qonnect.domain.models.Organization;
import com.example.qonnect.domain.models.User;
import com.example.qonnect.infrastructure.adapters.input.rest.messages.ErrorMessages;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.OrganizationEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.UserEntity;
import com.example.qonnect.infrastructure.adapters.output.persistence.mappers.OrganizationPersistenceMapper;
import com.example.qonnect.infrastructure.adapters.output.persistence.repositories.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrganizationPersistenceAdapter implements OrganizationOutputPort {

    private final OrganizationPersistenceMapper organizationPersistenceMapper;
    private final OrganizationRepository organizationRepository;
    private final UserOutputPort userOutputPort;


    @Override
    public Organization saveOrganization(Organization organization) {
        log.info("Saving user: {}", organization);

        OrganizationEntity entity = organizationPersistenceMapper.toOrganizationEntity(organization);
        log.info("Mapped to entity: {}", entity);

        entity = organizationRepository.save(entity);
        log.info("Saved entity: {}", entity);

        Organization savedOrganization = organizationPersistenceMapper.toOrganization(entity);
        log.info("Mapped back to domain user: {}", savedOrganization);

        return savedOrganization;
    }

    @Override
    public Organization getOrganizationByName(String name) {
        OrganizationEntity foundOrganization = organizationRepository.findByName(name).orElseThrow(() -> new OrganizationNotFoundException(ErrorMessages.ORGANIZATION_NOT_FOUND, HttpStatus.NOT_FOUND));
        return organizationPersistenceMapper.toOrganization(foundOrganization);
    }

    @Override
    public boolean existsByName(String name) {
        return organizationRepository.existsByName(name);
    }

    @Override
    public Organization getOrganizationById(Long id) {
        OrganizationEntity foundOrganization = organizationRepository.findById(id)
                .orElseThrow(() -> new OrganizationNotFoundException(
                        ErrorMessages.ORGANIZATION_NOT_FOUND, HttpStatus.NOT_FOUND
                ));
        return organizationPersistenceMapper.toOrganization(foundOrganization);
    }

    @Override
    public void removeUserFromOrganization(User userToBeRemoved) {
        OrganizationEntity organization = organizationPersistenceMapper.toOrganizationEntity(userToBeRemoved.getOrganization());
        if (organization.getUsers() == null) {
            organization.setUsers(new ArrayList<>());
        }
        organization.getUsers().removeIf(u -> u.getId().equals(userToBeRemoved.getId()));
        userToBeRemoved.setOrganization(null);
        userOutputPort.saveUser(userToBeRemoved);
        organizationRepository.save(organization);
    }


}
