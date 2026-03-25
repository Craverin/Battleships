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

import static gamestudio.cli.Color.*;

@Component
public class CliRunner implements CommandLineRunner
{
    private final char[] columns = "ABCDEFGHIJ".toCharArray();
    private final String playerName = "player";
    private Game game;
    private UUID gameId, hostToken, opponentToken;
    private Board board;
    private final boolean[][] occupiedCells = new boolean[Board.SIZE][Board.SIZE];
    private final Menu menu;
    private final Scanner sc = new Scanner(System.in);
    private final GameService gameService = new GameService();
    private final ScoreRepository scoreRep;
    private final RatingRepository ratingRep;
    private final CommentRepository commentRep;
    private CombatViewResponse hostCombatView;

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

    enum Action
    {
        SHOW_MENU,
        RESTART,
        EXIT
    }

    public CliRunner(Menu menu, ScoreRepository scoreRep, RatingRepository ratingRep, CommentRepository commentRep)
    {
        this.scoreRep = scoreRep;
        this.ratingRep = ratingRep;
        this.commentRep = commentRep;
        this.menu = menu;
    }

    public static void main(String[] args)
    {
        new SpringApplicationBuilder(CliRunner.class).web(WebApplicationType.NONE).run(args);
    }

    @Override
    public void run(@Nullable String... args) throws InterruptedException
    {
        boolean running = true, showingMenu = true;

        while (running)
        {
            String input = "1";
            if (showingMenu)
            {
                menu.showMenu();
                input = sc.nextLine().trim().toLowerCase();
            }

            switch (input)
            {
                case "1":
                    System.out.println();
                    printRules();
                    playGame();

                    Action action = parsePostGameCommands();

                    if (action.equals(Action.EXIT)) running = false;
                    else if (action.equals(Action.SHOW_MENU)) showingMenu = true;
                    else if (action.equals(Action.RESTART)) showingMenu = false;

                    break;

                case "2":
                    openCommentsMenu();
                    break;

                case "3":
                    System.out.println();
                    menu.showRatingPage(playerName);
                    waitForEnter();
                    break;

                case "4":
                    System.out.println();
                    menu.showScoresPage(playerName);
                    waitForEnter();
                    break;

                case "5":
                case "exit":
                    running = false;
                    break;

                default:
                    System.out.println(ANSI_RED.unicode + "Unknown option. Please enter a number from 1 to 5." + ANSI_RESET.unicode);
                    break;
            }
        }

        System.out.println();
        System.out.println(ANSI_CYAN.unicode + "See you soon!" + ANSI_RESET.unicode);
        System.exit(0);
    }

    private Action parsePostGameCommands()
    {
        Scanner sc = new Scanner(System.in);

        while (true)
        {
            String input = sc.nextLine();
            String trimmedInput = input.trim().toLowerCase();

            switch (trimmedInput)
            {
                case "exit" -> { return Action.EXIT; }
                case "menu" -> { return Action.SHOW_MENU; }
                case "restart" -> { return Action.RESTART; }
            }

            if (!input.contains(" "))
            {
                System.out.println(ANSI_RED.unicode + "Invalid command" + ANSI_RESET.unicode);
                continue;
            }

            String command = input.substring(0, input.indexOf(" "));
            parseQuery(command, input.substring(command.length() + 1));
        }
    }

    private void openCommentsMenu()
    {
        int pageNum = 1;
        boolean inComments = true;

        while (inComments)
        {
            System.out.println();
            menu.showUserComment(playerName);
            menu.showCommentsPage(pageNum);

            String input = sc.nextLine().trim().toLowerCase();

            if (input.equals("n") || input.equals("next"))
            {
                if (pageNum < menu.getCommentsPageCount()) pageNum++;
                else System.out.println(ANSI_YELLOW.unicode + "You are already on the last page." + ANSI_RESET.unicode);
            }

            else if (input.equals("p") || input.equals("prev"))
            {
                if (pageNum > 1) pageNum--;
                else System.out.println(ANSI_YELLOW.unicode + "You are already on the first page." + ANSI_RESET.unicode);
            }

            else if (input.startsWith("j ") || input.startsWith("jump "))
            {
                Integer targetPage = parseJumpPage(input);

                if (targetPage == null)
                    System.out.println(ANSI_RED.unicode + "Invalid command. Use: j <pageNumber>" + ANSI_RESET.unicode);

                else if (targetPage <= 0 || targetPage > menu.getCommentsPageCount())
                    System.out.println(ANSI_RED.unicode + "Page does not exist." + ANSI_RESET.unicode);

                else
                    pageNum = targetPage;
            }

            else if (input.equals("b") || input.equals("back"))
                inComments = false;

            else
                System.out.println(ANSI_RED.unicode + "Unknown command. Use n, p, j <pageNumber>, or b." + ANSI_RESET.unicode);
        }
    }

