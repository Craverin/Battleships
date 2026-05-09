package gamestudio.server.webservice;

import gamestudio.server.entity.Comment;
import gamestudio.server.service.CommentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController
{
    private final CommentService commentService;

    public CommentController(CommentService commentService)
    {
        this.commentService = commentService;
    }

    @GetMapping("/{game}")
    public List<Comment> getComments(@PathVariable String game)
    {
        return commentService.getComments(game);
    }

    @GetMapping("/{game}/players/{player}")
    public List<Comment> getPlayerComments(@PathVariable String game, @PathVariable String player)
    {
        return commentService.getPlayerComments(game, player);
    }

    @PostMapping
    public void addComment(@RequestBody Comment comment)
    {
        commentService.addComment(comment);
    }
}
