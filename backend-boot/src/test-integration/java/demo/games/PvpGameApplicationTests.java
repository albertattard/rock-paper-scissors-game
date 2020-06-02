package demo.games;

import demo.games.model.ActiveGame;
import demo.games.model.AllGames;
import demo.games.model.CreateGame;
import demo.games.model.GameDetails;
import demo.games.model.GameState;
import demo.games.model.Hand;
import demo.games.model.PlayGame;
import demo.games.model.PvpOutcome;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@DisplayName( "PvP game application" )
@SpringBootTest( webEnvironment = WebEnvironment.RANDOM_PORT )
public class PvpGameApplicationTests {

  @Test
  @DisplayName( "should play a game against another player" )
  public void shouldPlayAGameAgainstAnotherPlayer() {
    final Hand player1 = Hand.ROCK;
    final Hand player2 = Hand.SCISSORS;

    final String code = createGameAndAssertReturnedCodeAndReturnCode( player1 );
    listActiveGamesAndAssertThatCreatedGamesIsInList( code );
    fetchSingleGameAndAssertThatGameIsStillActive( code );
    playGameAndAssertResult( player1, player2, code );
    listAllGamesAndAssertThatGameIsOnlyInClosedList( code, player1, player2 );
    fetchSingleGameAndAssertThatGameIsNowClosed( code, player1, player2 );
  }

  private String createGameAndAssertReturnedCodeAndReturnCode( final Hand player1 ) {
    final ResponseEntity<ActiveGame> entity =
      restTemplate.postForEntity( newGamePath(), new CreateGame( player1 ), ActiveGame.class );
    assertNotNull( entity );
    assertEquals( HttpStatus.CREATED, entity.getStatusCode() );

    final String location = entity.getHeaders().getLocation().getRawPath();
    assertNotNull( location );
    assertTrue( location.matches( "/pvp/[a-zA-Z0-9]{8}" ) );

    final String code = StringUtils.substringAfter( location, "/pvp/" );
    assertEquals( code, entity.getBody().getCode() );
    return code;
  }

  private void listActiveGamesAndAssertThatCreatedGamesIsInList( final String code ) {
    final ActiveGame[] activeGames = restTemplate.getForObject( listActivePath(), ActiveGame[].class );
    assertNotNull( activeGames );
    assertTrue( activeGames.length > 0 );

    final Set<String> codes = toSetOfCodes( Arrays.stream( activeGames ) );
    assertTrue( codes.contains( code ),
      String.format( "The code '%s' was not found in the list of active games: %s", code, Arrays.toString( activeGames ) ) );
  }

  private void fetchSingleGameAndAssertThatGameIsStillActive( final String code ) {
    final GameDetails game = restTemplate.getForObject( gameDetailsPath( code ), GameDetails.class );
    assertNotNull( game );
    assertEquals( code, game.getCode() );
    assertNull( game.getPlayer1() );
    assertNull( game.getPlayer2() );
    assertNull( game.getOutcome() );
    assertSame( GameState.ACTIVE, game.getState() );
  }

  private void playGameAndAssertResult( final Hand player1, final Hand player2, final String code ) {
    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType( MediaType.APPLICATION_JSON );

    final ResponseEntity<GameDetails> entity =
      restTemplate.exchange(
        gameDetailsPath( code ),
        HttpMethod.PUT,
        new HttpEntity<>( new PlayGame( player2 ), headers ),
        GameDetails.class
      );

    assertNotNull( entity );
    assertEquals( HttpStatus.OK, entity.getStatusCode() );

    final GameDetails closedGame = entity.getBody();
    assertNotNull( closedGame );
    assertEquals( code, closedGame.getCode() );
    assertEquals( player1, closedGame.getPlayer1() );
    assertEquals( player2, closedGame.getPlayer2() );
    assertEquals( PvpOutcome.of( player1, player2 ), closedGame.getOutcome() );
    assertEquals( GameState.CLOSED, closedGame.getState() );
  }

  private void listAllGamesAndAssertThatGameIsOnlyInClosedList( final String code, final Hand player1, final Hand player2 ) {
    final AllGames games = restTemplate.getForObject( listAllPath(), AllGames.class );
    assertNotNull( games );

    final Set<String> activeGames = toSetOfCodes( games.getActiveGames().stream() );
    assertFalse( activeGames.contains( code ),
      String.format( "The game with code '%s' was found in the list of active games: %s", code, games.getActiveGames() ) );

    final Optional<GameDetails> match = games.getClosedGames().stream().filter( game -> code.equals( game.getCode() ) ).findFirst();
    assertTrue( match.isPresent(),
      String.format( "The game with code '%s' was not found in the list of closed games: %s", code, games.getClosedGames() ) );

    assertClosedGame( code, player1, player2, match.get() );
  }

  private void fetchSingleGameAndAssertThatGameIsNowClosed( final String code, final Hand player1, final Hand player2 ) {
    final GameDetails game = restTemplate.getForObject( gameDetailsPath( code ), GameDetails.class );
    assertClosedGame( code, player1, player2, game );
  }

  private void assertClosedGame( final String code, final Hand player1, final Hand player2, final GameDetails game ) {
    assertNotNull( game );
    assertEquals( code, game.getCode() );
    assertSame( player1, game.getPlayer1() );
    assertSame( player2, game.getPlayer2() );
    assertSame( PvpOutcome.of( player1, player2 ), game.getOutcome() );
    assertSame( GameState.CLOSED, game.getState() );
  }

  private Set<String> toSetOfCodes( final Stream<ActiveGame> activeGames ) {
    return activeGames.map( ActiveGame::getCode ).collect( Collectors.toSet() );
  }

  private String newGamePath() {
    return "/pvp";
  }

  private String listActivePath() {
    return "/pvp/list/active";
  }

  private String listAllPath() {
    return "/pvp/list/all";
  }

  private String gameDetailsPath( final String code ) {
    return String.format( "/pvp/%s", code );
  }

  @Autowired
  private TestRestTemplate restTemplate;

}
