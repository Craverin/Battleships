package com.example.demo.entity;

public enum HitDirection
{
    UP(new Coordinate(-1, 0)),
    DOWN(new Coordinate(1, 0)),
    LEFT(new Coordinate(0, -1)),
    RIGHT(new Coordinate(0, 1));

    public final Coordinate coordinate;
    HitDirection(Coordinate coordinate)
    {
        this.coordinate = coordinate;
    }

}
