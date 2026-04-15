package gamestudio.service.rest;

import gamestudio.entity.Comment;
import gamestudio.service.CommentService;
import gamestudio.service.exception.CommentException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

public class CommentServiceRestClient implements CommentService
{
    private final String url;
    private final RestTemplate restTemplate;

    public CommentServiceRestClient(RestTemplate restTemplate)
    {
        this.restTemplate = restTemplate;
        this.url = "http://localhost:8080/api/comment";
    }

    @Override
    public void addComment(Comment comment)
    {
        restTemplate.postForEntity(url, comment, Void.class);
    }

    @Override
    public List<Comment> getComments(String game)
    {
        Comment[] comments = restTemplate.getForObject(url + "/" + game, Comment[].class);

        return comments == null ? List.of() : Arrays.asList(comments);
    }

    @Override
    public List<Comment> getPlayerComments(String game, String player)
    {
        Comment[] comments = restTemplate.getForObject(url + "/" + game + "/players/" + player, Comment[].class);

        return comments == null ? List.of() : Arrays.asList(comments);
    }

    @Override
    public void reset() throws CommentException
    {
        throw new UnsupportedOperationException("Not supported via web service");
    }
}
