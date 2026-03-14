package gamestudio.domain;

public record Coordinate(int row, int col)
{
    public static Coordinate addCoordinates(Coordinate first, Coordinate second)
    {
        return new Coordinate(first.row() + second.row(), first.col() + second.col());
    }

    public static boolean isValidCoordinate(Coordinate coordinate)
    {
        return coordinate.row() >= 0 && coordinate.row <= 9 && coordinate.col() >= 0 && coordinate.col <= 9;
    }
}
