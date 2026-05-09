package gamestudio.server.entity;

import gamestudio.server.service.exception.CommentException;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;

import java.io.Serializable;
import java.util.Date;

@Entity
@NamedQuery(name = "Comment.getComments",
            query = "SELECT c FROM Comment c WHERE c.game=:game ORDER BY c.commentedOn DESC")
@NamedQuery(name = "Comment.getPlayerComments",
            query = "SELECT c FROM Comment c WHERE c.game=:game AND c.player=:player ORDER BY c.commentedOn DESC")
public class Comment implements Serializable
{
    @Id
    @GeneratedValue
    private int ident;

    private String player;
    private String game;
    private String comment;
    private Date commentedOn;

    public Comment() {}

    public Comment(String player, String game, String comment, Date commentedOn)
    {
        this.player = player.trim();
        this.game = game.trim();
        this.comment = comment.trim();
        if (player.isEmpty() || game.isEmpty() || comment.isEmpty()) throw new CommentException("Invalid comment");
        this.commentedOn = commentedOn;
    }


    public String getPlayer() { return player; }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getCommentedOn() {
        return commentedOn;
    }

    public void setCommentedOn(Date commentedOn) {
        this.commentedOn = commentedOn;
    }

    public int getIdent() { return ident; }

    public void setIdent(int ident) { this.ident = ident; }
}
