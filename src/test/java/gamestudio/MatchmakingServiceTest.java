package gamestudio;

import gamestudio.server.domain.Game;
import gamestudio.server.dto.CreateGameResponse;
import gamestudio.server.dto.MatchmakingResponse;
import gamestudio.server.dto.MatchmakingStatus;
import gamestudio.server.service.GameService;
import gamestudio.server.service.MatchmakingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchmakingServiceTest
{
    @Mock
    private GameService gameService;

    @InjectMocks
    private MatchmakingService matchmakingService;


    @Test
    void findGame_noWaitingGame_createsGameAndReturnsSearching()
    {
        UUID gameId = UUID.randomUUID();
        UUID hostToken = UUID.randomUUID();
        when(gameService.createGame()).thenReturn(new CreateGameResponse(gameId, hostToken));

        MatchmakingResponse response = matchmakingService.findGame();

        assertEquals(gameId, response.gameId());
        assertEquals(hostToken, response.playerToken());
        assertNull(response.opponentUsername());
        assertEquals(MatchmakingStatus.SEARCHING, response.status());
        verify(gameService).createGame();
    }

    @Test
    void findGame_waitingGameCanBeJoined_returnsMatchedGame()
    {
        UUID gameId = UUID.randomUUID();
        UUID hostToken = UUID.randomUUID();
        UUID opponentToken = UUID.randomUUID();
        when(gameService.createGame()).thenReturn(new CreateGameResponse(gameId, hostToken));

        MatchmakingResponse first = matchmakingService.findGame();
        assertEquals(MatchmakingStatus.SEARCHING, first.status());

        Game game = mock(Game.class);
        when(gameService.joinGame(gameId)).thenReturn(opponentToken);
        when(gameService.getGame(gameId)).thenReturn(game);
        when(game.getHostToken()).thenReturn(hostToken);
        when(game.getUsername(hostToken)).thenReturn("host");

        MatchmakingResponse second = matchmakingService.findGame();

        assertEquals(gameId, second.gameId());
        assertEquals(opponentToken, second.playerToken());
        assertEquals("host", second.opponentUsername());
        assertEquals(MatchmakingStatus.MATCHED, second.status());
        verify(gameService, times(1)).createGame();
    }

    @Test
    void findGame_staleWaitingGameCannotBeJoined_skipsItAndCreatesAnotherGame()
    {
        UUID staleGameId = UUID.randomUUID();
        UUID staleHostToken = UUID.randomUUID();
        UUID newGameId = UUID.randomUUID();
        UUID newHostToken = UUID.randomUUID();
        when(gameService.createGame())
                .thenReturn(new CreateGameResponse(staleGameId, staleHostToken))
                .thenReturn(new CreateGameResponse(newGameId, newHostToken));

        matchmakingService.findGame();
        when(gameService.joinGame(staleGameId)).thenReturn(null);

        MatchmakingResponse response = matchmakingService.findGame();

        assertEquals(newGameId, response.gameId());
        assertEquals(newHostToken, response.playerToken());
        assertEquals(MatchmakingStatus.SEARCHING, response.status());
        verify(gameService).joinGame(staleGameId);
        verify(gameService, times(2)).createGame();
    }

    @Test
    void cancelSearch_nullGameId_doesNothing()
    {
        matchmakingService.cancelSearch(null, UUID.randomUUID());

        verifyNoInteractions(gameService);
    }

    @Test
    void cancelSearch_waitingHost_removesGame()
    {
        UUID gameId = UUID.randomUUID();
        UUID hostToken = UUID.randomUUID();
        when(gameService.createGame()).thenReturn(new CreateGameResponse(gameId, hostToken));
        matchmakingService.findGame();

        Game game = mock(Game.class);
        when(gameService.getGame(gameId)).thenReturn(game);
        when(game.getHostToken()).thenReturn(hostToken);

        matchmakingService.cancelSearch(gameId, hostToken);

        verify(gameService).removeGame(gameId);
    }

    @Test
    void cancelSearch_unknownGame_throwsNotFound()
    {
        UUID gameId = UUID.randomUUID();
        when(gameService.getGame(gameId)).thenReturn(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> matchmakingService.cancelSearch(gameId, UUID.randomUUID())
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(gameService, never()).removeGame(any());
    }

    @Test
    void cancelSearch_wrongPlayerToken_throwsForbidden()
    {
        UUID gameId = UUID.randomUUID();
        UUID hostToken = UUID.randomUUID();
        Game game = mock(Game.class);
        when(gameService.getGame(gameId)).thenReturn(game);
        when(game.getHostToken()).thenReturn(hostToken);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> matchmakingService.cancelSearch(gameId, UUID.randomUUID())
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(gameService, never()).removeGame(any());
    }

    @Test
    void cancelSearch_gameIsNotWaiting_throwsConflict()
    {
        UUID gameId = UUID.randomUUID();
        UUID hostToken = UUID.randomUUID();
        Game game = mock(Game.class);
        when(gameService.getGame(gameId)).thenReturn(game);
        when(game.getHostToken()).thenReturn(hostToken);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> matchmakingService.cancelSearch(gameId, hostToken)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(gameService, never()).removeGame(any());
    }
}