    private Integer parseJumpPage(String input)
    {
        String[] page = input.split("\\s+");

        if (page.length != 2) return null;

        try { return Integer.parseInt(page[1]); }
        catch (NumberFormatException e) { return null; }
    }

    private void waitForEnter()
    {
        System.out.println();
        System.out.println(ANSI_BRIGHT_BLACK.unicode + "Press Enter to return to the menu..." + ANSI_RESET.unicode);
        sc.nextLine();
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
        String color = game.getWinner().equals(hostToken) ? ANSI_GREEN.unicode : ANSI_YELLOW.unicode;

        System.out.println(ANSI_RED.unicode + "GAME OVER!\n" + color + winner + ANSI_RESET.unicode
                           + " has won with a score " + ANSI_BLUE.unicode + hostCombatView.score()
                           + ANSI_RESET.unicode + ".");
        addNewScore(hostCombatView.score());

        printPostGameCommands();
    }

    private void printPostGameCommands()
    {
        System.out.println("Please, consider adding a " + ANSI_YELLOW.unicode + "comment" + ANSI_RESET.unicode + ": " + ANSI_GREEN.unicode + "comment <your comment>" + ANSI_RESET.unicode);
        System.out.println("To add a " + ANSI_YELLOW.unicode + "rating" + ANSI_RESET.unicode + ": " + ANSI_GREEN.unicode + "rating <1-5>" + ANSI_RESET.unicode);

        System.out.println("\nRestart the game: " + ANSI_YELLOW.unicode + "restart" + ANSI_RESET.unicode);
        System.out.println("Return to the menu: " + ANSI_GREEN.unicode + "menu" + ANSI_RESET.unicode);
        System.out.println("Leave the game: " + ANSI_RED.unicode + "exit" + ANSI_RESET.unicode);
    }

    private void playPlacementPhase()
    {
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
       // drawCombatBoard(opponentToken);
        DEBUG_drawBotBoard(botCombatView);

        while (game.getPhase().equals(GamePhase.COMBAT))
        {
            System.out.print("Enter coordinates: ");
            input = sc.nextLine().toLowerCase();
            matcher = combatPattern.matcher(input);

            if (!matcher.matches())
            {
                System.out.println(ANSI_RED.unicode + "Invalid input!" + ANSI_RESET.unicode);
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
                System.out.println(ANSI_RED.unicode + "Invalid comment" + ANSI_RESET.unicode);
                return;
            }
            commentRep.addComment(new Comment(playerName, "battleships", arg.trim(), new Date()));
        }

        else if (query.equals("rating"))
        {
            int rating = parseRating(arg);

            if (rating == -1)
            {
                System.out.println(ANSI_RED.unicode + "Invalid rating!" + ANSI_RESET.unicode);
                return;
            }

            ratingRep.setRating(new Rating(playerName, "battleships", rating, new Date()));
        }

        else System.out.println(ANSI_RED.unicode + "Invalid command!" + ANSI_RESET.unicode);
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
        List<Score> scores = scoreRep.getTopScores("battleships");
        int topScore = scoreRep.getTopScore("battleships", playerName);

        System.out.println();

        if (score > topScore)
            System.out.println(ANSI_GREEN.unicode + "Congratulations! New best score: "
                               + ANSI_YELLOW.unicode + score + ANSI_RESET.unicode);

        for (int i = 0; i < scores.size(); i++)
        {
            if (scores.get(i).getPoints() < score)
            {
                System.out.println(ANSI_CYAN.unicode + "Unbelievable! You're on " + ANSI_YELLOW.unicode
                                   + (i + 1) + ANSI_CYAN.unicode + ". place right now!" + ANSI_RESET.unicode);
                break;
            }
        }

        scoreRep.addScore(new Score(playerName, "battleships", score, new Date()));
        scores = scoreRep.getTopScores("battleships");

        System.out.println();
        System.out.println(ANSI_CYAN.unicode + "All-time Best:" + ANSI_RESET.unicode);

        for (int i = 0; i < scores.size(); i++)
            System.out.println(ANSI_YELLOW.unicode + (i + 1) + "." + ANSI_RESET.unicode + " "
                               + scores.get(i).getPoints() + " " + ANSI_BRIGHT_BLACK.unicode
                               + "(" + scores.get(i).getPlayer() + ")" + ANSI_RESET.unicode);

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

        return new Coordinate(row, col);
    }

    private boolean isMoveValid(Matcher matcher)
    {
        if (!matcher.matches())
        {
            System.out.println(ANSI_RED.unicode + "Invalid input!" + ANSI_RESET.unicode);
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

    private void DEBUG_drawBotBoard(CombatViewResponse botCombatView)
    {
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
                System.out.print(hostCellMarkers.get(botCombatView.hostBoard()[i][k]));
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
