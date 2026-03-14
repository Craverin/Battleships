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
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CliRunner implements CommandLineRunner
{
    private static final int BOARD_SIZE = 10;
    private static final char[] columns = "ABCDEFGHIJ".toCharArray();
    private static GamePhase gamePhase = GamePhase.PLACEMENT;
    private static UUID gameId, hostToken, opponentToken;
    private static Board board;
    private static final boolean[][] occupiedCells = new boolean[BOARD_SIZE][BOARD_SIZE];
    private static final GameService gameService = new GameService();
    private final ScoreRepository scoreRep;
    private final RatingRepository ratingRep;
    private final CommentRepository commentRep;
    private CombatViewResponse hostCombatView = null, botCombatView = null;
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

    public void run(String... args) throws InterruptedException
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
            String inp = sc.nextLine();

            if (inp.trim().equalsIgnoreCase("exit")) return Action.EXIT;
            else if (inp.trim().equalsIgnoreCase("restart")) return Action.RESTART;

            if (!inp.contains(" "))
            {
                System.out.println("Invalid command");
                continue;
            }

            String command = inp.substring(0, inp.indexOf(" "));
            parseQuery(command, inp.substring(command.length() + 1));
        }
    }

    private void playGame() throws InterruptedException
    {
        var resp = gameService.createGame();

        gameId = resp.gameId();
        Game game = gameService.getGame(gameId);
        hostToken = resp.hostToken();
        board = game.getBoard(hostToken);

        drawPlacementBoard();
        playPlacementPhase();

        System.out.println("\nGame started! Your turn!\n");
        System.out.println("\n\t\t\t\t\t\tOpponent's board");

        drawCombatBoard(null, 0);
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
            drawCombatBoard(hostCombatView.opponentBoard(), hostCombatView.score());

            if (hostCombatView.phase().equals(GamePhase.FINISHED)) break;
            if (hostCombatView.shotResult().equals(ShotResult.HIT)) continue;

            do
            {
                Coordinate attackedCell;
                if (botCombatView == null)
                {
                    Random rnd = new Random();
                    attackedCell = new Coordinate(rnd.nextInt(BOARD_SIZE), rnd.nextInt(BOARD_SIZE));
                }
                else attackedCell = bot.getTargetCell(botCombatView.opponentBoard());

                System.out.println("ATTACKING (" + attackedCell.row() + ", " + attackedCell.col() + ")");

                botCombatView = gameService.shoot(gameId, opponentToken, attackedCell);
                gamePhase = botCombatView.phase();

                printBotMessages();
                drawCombatBoard(botCombatView.opponentBoard(), -1);


            } while (botCombatView.shotResult().equals(ShotResult.HIT) && gamePhase.equals(GamePhase.COMBAT));

            System.out.println("\n\t\t\t\t\t\tOpponent's board");
            TimeUnit.MILLISECONDS.sleep(1500);
            drawCombatBoard(hostCombatView.opponentBoard(), hostCombatView.score());
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

    private static boolean isMoveValid(Matcher matcher)
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

    private static void drawCombatBoard(CellStateView[][] board, int score)
    {
        // Score -1 is designed exclusively for the bot, there's no sense in printing it.
        if (score != -1) System.out.println("Score: " + score + "\n");
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
