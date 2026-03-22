package gamestudio.cli;

public enum Colours
{
    ANSI_RED("\u001B[31m"),
    ANSI_GREEN("\u001B[32m"),
    ANSI_YELLOW("\u001B[33m"),
    ANSI_BLUE("\u001B[34m"),
    ANSI_PURPLE("\u001B[35m"),
    ANSI_CYAN("\u001B[36m"),
    ANSI_WHITE("\u001B[97m"),

    ANSI_RESET("\u001B[0m");

    public final String unicode;

    Colours(String unicode) { this.unicode = unicode; }
}
