package gamestudio.entity;

import gamestudio.repository.exception.CommentException;

import java.util.Date;

public class Comment
{
    private String player;
    private String game;
    private String comment;
    private Date commentedOn;

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
}
