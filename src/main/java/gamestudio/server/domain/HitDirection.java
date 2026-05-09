package gamestudio.server.domain;

public enum HitDirection
{
    UP(new Coordinate(-1, 0)),
    LEFT(new Coordinate(0, -1)),
    RIGHT(new Coordinate(0, 1)),
    DOWN(new Coordinate(1, 0));

    public final Coordinate coordinate;
    HitDirection(Coordinate coordinate)
    {
        this.coordinate = coordinate;
    }

    HitDirection getOpposite()
    {
        var values = HitDirection.values();
        return values[values.length - this.ordinal() - 1];
    }
}
