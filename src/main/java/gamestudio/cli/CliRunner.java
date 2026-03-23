package gamestudio.cli;

import gamestudio.domain.*;
import gamestudio.dto.CellStateView;
import gamestudio.dto.CombatViewResponse;
import gamestudio.dto.ShipResponse;
import gamestudio.dto.ShotResult;
import gamestudio.entity.Comment;
import gamestudio.entity.Rating;
import gamestudio.entity.Score;
import gamestudio.repository.CommentRepository;
import gamestudio.repository.RatingRepository;
import gamestudio.repository.ScoreRepository;
import gamestudio.service.GameService;
import jakarta.annotation.Nullable;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gamestudio.cli.Colours.*;

@Component
public class CliRunner implements CommandLineRunner
{
    private final char[] columns = "ABCDEFGHIJ".toCharArray();
    private Game game;
    private UUID gameId, hostToken, opponentToken;
    private Board board;
    private final boolean[][] occupiedCells = new boolean[Board.SIZE][Board.SIZE];
    private final GameService gameService = new GameService();
    private final ScoreRepository scoreRep;
    private final RatingRepository ratingRep;
    private final CommentRepository commentRep;
    private CombatViewResponse hostCombatView;
    private final Map<CellStateView, String> opponentCellMarkers = new HashMap<>()
    {
        {
            put(CellStateView.UNKNOWN, " ");
            put(CellStateView.SHIP, ANSI_CYAN.unicode + "■" + ANSI_RESET.unicode);
            put(CellStateView.BLOCKED, ANSI_WHITE.unicode + "." + ANSI_RESET.unicode);
            put(CellStateView.HIT, ANSI_RED.unicode + "X" + ANSI_RESET.unicode);
            put(CellStateView.MISS, ANSI_YELLOW.unicode + "*" + ANSI_RESET.unicode);
            put(CellStateView.SUNK, ANSI_PURPLE.unicode + "#" + ANSI_RESET.unicode);
        }
    };

    private final Map<CellState, String> hostCellMarkers = new HashMap<>()
    {
        {
            put(CellState.EMPTY, " ");
            put(CellState.OCCUPIED, ANSI_CYAN.unicode + "■" + ANSI_RESET.unicode);
            put(CellState.INDIRECTLY_OCCUPIED, " ");
            put(CellState.BLOCKED, " ");
            put(CellState.HIT, ANSI_RED.unicode + "X" + ANSI_RESET.unicode);
            put(CellState.MISS, ANSI_YELLOW.unicode + "*" + ANSI_RESET.unicode);
            put(CellState.SUNK, ANSI_PURPLE.unicode + "#" + ANSI_RESET.unicode);
        }
    };

    public CliRunner(ScoreRepository scoreRep, RatingRepository ratingRep, CommentRepository commentRep)
    {
        this.scoreRep = scoreRep;
        this.ratingRep = ratingRep;
        this.commentRep = commentRep;
    }

    enum Action
    {
        RESTART,
        EXIT
    }

    public static void main(String[] args)
    {
        new SpringApplicationBuilder(CliRunner.class).web(WebApplicationType.NONE).run(args);
    }

    @Override
    public void run(@Nullable String... args) throws InterruptedException
    {
        boolean running = true;
        printRules();

        while (running)
        {
            playGame();
            Action action = parsePostGameCommands();
            if (action.equals(Action.EXIT)) running = false;
        }

        System.out.println("See you soon!");
        System.exit(0);
    }

    private Action parsePostGameCommands()
    {
        Scanner sc = new Scanner(System.in);

        while (true)
        {
            String input = sc.nextLine();

            if (input.trim().equalsIgnoreCase("exit")) return Action.EXIT;
            else if (input.trim().equalsIgnoreCase("restart")) return Action.RESTART;

            if (!input.contains(" "))
            {
                System.out.println("Invalid command");
                continue;
            }

            String command = input.substring(0, input.indexOf(" "));
            parseQuery(command, input.substring(command.length() + 1));
        }
    }

