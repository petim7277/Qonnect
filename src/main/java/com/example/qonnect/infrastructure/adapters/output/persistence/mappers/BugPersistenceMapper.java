package com.example.qonnect.infrastructure.adapters.output.persistence.mappers;

import com.example.qonnect.domain.models.Bug;
import com.example.qonnect.infrastructure.adapters.output.persistence.entities.BugEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = { ProjectPersistenceMapper.class, UserPersistenceMapper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface BugPersistenceMapper {

    Bug toBug(BugEntity entity);

    BugEntity toBugEntity(Bug bug);

    List<Bug> toBugList(List<BugEntity> entities);

    List<BugEntity> toBugEntityList(List<Bug> bugs);
}
