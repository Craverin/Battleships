package com.example.demo.cli;

import com.example.demo.dto.CellStateView;
import com.example.demo.dto.CombatViewResponse;
import com.example.demo.dto.ShipResponse;
import com.example.demo.dto.ShotResult;
import com.example.demo.entity.*;
import com.example.demo.service.GameService;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CliRunner
{
    private static final int BOARD_SIZE = 10;
    private static final char[] columns = "ABCDEFGHIJ".toCharArray();
    private static GamePhase gamePhase = GamePhase.PLACEMENT;
    private static UUID gameId, hostToken, opponentToken;
    private static Game game;
    private static Board board;
    private static boolean[][] occupiedCells = new boolean[BOARD_SIZE][BOARD_SIZE];
    private static GameService gameService = new GameService();
    private static final Map<CellStateView, String> cellMarkers = new HashMap<>(){
        {
            put(CellStateView.EMPTY, " ");
            put(CellStateView.UNKNOWN, " ");
            put(CellStateView.SHIP, "O");
            put(CellStateView.BLOCKED, " ");
            put(CellStateView.HIT, "X");
            put(CellStateView.MISS, "*");
            put(CellStateView.SUNK, "#");
        }
    };

    public static void main(String[] args) throws InterruptedException
    {
        var resp = gameService.createGame();

        gameId = resp.gameId();
        game = gameService.getGame(gameId);
        hostToken = resp.hostToken();
        board = game.getBoard(hostToken);

        System.out.println("\nWelcome to Battleships!\n");
        System.out.println("To move a ship, enter two coordinates separated by a space:");
        System.out.println("1) the ship's current start cell, 2) the new start cell.");
        System.out.println("Optionally add the new orientation at the end (H or V).");
        System.out.println();
        System.out.println("Format: <oldStart> <newStart> [H|V]");
        System.out.println("Examples:");
        System.out.println("  5A 3C");
        System.out.println("  8B 3D V");
        System.out.println("\nWhen ready, just enter \"start\". Good luck!\n");

        drawPlacementBoard();

        Pattern placementPattern = Pattern.compile("((?:10|[1-9])[a-j])\\s((?:10|[1-9])[a-j])\\s*([HhVv])?", Pattern.CASE_INSENSITIVE);
        Pattern combatPattern = Pattern.compile("((?:10|[1-9])[a-j])");
        Matcher matcher;

        Scanner sc = new Scanner(System.in);
        String input;
        Coordinate start, newStart;
        Orientation newOrientation;
        UUID shipId;

        // TODO Refactor the code below, probably move some of the logic to the separate methods

        while (gamePhase.equals(GamePhase.PLACEMENT))
        {
            input = sc.nextLine().toLowerCase();
            if (input.equals("start"))
            {
                gamePhase = GamePhase.COMBAT;
                opponentToken = gameService.joinGame(gameId);
                break;
            }


            matcher = placementPattern.matcher(input);
            if (!matcher.matches())
            {
                System.out.println("Invalid input!");
                continue;
            }

            start = convertCellCoordinate(matcher.group(1));
            newStart = convertCellCoordinate(matcher.group(2));
            newOrientation = null;
            if (matcher.group(3) != null) newOrientation = matcher.group(3).equals("v")
                                                           ? Orientation.VERTICAL
                                                           : Orientation.HORIZONTAL;
            shipId = getShipId(start);
            if (shipId == null)
            {
                System.out.println("Invalid cell!");
                continue;
            }

            if (!gameService.moveShip(gameId, shipId, hostToken, newStart, newOrientation))
            {
                System.out.println("Unable to move ship!");
                continue;
            }
            drawPlacementBoard();

        }

        System.out.println("\nGame started! Your turn!\n");
        Bot opponent = new Bot();
        CombatViewResponse hostCombatView, opponentCombatView = null;

        System.out.println("\n\t\t\t\t\t\tOpponent's board");
        drawCombatBoard(null);

        while (gamePhase.equals(GamePhase.COMBAT))
        {
            System.out.print("Enter coordinates: ");
            input = sc.nextLine().toLowerCase();
            matcher = combatPattern.matcher(input);

            if (!matcher.matches())
            {
                System.out.println("Invalid input!");
                continue;
            }

            Coordinate cell = convertCellCoordinate(matcher.group(1));
            hostCombatView = gameService.shoot(gameId, hostToken, cell);

            System.out.println("\n\t\t\t\t\t\tOpponent's board");
            drawCombatBoard(hostCombatView.opponentBoard());

            if (hostCombatView.shotResult().equals(ShotResult.HIT)) continue;

            do
            {
                Coordinate attackedCell;
                if (opponentCombatView == null)
                {
                    Random rnd = new Random();
                    attackedCell = new Coordinate(rnd.nextInt(BOARD_SIZE), rnd.nextInt(BOARD_SIZE));
                }
                else attackedCell = opponent.attack(opponentCombatView.opponentBoard());

              //  System.out.println("Bot attacked (" + attackedCell.row() + ", " + attackedCell.col() + ")");
                TimeUnit.MILLISECONDS.sleep(500);
                opponentCombatView = gameService.shoot(gameId, opponentToken, attackedCell);

                System.out.println("\n\t\t\t\t\t\tOpponent's turn!");
                TimeUnit.SECONDS.sleep(3);
                System.out.println("\n\t\t\t\t\t\t\tYour board");
                TimeUnit.MILLISECONDS.sleep(500);

                drawCombatBoard(opponentCombatView.opponentBoard());
            } while (opponentCombatView.shotResult().equals(ShotResult.HIT));

            System.out.println("\n\t\t\t\t\t\tOpponent's board");
            TimeUnit.MILLISECONDS.sleep(1500);
            drawCombatBoard(hostCombatView.opponentBoard());
        }
    }

    private static Coordinate convertCellCoordinate(String coordinate)
    {
        int row, col;
        char[] coords = coordinate.toCharArray();

        if (coords.length == 3)
        {
            row = 9;
            col = coords[2] - 'a';
        }
        else
        {
            row = coords[0] - '0' - 1;
            col = coords[1] - 'a';
        }

        System.out.println("Coordinates: " + row + ", " + col);

        return new Coordinate(row, col);
    }

    private static void drawPlacementBoard()
    {
        printColumns();
        List<ShipResponse> ships = gameService.getShips(gameId, hostToken);
        fillOccupiedCells(ships);

        for (int i = 0; i < BOARD_SIZE; i++)
        {
            System.out.println("  ");
            printBorderLine();

            if (i < BOARD_SIZE - 1) System.out.print(" ");
            System.out.print(i + 1 + " ");

            for (int k = 0; k < BOARD_SIZE; k++) System.out.print("|  " + (occupiedCells[i][k] ? "O" : " ") + "  ");
            System.out.print("|");
        }

        System.out.println();
        printBorderLine();
    }

    private static void drawCombatBoard(CellStateView[][] board)
    {
        printColumns();
        for (int i = 0; i < BOARD_SIZE; i++)
        {
            System.out.println("  ");
            printBorderLine();

            if (i < BOARD_SIZE - 1) System.out.print(" ");
            System.out.print(i + 1 + " ");

            for (int k = 0; k < BOARD_SIZE; k++)
            {
                System.out.print("|  ");

                if (board != null) System.out.print(cellMarkers.get(board[i][k]));
                else System.out.print(" ");

                System.out.print("  ");
            }
            System.out.print("|");

        }

        System.out.println();
        printBorderLine();
    }

    private static void fillOccupiedCells(List<ShipResponse> ships)
    {
        resetOccupiedCells();
        for (ShipResponse ship : ships)
        {
            for (Coordinate cell : getShipCoordinates(ship))
                occupiedCells[cell.row()][cell.col()] = true;
        }
    }

    private static void resetOccupiedCells()
    {
        for (int i = 0; i < BOARD_SIZE; i++)
        {
            for (int j = 0; j < BOARD_SIZE; j++)
                occupiedCells[i][j] = false;
        }
    }

    private static List<Coordinate> getShipCoordinates(ShipResponse ship)
    {
        List<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(new Coordinate(ship.row(), ship.col()));

        for (int i = 1; i < ship.length(); i++)
        {
            if (ship.orientation().equals(Orientation.VERTICAL))
                coordinates.add(new Coordinate(ship.row() + i, ship.col()));
            else
                coordinates.add(new Coordinate(ship.row(), ship.col() + i));
        }

        return coordinates;
    }

    private static void printBorderLine()
    {
        System.out.print("   ");
        for (int i = 0; i < BOARD_SIZE; i++)
        {
            System.out.print("+-----");
        }
        System.out.println("+");
    }

    private static void printColumns()
    {
        System.out.print("      " + columns[0]);
        for (int i = 1; i < columns.length; i++) System.out.print("     " + columns[i]);
    }

    private static UUID getShipId(Coordinate cell)
    {
        List<Ship> ships = board.getShips();
        for (Ship ship : ships)
        {
            Coordinate start = ship.getStart();
            if (start.row() == cell.row() && start.col() == cell.col()) return ship.getId();
        }

        return null;
    }
}