    private void playGame() throws InterruptedException
    {
        var resp = gameService.createGame();

        gameId = resp.gameId();
        game = gameService.getGame(gameId);
        hostToken = resp.hostToken();
        board = game.getBoard(hostToken);

        drawPlacementBoard();
        playPlacementPhase();

        System.out.println("\nGame started! Your turn!\n");
        System.out.println("\n\t\t\t\t\t\tOpponent's board");

        playCombatPhase();

        String winner = game.getWinner().equals(hostToken) ? "You" : "Opponent";
        System.out.println("GAME OVER! " + winner + " has won!");
        addNewScore(hostCombatView.score());
    }

    private void playPlacementPhase()
    {
        Scanner sc = new Scanner(System.in);
        String input;
        Matcher matcher;
        Pattern placementPattern = Pattern.compile("((?:10|[1-9])[a-j])\\s((?:10|[1-9])[a-j])\\s*([HhVv])?", Pattern.CASE_INSENSITIVE);

        while (game.getPhase().equals(GamePhase.PLACEMENT))
        {
            input = sc.nextLine().toLowerCase();
            if (input.equals("start"))
            {
                game.changeToCombatPhase();
                opponentToken = gameService.joinGame(gameId);
                break;
            }

            matcher = placementPattern.matcher(input);
            if (isMoveValid(matcher)) drawPlacementBoard();
        }
    }

