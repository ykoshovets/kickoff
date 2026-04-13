package com.kickoff.prediction_service.mapper;

import com.kickoff.prediction_service.dto.PredictionRequest;
import com.kickoff.prediction_service.dto.PredictionResponse;
import com.kickoff.prediction_service.model.Prediction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PredictionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "result", ignore = true)
    @Mapping(target = "coinsAwarded", ignore = true)
    @Mapping(target = "evaluatedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void map(PredictionRequest request, @MappingTarget Prediction prediction);

    PredictionResponse toResponse(Prediction prediction);

    List<PredictionResponse> toResponse(List<Prediction> predictions);
}