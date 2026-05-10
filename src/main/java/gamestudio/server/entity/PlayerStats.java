package gamestudio.server.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "player_stats",
       uniqueConstraints = {
        @UniqueConstraint(
                name = "UniqueGameAndUserId",
                columnNames = {"game", "user_id"}
        )
   }
)
@NamedQuery(name = "PlayerStats.getPlayerStats",
            query = "SELECT s FROM PlayerStats s WHERE s.game=:game AND s.player=:player")
@NamedQuery(name = "PlayerStats.getRank",
        query = "SELECT COUNT(s) FROM PlayerStats s WHERE s.game=:game AND (s.bestScore > :bestScore OR (s.bestScore = :bestScore AND s.totalScore > :totalScore))")
public class PlayerStats implements Serializable
{
    @Id
    @GeneratedValue
    private int ident;

    private String player;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String game;

    @Column(name = "games_played")
    private int gamesPlayed;

    @Column(name = "games_won")
    private int gamesWon;

    @Column(name = "total_score")
    private int totalScore;

    @Column(name = "best_score")
    private int bestScore;

    public PlayerStats() {}

    public PlayerStats(String player, String game, int gamesPlayed,
                       int gamesWon, int totalScore, int bestScore)
    {
        this.player = player.trim();
        this.game = game.trim();
        this.gamesPlayed = gamesPlayed;
        this.gamesWon = gamesWon;
        this.totalScore = totalScore;
        this.bestScore = bestScore;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getBestScore() {
        return bestScore;
    }

    public void setBestScore(int bestScore) {
        this.bestScore = bestScore;
    }

    public int getIdent() {
        return ident;
    }

    public void setIdent(int ident) {
        this.ident = ident;
    }
}
