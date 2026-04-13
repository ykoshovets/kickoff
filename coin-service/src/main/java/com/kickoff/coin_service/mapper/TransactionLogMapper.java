package com.kickoff.coin_service.mapper;

import com.kickoff.coin_service.dto.TransactionLogDto;
import com.kickoff.coin_service.model.TransactionLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionLogMapper {

    @Mapping(target = "transactionType", source = "type")
    TransactionLogDto map(TransactionLog transactionLog);

    List<TransactionLogDto> map(List<TransactionLog> transactionLogs);
}
