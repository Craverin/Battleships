package gamestudio.cli;

import gamestudio.domain.Board;

import java.util.concurrent.TimeUnit;

import static gamestudio.cli.Color.*;
import static gamestudio.cli.Color.ANSI_CYAN;
import static gamestudio.cli.Color.ANSI_GREEN;
import static gamestudio.cli.Color.ANSI_RESET;
import static gamestudio.cli.Color.ANSI_YELLOW;

public class BoardMessages
{
    private static final char[] columns = "ABCDEFGHIJ".toCharArray();

    public static void printBorderLine()
    {
        System.out.print("   ");
        for (int i = 0; i < Board.SIZE; i++)
        {
            System.out.print("+-----");
        }
        System.out.println("+");
    }

    public static void printColumns()
    {
        System.out.print("      " + columns[0]);
        for (int i = 1; i < columns.length; i++) System.out.print("     " + columns[i]);
    }

    public static void printBotMessages() throws InterruptedException
    {
        System.out.println("\n\t\t\t\t\t\tOpponent's turn!");
        TimeUnit.SECONDS.sleep(3);
        System.out.println("\n\t\t\t\t\t\t\tYour board");
        TimeUnit.MILLISECONDS.sleep(500);
    }

    public static void printRules()
    {
        System.out.println(ANSI_CYAN.unicode + "\nWelcome to Battleships!" + ANSI_RESET.unicode);
        System.out.println("\nTo move a ship, enter two coordinates separated by a space:");
        System.out.println("1) the ship's current start cell, 2) the new start cell.");
        System.out.println("Optionally add the new orientation at the end (" + ANSI_YELLOW.unicode
                + "H" + ANSI_RESET.unicode + " or " + ANSI_YELLOW.unicode + "V"
                + ANSI_RESET.unicode + ").");
        System.out.println(ANSI_CYAN.unicode + "\nFormat:" + ANSI_RESET.unicode + " "
                + ANSI_GREEN.unicode + "<oldStart> <newStart> [H|V]" + ANSI_RESET.unicode);
        System.out.println(ANSI_CYAN.unicode + "Examples:" + ANSI_RESET.unicode);
        System.out.println("  " + ANSI_GREEN.unicode + "5A 3C" + ANSI_RESET.unicode);
        System.out.println("  " + ANSI_GREEN.unicode + "8B 3D V" + ANSI_RESET.unicode);
        System.out.println("\nWhen ready, just enter " + ANSI_YELLOW.unicode
                + "\"start\"" + ANSI_RESET.unicode + ". Good luck!\n");
    }

    public static void printPostGameCommands()
    {
        System.out.println("Please, consider adding a " + ANSI_YELLOW.unicode + "comment" + ANSI_RESET.unicode + ": " + ANSI_GREEN.unicode + "comment <your comment>" + ANSI_RESET.unicode);
        System.out.println("To add a " + ANSI_YELLOW.unicode + "rating" + ANSI_RESET.unicode + ": " + ANSI_GREEN.unicode + "rating <1-5>" + ANSI_RESET.unicode);

        System.out.println("\nRestart the game: " + ANSI_YELLOW.unicode + "restart" + ANSI_RESET.unicode);
        System.out.println("Return to the menu: " + ANSI_GREEN.unicode + "menu" + ANSI_RESET.unicode);
        System.out.println("Leave the game: " + ANSI_RED.unicode + "exit" + ANSI_RESET.unicode);
    }
}
