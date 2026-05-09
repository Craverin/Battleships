package gamestudio.cli;

import gamestudio.server.domain.*;
import gamestudio.server.dto.CombatViewResponse;
import gamestudio.server.dto.ShotResult;
import gamestudio.server.entity.Comment;
import gamestudio.server.entity.Rating;
import gamestudio.server.entity.Score;
import gamestudio.server.service.*;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gamestudio.cli.Color.*;
import static gamestudio.cli.Color.ANSI_BLUE;
import static gamestudio.cli.Color.ANSI_RESET;

public class CliController
{
    private final String playerName;
    private final Scanner sc;
    private final GameService gameService;
    private final ScoreService scoreRep;
    private final RatingService ratingRep;
    private final CommentService commentRep;
    private final PlayerStatsService pl;
    private final BoardRenderer boardRenderer;

    private Game game;
    private UUID gameId;
    private UUID hostToken;
    private UUID opponentToken;
    private Board board;
    private CombatViewResponse hostCombatView;

    public CliController(ScoreService scoreRep, RatingService ratingRep, CommentService commentRep, PlayerStatsService pl, String playerName)
    {
        this.scoreRep = scoreRep;
        this.ratingRep = ratingRep;
        this.commentRep = commentRep;
        this.pl = pl;
        this.gameService = new GameService();
        this.playerName = playerName;

        this.sc = new Scanner(System.in);
        this.boardRenderer = new BoardRenderer();
    }

    public void playGame() throws InterruptedException
    {
        pl.addPlayerStats("battleships", "me", 2250, true);
        pl.addPlayerStats("battleships", "me", 250, false);
        pl.addPlayerStats("battleships", "me", 1550, true);

        var resp = gameService.createGame();

        gameId = resp.gameId();
        game = gameService.getGame(gameId);
        hostToken = resp.hostToken();
        board = game.getBoard(hostToken);

        boardRenderer.drawPlacementBoard(gameService.getShips(gameId, hostToken));
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
        BoardMessages.printPostGameCommands();
    }

    private void playPlacementPhase()
    {
        String input;
        Matcher matcher;

        // Placement command format: <oldStart> <newStart> [H|V]
        // The start cell is the topmost or leftmost cell of the ship
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
            if (isMoveValid(matcher)) boardRenderer.drawPlacementBoard(gameService.getShips(gameId, hostToken));
        }
    }

    private void playCombatPhase() throws InterruptedException
    {
        String input;
        Matcher matcher;

        // Combat command format: <cell>. For example 3b or 10j
        Pattern combatPattern = Pattern.compile("((?:10|[1-9])[a-j])", Pattern.CASE_INSENSITIVE);

        Bot bot = new Bot();
        Coordinate firstHitCell = null;


        CombatViewResponse botCombatView = gameService.getCombatView(gameId, opponentToken);
        hostCombatView = gameService.getCombatView(gameId, hostToken);

        boardRenderer.drawCombatBoard(hostCombatView, opponentToken, hostToken, game.getScore(hostToken));

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

            if (hostCombatView.shotResult().equals(ShotResult.NONE))
            {
                System.out.println(ANSI_RED.unicode + "Invalid cell!" + ANSI_RESET.unicode);
                continue;
            }

            System.out.println("\n\t\t\t\t\t\tOpponent's board");
            boardRenderer.drawCombatBoard(hostCombatView, opponentToken, hostToken, game.getScore(hostToken));

            if (hostCombatView.phase().equals(GamePhase.FINISHED)) break;
            if (!hostCombatView.shotResult().equals(ShotResult.MISS)) continue;

            ShotResult botShotResult;
            do
            {
                Coordinate targetCell;

                targetCell = bot.getTargetCell(botCombatView.opponentBoard(), firstHitCell);

                botCombatView = gameService.shoot(gameId, opponentToken, targetCell);
                botShotResult = botCombatView.shotResult();

                // Remember the first hit in the current hit series. Reset it when ship is sunk
                if (firstHitCell == null && botShotResult.equals(ShotResult.HIT)) firstHitCell = targetCell;
                else if (firstHitCell != null && botShotResult.equals(ShotResult.SUNK)) firstHitCell = null;

                BoardMessages.printBotMessages();
                boardRenderer.drawCombatBoard(hostCombatView, hostToken, hostToken, game.getScore(hostToken));

            } while (!botShotResult.equals(ShotResult.MISS) && botCombatView.phase().equals(GamePhase.COMBAT));

            System.out.println("\n\t\t\t\t\t\tOpponent's board");
            TimeUnit.MILLISECONDS.sleep(1500);
            boardRenderer.drawCombatBoard(hostCombatView, opponentToken, hostToken, game.getScore(hostToken));
        }
    }

    public Action parsePostGameCommands()
    {
        Action action = null;

        do
        {
            String input = sc.nextLine();
            String trimmedInput = input.trim().toLowerCase();

            switch (trimmedInput)
            {
                case "exit" -> action = Action.EXIT;
                case "menu" -> action = Action.SHOW_MENU;
                case "restart" -> action = Action.RESTART;
            }

            if (action == null)
            {
                if (!input.contains(" "))
                {
                    System.out.println(ANSI_RED.unicode + "Invalid command" + ANSI_RESET.unicode);
                    continue;
                }

                String command = input.substring(0, input.indexOf(" "));
                parseQuery(command, input.substring(command.length() + 1));
            }
        } while (action == null);

        return action;
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

    private boolean isMoveValid(Matcher matcher)
    {
        if (!matcher.matches())
        {
            System.out.println(ANSI_RED.unicode + "Invalid input!" + ANSI_RESET.unicode);
            return false;
        }

        Coordinate start, newStart;
        Orientation newOrientation = null;
        UUID shipId;

        start = convertCellCoordinate(matcher.group(1));
        newStart = convertCellCoordinate(matcher.group(2));

        if (matcher.group(3) != null)
        {
            newOrientation = matcher.group(3).equals("v")
                             ? Orientation.VERTICAL
                             : Orientation.HORIZONTAL;
        }

        shipId = getShipId(start);

        if (shipId == null)
        {
            System.out.println(ANSI_RED.unicode + "Invalid cell!" + ANSI_RESET.unicode);
            return false;
        }

        if (!gameService.moveShip(gameId, shipId, hostToken, newStart, newOrientation))
        {
            System.out.println(ANSI_RED.unicode + "Unable to move ship!" + ANSI_RESET.unicode);
            return false;
        }

        return true;
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

    private Coordinate convertCellCoordinate(String coordinate)
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
}
