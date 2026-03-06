package com.example.demo.dto;

import com.example.demo.entity.Orientation;

public record MoveShipRequest(int row, int col, Orientation orientation) { }