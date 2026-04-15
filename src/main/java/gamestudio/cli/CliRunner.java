package gamestudio.cli;

import gamestudio.service.CommentService;
import gamestudio.service.RatingService;
import gamestudio.service.ScoreService;
import jakarta.annotation.Nullable;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

import static gamestudio.cli.Color.*;

@Component
public class CliRunner implements CommandLineRunner
{
    private final String playerName = "player";
    private final Scanner sc = new Scanner(System.in);
    private final Menu menu;
    private final CliController gameController;

    public CliRunner(Menu menu, ScoreService scoreRep, RatingService ratingRep, CommentService commentRep)
    {
        this.menu = menu;
        this.gameController = new CliController(scoreRep, ratingRep, commentRep, playerName);
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
                    BoardMessages.printRules();
                    gameController.playGame();

                    Action action = gameController.parsePostGameCommands();

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

    private void openCommentsMenu()
    {
        int pageNum = 1;
        boolean inComments = true;

        while (inComments)
        {
            System.out.println();
            menu.showUserComments(playerName);
            menu.showCommentsPage(pageNum);

            String input = sc.nextLine().trim().toLowerCase();

            if (input.equals("n") || input.equals("next"))
            {
                if (pageNum < menu.getCommentsPageCount())
                    pageNum++;
                else
                    System.out.println(ANSI_YELLOW.unicode + "You are already on the last page." + ANSI_RESET.unicode);
            }

            else if (input.equals("p") || input.equals("prev"))
            {
                if (pageNum > 1)
                    pageNum--;
                else
                    System.out.println(ANSI_YELLOW.unicode + "You are already on the first page." + ANSI_RESET.unicode);
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
}