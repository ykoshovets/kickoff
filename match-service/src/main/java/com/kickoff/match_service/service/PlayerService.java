package com.kickoff.match_service.service;

import com.kickoff.match_service.dto.PlayerResponse;
import com.kickoff.match_service.mapper.PlayerMapper;
import com.kickoff.match_service.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PlayerService {

    private final PlayerMapper playerMapper;
    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository, PlayerMapper playerMapper) {
        this.playerRepository = playerRepository;
        this.playerMapper = playerMapper;
    }

    @Transactional(readOnly = true)
    public List<PlayerResponse> getAll() {
        return playerMapper.toResponse(playerRepository.findAllWithTeam());
    }


    public Optional<PlayerResponse> getByExternalId(Integer externalId) {
        return playerRepository.findByExternalIdWithTeam(externalId)
                .map(playerMapper::toResponse);
    }
}