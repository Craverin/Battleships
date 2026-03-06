package com.example.demo.dto;

import java.util.UUID;

public record CreateGameResponse(UUID gameId, UUID hostToken) { }