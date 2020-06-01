package demo.games.pvp;

import demo.games.shared.Hand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Comparator;

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

    final ResponseEntity<String> created = restTemplate.postForEntity( newGamePath(), new CreateGame( player1 ), String.class );
    assertNotNull( created );
    assertEquals( HttpStatus.CREATED, created.getStatusCode() );

    final String location = created.getHeaders().getLocation().getRawPath();
    assertNotNull( location );

    /* TODO: We should not be parsing links.  Remove once we have the GET game details */
    assertTrue( location.matches( "/game/[a-zA-Z0-9]{8}" ) );
    final String code = location.substring( 6 );

    final ActiveGame[] open = restTemplate.getForObject( listOpenPath(), ActiveGame[].class );
    assertNotNull( open );
    assertTrue( open.length > 0 );

    final Comparator<ActiveGame> comparator = Comparator.comparing( ActiveGame::getCode );
    Arrays.sort( open, comparator );
    assertTrue( Arrays.binarySearch( open, new ActiveGame( code ), comparator ) >= 0 );
  }

  private String newGamePath() {
    return String.format( "http://localhost:%d/game", port );
  }

  private String listOpenPath() {
    return String.format( "http://localhost:%d/game/list/open", port );
  }
}
