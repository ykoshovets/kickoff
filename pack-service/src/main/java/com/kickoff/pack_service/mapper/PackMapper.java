package com.kickoff.pack_service.mapper;

import com.kickoff.pack_service.dto.PackDefinitionResponse;
import com.kickoff.pack_service.model.PackDefinition;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PackMapper {
    PackDefinitionResponse toResponse(PackDefinition definition);

    List<PackDefinitionResponse> toResponse(List<PackDefinition> definitions);
}