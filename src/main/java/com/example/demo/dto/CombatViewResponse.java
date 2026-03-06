package com.example.demo.dto;

public record CombatViewResponse(ShotResult shotResult, CellStateView[][] hostBoard, CellStateView[][] opponentBoard) { }