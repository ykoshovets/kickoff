package com.kickoff.trade_service.mapper;

import com.kickoff.trade_service.dto.TradeResponse;
import com.kickoff.trade_service.model.Trade;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TradeMapper {
    TradeResponse toResponse(Trade trade);

    List<TradeResponse> toResponse(List<Trade> trades);
}