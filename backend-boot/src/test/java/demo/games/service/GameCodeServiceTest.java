package demo.games.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName( "Game code service" )
public class GameCodeServiceTest {

  @Test
  @DisplayName( "should return a number of random codes without collisions" )
  public void shouldReturnARandomCode() {

    final int testSize = 10_000;
    final int length = 8;

    final Set<String> observedCodes = new TreeSet<>( String.CASE_INSENSITIVE_ORDER );

    final GameCodeService service = new GameCodeService();
    for ( int i = 0; i < testSize; i++ ) {
      final String code = service.nextCode( length );
      assertTrue( observedCodes.add( code ), String.format( "The code %s was already observed", code ) );
    }

    assertEquals( testSize, observedCodes.size() );
  }
}
