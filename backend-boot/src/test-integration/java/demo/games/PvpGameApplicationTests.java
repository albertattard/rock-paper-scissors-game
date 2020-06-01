package demo.games;

import demo.games.model.ActiveGame;
import demo.games.model.CreateGame;
import demo.games.model.Hand;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@DisplayName( "PvP game application" )
@SpringBootTest( webEnvironment = WebEnvironment.RANDOM_PORT )
public class PvpGameApplicationTests {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  @DisplayName( "should play a game" )
  public void shouldPlayAGameAgainstAnotherPlayer() {
    final Hand player1 = Hand.ROCK;

    final ResponseEntity<ActiveGame> entity =
      restTemplate.postForEntity( newGamePath(), new CreateGame( player1 ), ActiveGame.class );
    final String code = assertHeadersAndReturnCode( entity );
    assertThatGameCodeInHeadersMatchesCodeInBody( code, entity );

    final ActiveGame[] activeGames = restTemplate.getForObject( listOpenPath(), ActiveGame[].class );
    assertThatAtLeastOneActiveGameExists( activeGames );
    assertThatTheNewGameIsAmongstTheActiveGames( activeGames, code );
  }

  private String assertHeadersAndReturnCode( final ResponseEntity<ActiveGame> entity ) {
    assertNotNull( entity );
    assertEquals( HttpStatus.CREATED, entity.getStatusCode() );

    final String location = entity.getHeaders().getLocation().getRawPath();
    assertNotNull( location );

    assertTrue( location.matches( "/pvp/[a-zA-Z0-9]{8}" ) );
    return StringUtils.substringAfter( location, "/pvp/" );
  }

  private void assertThatGameCodeInHeadersMatchesCodeInBody( final String code, ResponseEntity<ActiveGame> entity ) {
    assertEquals( code, entity.getBody().getCode() );
  }

  private void assertThatTheNewGameIsAmongstTheActiveGames( final ActiveGame[] activeGames, final String code ) {
    final Set<String> codes = toSetOfCodes( activeGames );
    assertTrue( codes.contains( code ),
      String.format( "The code '%s' was not found in the list of active games: %s", code, Arrays.toString( activeGames ) ) );
  }

  private void assertThatAtLeastOneActiveGameExists( final ActiveGame[] activeGames ) {
    assertNotNull( activeGames );
    assertTrue( activeGames.length > 0 );
  }

  private Set<String> toSetOfCodes( final ActiveGame[] activeGames ) {
    return Arrays.stream( activeGames ).map( ActiveGame::getCode ).collect( Collectors.toSet() );
  }

  private String newGamePath() {
    return String.format( "http://localhost:%d/pvp", port );
  }

  private String listOpenPath() {
    return String.format( "http://localhost:%d/pvp/list/active", port );
  }
}
