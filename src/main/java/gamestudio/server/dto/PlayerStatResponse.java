package gamestudio.server.dto;

public record PlayerStatResponse(int ident,
                                 String game,
                                 String player,
                                 int gamesPlayed,
                                 int gamesWon,
                                 int totalScore,
                                 int bestScore,
                                 int rank) {}