    private void playCombatPhase() throws InterruptedException
    {
        Scanner sc = new Scanner(System.in);
        String input;
        Matcher matcher;
        Pattern combatPattern = Pattern.compile("((?:10|[1-9])[a-j])", Pattern.CASE_INSENSITIVE);

        Bot bot = new Bot();
        Coordinate firstHitCell = null;

        CombatViewResponse botCombatView = gameService.getCombatView(gameId, opponentToken);
        drawCombatBoard(opponentToken);

        while (game.getPhase().equals(GamePhase.COMBAT))
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
            drawCombatBoard(opponentToken);

            if (hostCombatView.phase().equals(GamePhase.FINISHED)) break;
            if (!hostCombatView.shotResult().equals(ShotResult.MISS)) continue;

            ShotResult shotResult;
            do
            {
                Coordinate targetCell;

                targetCell = bot.getTargetCell(botCombatView.opponentBoard(), firstHitCell);

                botCombatView = gameService.shoot(gameId, opponentToken, targetCell);
                shotResult = botCombatView.shotResult();

                if (firstHitCell == null && shotResult.equals(ShotResult.HIT)) firstHitCell = targetCell;
                else if (firstHitCell != null && shotResult.equals(ShotResult.SUNK)) firstHitCell = null;

                printBotMessages();
                drawCombatBoard(hostToken);

            } while (!shotResult.equals(ShotResult.MISS) && botCombatView.phase().equals(GamePhase.COMBAT));

            System.out.println("\n\t\t\t\t\t\tOpponent's board");
            TimeUnit.MILLISECONDS.sleep(1500);
            drawCombatBoard(opponentToken);
        }
    }

    private void parseQuery(String query, String arg)
    {
        if (query.equals("comment"))
        {
            if (arg.isBlank())
            {
                System.out.println("Invalid comment");
                return;
            }
            commentRep.addComment(new Comment("me", "Battleships", arg.trim(), new Date()));
        }

        else if (query.equals("rating"))
        {
            int rating = parseRating(arg);

            if (rating == -1)
            {
                System.out.println("Invalid rating!");
                return;
            }

            ratingRep.setRating(new Rating("me", "Battleships", rating, new Date()));
        }

        else
        {
            System.out.println("Invalid command!");
        }
    }

    private int parseRating(String str)
    {
        try
        {
            int rating = Integer.parseInt(str);
            if (rating < 0 || rating > 5) return -1;

            return rating;
        }
        catch (NumberFormatException e) { return -1; }
    }

    private void addNewScore(int score)
    {
        List<Score> scores = scoreRep.getTopScores("Battleships");
        int topScore = scoreRep.getTopScore("Battleships", "me");

        if (score > topScore)
        {
            System.out.println("Congratulations! New best score: " + score);
        }

        for (int i = 0; i < scores.size(); i++)
        {
            System.out.println(i + ": " + scores.get(i).getPoints());
            if (scores.get(i).getPoints() < score)
            {
                System.out.println("Unbelievable! You're on " + (i + 1) + ". place right now!");
                break;
            }
        }

        scoreRep.addScore(new Score("Battleships", "me", score, new Date()));
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

    private boolean isMoveValid(Matcher matcher)
    {
        if (!matcher.matches())
        {
            System.out.println("Invalid input!");
            return false;
        }

        Coordinate start, newStart;
        Orientation newOrientation;
        UUID shipId;

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
            return false;
        }

        if (!gameService.moveShip(gameId, shipId, hostToken, newStart, newOrientation))
        {
            System.out.println("Unable to move ship!");
            return false;
        }

        return true;
    }

    private static void printBotMessages() throws InterruptedException
    {
        System.out.println("\n\t\t\t\t\t\tOpponent's turn!");
        TimeUnit.SECONDS.sleep(3);
        System.out.println("\n\t\t\t\t\t\t\tYour board");
        TimeUnit.MILLISECONDS.sleep(500);
    }

    private static void printRules()
    {
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
    }

    private void drawPlacementBoard()
    {
        printColumns();
        List<ShipResponse> ships = gameService.getShips(gameId, hostToken);
        fillOccupiedCells(ships);

        for (int i = 0; i < Board.SIZE; i++)
        {
            System.out.println("  ");
            printBorderLine();

            if (i < Board.SIZE - 1) System.out.print(" ");
            System.out.print(i + 1 + " ");

            for (int k = 0; k < Board.SIZE; k++)
                System.out.print("|  " + (occupiedCells[i][k] ? ANSI_CYAN.unicode + "■" + ANSI_RESET.unicode : " ") + "  ");

            System.out.print("|");
        }

        System.out.println();
        printBorderLine();
    }

    private void drawCombatBoard(UUID playerToken)
    {
        hostCombatView = gameService.getCombatView(gameId, hostToken);
        boolean hostBoard = playerToken.equals(hostToken);
        if (!hostBoard)
            System.out.println(ANSI_BLUE.unicode + "Score: " + game.getScore(hostToken) + ANSI_RESET.unicode);

        printColumns();
        for (int i = 0; i < Board.SIZE; i++)
        {
            System.out.println("  ");
            printBorderLine();

            if (i < Board.SIZE - 1) System.out.print(" ");
            System.out.print(i + 1 + " ");

            for (int k = 0; k < Board.SIZE; k++)
            {
                System.out.print("|  ");

                if (hostBoard)
                    System.out.print(hostCellMarkers.get(hostCombatView.hostBoard()[i][k]));
                else
                    System.out.print(opponentCellMarkers.get(hostCombatView.opponentBoard()[i][k]));

                System.out.print("  ");
            }
            System.out.print("|");

        }

        System.out.println();
        printBorderLine();
    }

    private void fillOccupiedCells(List<ShipResponse> ships)
    {
        resetOccupiedCells();
        for (ShipResponse ship : ships)
        {
            for (Coordinate cell : getShipCoordinates(ship))
                occupiedCells[cell.row()][cell.col()] = true;
        }
    }

    private void resetOccupiedCells()
    {
        for (int i = 0; i < Board.SIZE; i++)
        {
            for (int j = 0; j < Board.SIZE; j++)
                occupiedCells[i][j] = false;
        }
    }

    private List<Coordinate> getShipCoordinates(ShipResponse ship)
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

    private void printBorderLine()
    {
        System.out.print("   ");
        for (int i = 0; i < Board.SIZE; i++)
        {
            System.out.print("+-----");
        }
        System.out.println("+");
    }

    private void printColumns()
    {
        System.out.print("      " + columns[0]);
        for (int i = 1; i < columns.length; i++) System.out.print("     " + columns[i]);
    }

    private UUID getShipId(Coordinate cell)
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
