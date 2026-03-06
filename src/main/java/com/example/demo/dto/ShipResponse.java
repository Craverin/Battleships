package com.example.demo.dto;

import com.example.demo.entity.Orientation;

import java.util.UUID;

public record ShipResponse(UUID shipId, int row, int col, Orientation orientation, int length) { }
