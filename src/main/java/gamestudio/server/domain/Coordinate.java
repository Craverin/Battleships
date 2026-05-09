package gamestudio.server.domain;

public record Coordinate(int row, int col)
{
    public static Coordinate addCoordinates(Coordinate first, Coordinate second)
    {
        return new Coordinate(first.row() + second.row(), first.col() + second.col());
    }

    public static boolean isValid(Coordinate coordinate)
    {
        return coordinate.row() >= 0 && coordinate.row < Board.SIZE &&
               coordinate.col() >= 0 && coordinate.col < Board.SIZE;
    }

    public static boolean isValid(int row, int col)
    {
        return row >= 0 && row < Board.SIZE && col >= 0 && col < Board.SIZE;
    }
}
